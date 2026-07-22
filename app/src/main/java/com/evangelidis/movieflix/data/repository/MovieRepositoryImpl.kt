package com.evangelidis.movieflix.data.repository

import com.evangelidis.movieflix.data.local.dao.MovieDao
import com.evangelidis.movieflix.data.mapper.toCachedEntity
import com.evangelidis.movieflix.data.mapper.toDomain
import com.evangelidis.movieflix.data.mapper.toMoviesPage
import com.evangelidis.movieflix.data.mapper.toSimilarMovies
import com.evangelidis.movieflix.data.remote.api.TmdbApiService
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.MovieDetails
import com.evangelidis.movieflix.domain.model.MoviesPage
import com.evangelidis.movieflix.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

/** Fetch movies data and wraps the results in DataResult (Success or Error). */
class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApiService,
    private val movieDao: MovieDao
) : MovieRepository {

    override suspend fun getPopularMovies(page: Int): DataResult<MoviesPage> =
        try {
            val response = api.getPopularMovies(page)
            val moviesPage = response.toMoviesPage()

            // Save page 1 to Room for offline fallback
            if (page == 1) {
                val entities = moviesPage.movies.mapIndexed { index, movie ->
                    movie.toCachedEntity(index)
                }
                movieDao.replaceAll(entities)
            }

            DataResult.Success(moviesPage)
        } catch (e: Exception) {
            if (page == 1) {
                // Offline fallback for page 1
                val cached = movieDao.getCachedMovies()
                if (cached.isNotEmpty()) {
                    DataResult.Success(
                        MoviesPage(
                            movies = cached.map { it.toDomain() },
                            page = 1,
                            totalPages = 1,
                            isFromCache = true
                        )
                    )
                } else {
                    DataResult.Error(e)
                }
            } else {
                DataResult.Error(e)
            }
        }


    override suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails> =
        try {
            coroutineScope {
                // Execute details, reviews, and similar requests concurrently
                val detailsDeferred = async { api.getMovieDetails(movieId) }
                val reviewsDeferred = async { runCatching { api.getMovieReviews(movieId) } }
                val similarDeferred = async { runCatching { api.getSimilarMovies(movieId) } }

                val detailsDto = detailsDeferred.await()
                val reviewsDto = reviewsDeferred.await().getOrNull()
                val similarDto = similarDeferred.await().getOrNull()

                val reviews = reviewsDto?.toDomain().orEmpty()
                val similarMovies = similarDto?.toSimilarMovies().orEmpty()

                val domainModel = detailsDto.toDomain(
                    reviews = reviews,
                    similarMovies = similarMovies
                )

                DataResult.Success(domainModel)
            }
        } catch (e: Exception) {
            DataResult.Error(e)
        }
}