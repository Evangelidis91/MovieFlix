package com.evangelidis.movieflix.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evangelidis.movieflix.data.local.FavoritesDataStore
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.Movie
import com.evangelidis.movieflix.domain.model.MoviesPage
import com.evangelidis.movieflix.domain.repository.MovieRepository
import com.evangelidis.movieflix.presentation.toDisplayDate
import com.evangelidis.movieflix.presentation.toRatingText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val favoritesDataStore: FavoritesDataStore
) : ViewModel() {

    private val _moviesResult = MutableStateFlow<DataResult<MoviesPage>?>(null)
    private val favoriteIds = favoritesDataStore.favoriteMovieIds

    private val _isRefreshing = MutableStateFlow(false)
    private val _isLoadingNextPage = MutableStateFlow(false)
    private val _paginatedMovies = MutableStateFlow<List<Movie>>(emptyList())

    val uiState: StateFlow<HomeScreenState> = combine(
        _moviesResult,
        favoriteIds,
        _isRefreshing,
        _isLoadingNextPage,
        _paginatedMovies
    ) { result, favorites, isRefreshing, isLoadingNextPage, paginatedList ->
        buildScreenState(result, favorites, isRefreshing, isLoadingNextPage, paginatedList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeScreenState.Loading
    )

    init {
        loadInitial()
    }

    fun onAction(action: HomeAction) {
        when(action) {
            HomeAction.LoadInitial -> loadInitial()
            HomeAction.Refresh -> refresh()
            HomeAction.LoadNextPage -> loadNextPage()
            HomeAction.Retry -> loadInitial()
            is HomeAction.ToggleFavorite -> toggleFavorite(action.movieId)
            is HomeAction.MovieClick -> {  }
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _moviesResult.value = null
            fetchPage(1)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            fetchPage(1)
            _isRefreshing.value = false
        }
    }

    private fun loadNextPage() {
        val currentState = uiState.value
        if (currentState is HomeScreenState.Content && currentState.hasMorePages && !_isLoadingNextPage.value) {
            viewModelScope.launch {
                _isLoadingNextPage.value = true
                fetchPage(currentState.currentPage + 1)
                _isLoadingNextPage.value = false
            }
        }
    }

    private suspend fun fetchPage(page: Int) {
        val result = repository.getPopularMovies(page)
        _moviesResult.value = result

        if (result is DataResult.Success) {
            if (page == 1) {
                _paginatedMovies.value = result.data.movies
            } else {
                _paginatedMovies.value = _paginatedMovies.value + result.data.movies
            }
        }
    }

    private fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            favoritesDataStore.setFavorites(movieId)
        }
    }

    private fun buildScreenState(
        result: DataResult<MoviesPage>?,
        favorites: Set<Int>,
        isRefreshing: Boolean,
        isLoadingNextPage: Boolean,
        paginatedMovies: List<Movie>
    ) : HomeScreenState {
        if (result == null) return HomeScreenState.Loading

        return when(result) {
            is DataResult.Error -> {
                if (paginatedMovies.isEmpty()) {
                    HomeScreenState.Error(result.throwable.message ?: "Failed to get data")
                } else {
                    HomeScreenState.Content(
                        movies = paginatedMovies.toUiModels(favorites).toImmutableList(),
                        isRefreshing = isRefreshing,
                        isLoadingNextPage = isLoadingNextPage
                    )
                }
            }
            is DataResult.Success -> {
                val uiMovies = paginatedMovies.toUiModels(favorites)
                if (uiMovies.isEmpty()) {
                    HomeScreenState.Empty
                } else {
                    HomeScreenState.Content(
                        movies = uiMovies.toImmutableList(),
                        isRefreshing = isRefreshing,
                        isLoadingNextPage = isLoadingNextPage,
                        isOffline = result.data.isFromCache,
                        currentPage = result.data.page,
                        totalPages = result.data.totalPages
                    )
                }
            }
        }
    }

    private fun List<Movie>.toUiModels(favorites: Set<Int>): List<UiMovie> =
        map { domain ->
            UiMovie(
                id = domain.id,
                title = domain.title,
                overview = domain.overview,
                backdropUrl = domain.backdropUrl,
                posterUrl = domain.posterUrl,
                releaseDateFormatted = domain.releaseDate.toDisplayDate(),
                ratingFormatted = domain.voteAverage.toRatingText(),
                voteAverage = domain.voteAverage,
                isFavorite = domain.id in favorites
            )
        }
}
