package com.evangelidis.movieflix.domain.model

/** The domain model for a movie in a list. */
data class Movie(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val releaseDate: String?,
    val voteAverage: Double
)

/** Domain model for a page of movies. */
data class MoviesPage(
    val movies: List<Movie>,
    val page: Int,
    val totalPages: Int,
    val isFromCache: Boolean = false
)
