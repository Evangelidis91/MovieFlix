package com.evangelidis.movieflix.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidis.movieflix.data.local.FavoritesRepository
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.Movie
import com.evangelidis.movieflix.domain.repository.MovieRepository
import com.evangelidis.movieflix.presentation.toDisplayDate
import com.evangelidis.movieflix.presentation.toRatingText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val favoritesDataStore: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val _effects = Channel<HomeEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var favoriteIds: Set<Int> = emptySet()

    init {
        observeFavorites()
        loadInitial()
    }

    /**
     * The single entry point for Home screen actions.
     */
    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Refresh -> refresh()
            HomeIntent.LoadNextPage -> loadNextPage()
            HomeIntent.Retry -> loadInitial()
            is HomeIntent.ToggleFavorite -> toggleFavorite(intent.movieId)
            is HomeIntent.MovieClicked -> navigateToDetails(intent.movieId)
        }
    }

    /**
     * Observe favorite IDs and update every movie already displayed.
     */
    private fun observeFavorites() {
        viewModelScope.launch {
            favoritesDataStore.getFavoriteMovieIds().collect { favorites ->
                favoriteIds = favorites

                _uiState.update { currentState ->
                    currentState.copy(
                        movies = currentState.movies
                            .map { movie ->
                                movie.copy(
                                    isFavorite = movie.id in favorites
                                )
                            }
                            .toImmutableList()
                    )
                }
            }
        }
    }

    /**
     * Load the first page when the screen opens or when Retry is selected.
     */
    private fun loadInitial() {
        _uiState.update { currentState ->
            currentState.copy(
                isInitialLoading = true,
                isRefreshing = false,
                isLoadingNextPage = false,
                errorMessage = null,
                loadMoreError = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.getPopularMovies(page = 1)) {
                is DataResult.Success -> {
                    val movies = result.data.movies
                        .distinctBy(Movie::id)
                        .toUiModels(favoriteIds)

                    _uiState.update {
                        HomeState(
                            movies = movies,
                            isInitialLoading = false,
                            isOffline = result.data.isFromCache,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages
                        )
                    }
                }

                is DataResult.Error -> {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isInitialLoading = false,
                            errorMessage = result.throwable.message ?: "Failed to load movies"
                        )
                    }
                }
            }
        }
    }

    /**
     * Replace the current movie list with a fresh first page.
     */
    private fun refresh() {
        val currentState = _uiState.value

        if (currentState.isInitialLoading || currentState.isRefreshing || currentState.isLoadingNextPage) {
            return
        }

        _uiState.update {
            it.copy(
                isRefreshing = true,
                errorMessage = null,
                loadMoreError = null
            )
        }

        viewModelScope.launch {
            when (val result = repository.getPopularMovies(page = 1)) {
                is DataResult.Success -> {
                    val movies = result.data.movies
                        .distinctBy(Movie::id)
                        .toUiModels(favoriteIds)

                    _uiState.update { state ->
                        state.copy(
                            movies = movies,
                            isRefreshing = false,
                            isOffline = result.data.isFromCache,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            errorMessage = null,
                            loadMoreError = null
                        )
                    }
                }

                is DataResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isRefreshing = false,
                            errorMessage = result.throwable.message ?: "Failed to refresh movies"
                        )
                    }
                }
            }
        }
    }

    /**
     * Load the next available page.
     */
    private fun loadNextPage() {
        val currentState = _uiState.value

        if (
            currentState.isInitialLoading ||
            currentState.isRefreshing ||
            currentState.isLoadingNextPage ||
            currentState.isOffline ||
            !currentState.hasMorePages
        ) {
            return
        }

        val nextPage = currentState.currentPage + 1

        _uiState.update {
            it.copy(
                isLoadingNextPage = true
            )
        }

        viewModelScope.launch {
            when (val result = repository.getPopularMovies(nextPage)) {
                is DataResult.Success -> {
                    _uiState.update { state ->
                        val newMovies = result.data.movies
                            .map { movie ->
                                movie.toUiModel(favoriteIds)
                            }

                        val combinedMovies = (state.movies + newMovies)
                            .distinctBy(UiMovie::id)
                            .toImmutableList()

                        state.copy(
                            movies = combinedMovies,
                            isLoadingNextPage = false,
                            currentPage = result.data.page,
                            totalPages = result.data.totalPages,
                            loadMoreError = null
                        )
                    }
                }

                is DataResult.Error -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoadingNextPage = false,
                            loadMoreError = result.throwable.message ?: "Failed to load more movies"
                        )
                    }
                }
            }
        }
    }

    private fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            favoritesDataStore.setFavorites(movieId)
        }
    }

    private fun navigateToDetails(movieId: Int) {
        _effects.trySend(
            HomeEffect.NavigateToDetails(movieId)
        )
    }

    private fun List<Movie>.toUiModels(favorites: Set<Int>) = map { movie ->
        movie.toUiModel(favorites)
    }.toImmutableList()

    private fun Movie.toUiModel(favorites: Set<Int>) = UiMovie(
        id = id,
        title = title,
        imageUrl = imageUrl,
        releaseDateFormatted = releaseDate.toDisplayDate(),
        ratingFormatted = voteAverage.toRatingText(),
        isFavorite = id in favorites
    )
}
