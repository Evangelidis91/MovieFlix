package com.evangelidis.movieflix.data.repository

import com.evangelidis.movieflix.data.local.MovieDao
import com.evangelidis.movieflix.data.local.MovieImageCache
import com.evangelidis.movieflix.data.mapper.toCachedEntity
import com.evangelidis.movieflix.data.mapper.toDomain
import com.evangelidis.movieflix.data.mapper.toMoviesPage
import com.evangelidis.movieflix.data.mapper.toSimilarMovies
import com.evangelidis.movieflix.data.remote.api.TmdbApiService
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.Movie
import com.evangelidis.movieflix.domain.model.MovieDetails
import com.evangelidis.movieflix.domain.model.MoviesPage
import com.evangelidis.movieflix.domain.repository.MovieRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/** Fetch movies data and wraps the results in DataResult (Success or Error). */
class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApiService,
    private val movieDao: MovieDao,
    private val movieImageCache: MovieImageCache
) : MovieRepository {


    override suspend fun getPopularMovies(page: Int): DataResult<MoviesPage> {
        val moviesPage = try {
            api.getPopularMovies(page).toMoviesPage()
        } catch (e: CancellationException) {
            throw e
        } catch (networkError: Exception) {
            return if (page == 1) {
                getCachedPage(networkError)
            } else {
                DataResult.Error(networkError)
            }
        }

        if (page == 1) {
            cacheFirstPage(moviesPage)
        }

        return DataResult.Success(moviesPage)
    }

    private suspend fun cacheFirstPage(moviesPage: MoviesPage) {
        try {
            val entities = moviesPage.movies.mapIndexed { index, movie ->
                movie.toCachedEntity(index)
            }

            movieDao.replaceAll(entities)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Fresh network data remains valid even if Room fails.
        }

        try {
            movieImageCache.prefetch(
                moviesPage.movies.mapNotNull(Movie::imageUrl)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Image caching should not invalidate the network result.
        }
    }

    private suspend fun getCachedPage(
        networkError: Exception
    ): DataResult<MoviesPage> {
        return try {
            val cachedMovies = movieDao.getCachedMovies()

            if (cachedMovies.isEmpty()) {
                DataResult.Error(networkError)
            } else {
                DataResult.Success(
                    MoviesPage(
                        movies = cachedMovies.map { it.toDomain() },
                        page = 1,
                        totalPages = 1,
                        isFromCache = true
                    )
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (cacheError: Exception) {
            networkError.addSuppressed(cacheError)
            DataResult.Error(networkError)
        }
    }

    override suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails> =
        try {
            coroutineScope {
                // Execute details, reviews, and similar requests
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
            if (e is CancellationException) throw e
            DataResult.Error(e)
        }
}
