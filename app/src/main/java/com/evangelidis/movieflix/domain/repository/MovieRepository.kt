package com.evangelidis.movieflix.domain.repository

import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.MovieDetails
import com.evangelidis.movieflix.domain.model.MoviesPage

/** The contract for fetching movies. Connect this repo with the ViewModel */
interface MovieRepository {
    suspend fun getPopularMovies(page: Int): DataResult<MoviesPage>
    suspend fun getMovieDetails(movieId: Int): DataResult<MovieDetails>
}
