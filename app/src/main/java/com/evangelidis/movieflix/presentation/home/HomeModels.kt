package com.evangelidis.movieflix.presentation.home

import kotlinx.collections.immutable.ImmutableList

/** UI Model - Ready to be rendered by Compose */
data class UiMovie(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val releaseDateFormatted: String,
    val ratingFormatted: String,
    val isFavorite: Boolean
)

/** Every possible UI state for the Home Screen */
sealed interface HomeScreenState {
    data object Loading : HomeScreenState
    data class Content(
        val movies: ImmutableList<UiMovie>,
        val isRefreshing: Boolean = false,
        val isLoadingNextPage: Boolean = false,
        val isOffline: Boolean = false,
        val currentPage: Int = 1,
        val totalPages: Int = 1
    ) : HomeScreenState {
        val hasMorePages: Boolean get() = currentPage < totalPages
    }
    data object Empty : HomeScreenState
    data class Error(val message: String) : HomeScreenState
}

/** User actions that can be triggered from the Home Screen */
sealed interface HomeAction {
    data object LoadInitial : HomeAction
    data object Refresh : HomeAction
    data object LoadNextPage : HomeAction
    data class ToggleFavorite(val movieId: Int) : HomeAction
    //data class MovieClick(val movieId: Int) : HomeAction
    data object Retry : HomeAction
}
