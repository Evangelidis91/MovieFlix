package com.evangelidis.movieflix.presentation.details

import com.evangelidis.movieflix.presentation.home.UiMovie

data class UiCastMember(
    val id: Int,
    val name: String,
    val character: String,
    val profileUrl: String?
)

data class UiReview(
    val id: String,
    val authorName: String,
    val avatarUrl: String?,
    val ratingFormatted:String?,
    val content: String
)

data class UiMovieDetails(
    val id: Int,
    val title: String,
    val overview: String,
    val backdropUrl: String?,
    val posterUrl: String?,
    val releaseDateFormatted: String,
    val ratingFormatted: String,
    val runtimeFormatted: String,
    val genres: List<String>,
    val homepageUrl: String?,
    val isFavorite: Boolean,
    val isShareable: Boolean,
    val cast: List<UiCastMember>,
    val reviews: List<UiReview>,
    val similarMovies: List<UiMovie>
)

/** Every possible UI state for the Details Screen */
sealed interface DetailsScreenState {
    data object Loading : DetailsScreenState
    data class Content(val movie: UiMovieDetails) : DetailsScreenState
    data class Error(val message: String) : DetailsScreenState
}

/** Actions triggered from the Details Screen */
sealed interface DetailsAction {
    data object ToggleFavorite : DetailsAction
    data object ShareClick : DetailsAction
    data object Retry : DetailsAction
    data object BackClick : DetailsAction
    data class SimilarMovieClick(val movieId: Int) : DetailsAction
}
