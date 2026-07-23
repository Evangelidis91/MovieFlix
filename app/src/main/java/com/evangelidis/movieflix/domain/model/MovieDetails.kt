package com.evangelidis.movieflix.domain.model

/** The domain model for detailed movie information. */
data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    val imageUrl: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val runtimeMinutes: Int?,
    val genres: List<String>,
    val homepageUrl: String?,
    val cast: List<CastMember> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val similarMovies: List<Movie> = emptyList()
) {
    /** Hide share button if homepage URL is null or blank */
    val isShareable: Boolean get() = !homepageUrl.isNullOrBlank()
}

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?
)

data class Review(
    val id: String,
    val authorName: String,
    val rating: Double?,
    val content: String
)
