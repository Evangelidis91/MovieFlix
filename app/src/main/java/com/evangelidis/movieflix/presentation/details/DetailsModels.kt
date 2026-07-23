package com.evangelidis.movieflix.presentation.details

import com.evangelidis.movieflix.presentation.home.UiMovie
import kotlinx.collections.immutable.ImmutableList

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
    val genres: ImmutableList<String>,
    val homepageUrl: String?,
    val isFavorite: Boolean,
    val isShareable: Boolean,
    val cast: ImmutableList<UiCastMember>,
    val reviews: ImmutableList<UiReview>,
    val similarMovies: ImmutableList<UiMovie>
)

/** Every possible UI state for the Details Screen */
sealed interface DetailsScreenState {
    data object Loading : DetailsScreenState
    data class Content(val movie: UiMovieDetails) : DetailsScreenState
    data class Error(val message: String) : DetailsScreenState
}
