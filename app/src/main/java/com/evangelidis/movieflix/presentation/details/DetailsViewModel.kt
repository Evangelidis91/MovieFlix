package com.evangelidis.movieflix.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.evangelidis.movieflix.data.local.FavoritesDataStore
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.MovieDetails
import com.evangelidis.movieflix.domain.repository.MovieRepository
import com.evangelidis.movieflix.navigation.Route
import com.evangelidis.movieflix.presentation.home.UiMovie
import com.evangelidis.movieflix.presentation.toDisplayDate
import com.evangelidis.movieflix.presentation.toRatingText
import com.evangelidis.movieflix.presentation.toRuntimeText
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

/**
 * Classic MVVM: a single StateFlow the UI renders from, plus plain
 * callable functions (retry, toggleFavorite) - no sealed Action
 * hierarchy and no onAction() dispatcher, unlike HomeViewModel's MVI.
 */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
    private val favoritesDataStore: FavoritesDataStore
) : ViewModel() {

    private val movieId: Int = savedStateHandle.toRoute<Route.Details>().movieId

    private val _movieDetailsResult = MutableStateFlow<DataResult<MovieDetails>?>(null)
    private val favoriteIds = favoritesDataStore.favoriteMovieIds

    val uiState: StateFlow<DetailsScreenState> = combine(
        _movieDetailsResult,
        favoriteIds
    ) { result, favorites ->
        buildScreenState(result, favorites)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailsScreenState.Loading
    )

    init {
        loadDetails()
    }

    fun retry() = loadDetails()

    /**
     * Takes an explicit [movieId] rather than always toggling this
     * screen's own movie. Handles favoriting either the main movie
     * or any card in the Similar Movies row.
     */
    fun toggleFavorite(movieId: Int) {
        viewModelScope.launch {
            favoritesDataStore.setFavorites(movieId)
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _movieDetailsResult.value = null
            _movieDetailsResult.value = repository.getMovieDetails(movieId)
        }
    }

    private fun buildScreenState(
        result: DataResult<MovieDetails>?,
        favorites: Set<Int>
    ): DetailsScreenState {
        if (result == null) return DetailsScreenState.Loading

        return when (result) {
            is DataResult.Error -> DetailsScreenState.Error(result.throwable.message ?: "Failed to load details")
            is DataResult.Success -> DetailsScreenState.Content(result.data.toUiModel(favorites))
        }
    }

    private fun MovieDetails.toUiModel(favorites: Set<Int>): UiMovieDetails {
        return UiMovieDetails(
            id = id,
            title = title,
            overview = overview,
            backdropUrl = backdropUrl,
            posterUrl = posterUrl,
            releaseDateFormatted = releaseDate.toDisplayDate(),
            ratingFormatted = voteAverage.toRatingText(),
            runtimeFormatted = runtimeMinutes.toRuntimeText(),
            genres = genres.toImmutableList(),
            homepageUrl = homepageUrl,
            isFavorite = id in favorites,
            isShareable = isShareable,
            cast = cast.map { member ->
                UiCastMember(
                    id = member.id,
                    name = member.name,
                    character = member.character,
                    profileUrl = member.profileUrl
                )
            }.toImmutableList(),
            reviews = reviews.map { review ->
                UiReview(
                    id = review.id,
                    authorName = review.authorName,
                    avatarUrl = review.avatarUrl,
                    ratingFormatted = review.rating?.toRatingText(),
                    content = review.content
                )
            }.toImmutableList(),
            similarMovies = similarMovies.map { similar ->
                UiMovie(
                    id = similar.id,
                    title = similar.title,
                    overview = similar.overview,
                    backdropUrl = similar.backdropUrl,
                    posterUrl = similar.posterUrl,
                    releaseDateFormatted = similar.releaseDate.toDisplayDate(),
                    ratingFormatted = similar.voteAverage.toRatingText(),
                    voteAverage = similar.voteAverage,
                    isFavorite = similar.id in favorites
                )
            }.toImmutableList()
        )
    }
}
