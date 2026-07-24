package com.evangelidis.movieflix

import com.evangelidis.movieflix.data.local.FavoritesDataStore
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.Movie
import com.evangelidis.movieflix.domain.model.MoviesPage
import com.evangelidis.movieflix.domain.repository.MovieRepository
import com.evangelidis.movieflix.presentation.home.HomeScreenState
import com.evangelidis.movieflix.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for HomeViewModel.
 * Verifies UI state transitions (Content, Error)
 */

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()
    private val favoritesDataStore: FavoritesDataStore = mockk(relaxed = true)

    private lateinit var viewModel: HomeViewModel

    private val sampleMovie = Movie(
        id = 1,
        title = "Inception",
        imageUrl = null,
        releaseDate = "2010-07-16",
        voteAverage = 8.8
    )

    @Before
    fun setup() {
        coEvery { favoritesDataStore.favoriteMovieIds } returns flowOf(emptySet())
    }

    @Test
    fun `when repository returns success, uiState becomes Content`() = runTest {
        // Given
        val successResult = DataResult.Success(
            MoviesPage(
                movies = listOf(sampleMovie),
                page = 1,
                totalPages = 5
            )
        )
        coEvery { repository.getPopularMovies(1) } returns successResult

        // When
        viewModel = HomeViewModel(repository, favoritesDataStore)

        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is HomeScreenState.Content)

        val content = state as HomeScreenState.Content
        assertEquals(1, content.movies.size)
        assertEquals("Inception", content.movies[0].title)
    }

    @Test
    fun `when repository returns error, uiState becomes Error`() = runTest {
        // Given
        coEvery { repository.getPopularMovies(1) } returns DataResult.Error(Exception("No Internet"))

        // When
        viewModel = HomeViewModel(repository, favoritesDataStore)

        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is HomeScreenState.Error)

        val errorState = state as HomeScreenState.Error
        assertEquals("No Internet", errorState.message)
    }

}
