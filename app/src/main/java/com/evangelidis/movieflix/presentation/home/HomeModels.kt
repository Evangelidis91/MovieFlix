package com.evangelidis.movieflix.presentation.home

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * UI model to be displayed by Compose.
 */
data class UiMovie(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val releaseDateFormatted: String,
    val ratingFormatted: String,
    val isFavorite: Boolean
)

/**
 * The complete state of the Home screen.
 */
data class HomeState(
    val movies: ImmutableList<UiMovie> = persistentListOf(),
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val isOffline: Boolean = false,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val errorMessage: String? = null,
    val loadMoreError: String? = null
) {
    val hasMorePages: Boolean
        get() = currentPage < totalPages

    val isEmpty: Boolean
        get() = !isInitialLoading && errorMessage == null && movies.isEmpty()
}

/**
 * Actions requested by the user or the UI.
 */
sealed interface HomeIntent {
    data object Refresh : HomeIntent
    data object LoadNextPage : HomeIntent
    data object Retry : HomeIntent
    data class ToggleFavorite(val movieId: Int) : HomeIntent
    data class MovieClicked(val movieId: Int) : HomeIntent
}

/**
 * One-time events that should not be stored inside HomeState.
 */
sealed interface HomeEffect {
    data class NavigateToDetails(val movieId: Int) : HomeEffect
}
