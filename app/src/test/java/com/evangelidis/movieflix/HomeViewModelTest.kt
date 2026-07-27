package com.evangelidis.movieflix

import com.evangelidis.movieflix.data.local.FavoritesDataStore
import com.evangelidis.movieflix.domain.DataResult
import com.evangelidis.movieflix.domain.model.Movie
import com.evangelidis.movieflix.domain.model.MoviesPage
import com.evangelidis.movieflix.domain.repository.MovieRepository
import com.evangelidis.movieflix.presentation.home.HomeViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for HomeViewModel.
 *
 * Verifies the HomeState produced after successful and failed
 * initial movie requests.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: MovieRepository = mockk()
    private val favoritesDataStore: FavoritesDataStore = mockk()

    private lateinit var viewModel: HomeViewModel

    private val sampleMovie = Movie(
        id = 1,
        title = "Inception",
        imageUrl = null,
        releaseDate = "2010-07-16",
        voteAverage = 8.8
    )

    @Before
    fun setUp() {
        // favoriteMovieIds is a Flow property, not a suspend function.
        every {
            favoritesDataStore.favoriteMovieIds
        } returns flowOf(emptySet())
    }

    @Test
    fun `when repository returns success, state contains movies`() = runTest {
        // Given
        coEvery {
            repository.getPopularMovies(page = 1)
        } returns DataResult.Success(
            MoviesPage(
                movies = listOf(sampleMovie),
                page = 1,
                totalPages = 5
            )
        )

        // When
        viewModel = HomeViewModel(
            repository = repository,
            favoritesDataStore = favoritesDataStore
        )

        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value

        assertFalse(state.isInitialLoading)
        assertNull(state.errorMessage)
        assertFalse(state.isOffline)
        assertEquals(1, state.currentPage)
        assertEquals(5, state.totalPages)
        assertEquals(1, state.movies.size)

        val movie = state.movies.first()

        assertEquals(1, movie.id)
        assertEquals("Inception", movie.title)
        assertEquals("16 Jul 2010", movie.releaseDateFormatted)
        assertEquals("8.8", movie.ratingFormatted)
        assertFalse(movie.isFavorite)
    }

    @Test
    fun `when repository returns error, state contains error message`() = runTest {
        // Given
        coEvery {
            repository.getPopularMovies(page = 1)
        } returns DataResult.Error(
            IOException("No Internet")
        )

        // When
        viewModel = HomeViewModel(
            repository = repository,
            favoritesDataStore = favoritesDataStore
        )

        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value

        assertFalse(state.isInitialLoading)
        assertTrue(state.movies.isEmpty())
        assertEquals("No Internet", state.errorMessage)
        assertEquals(0, state.currentPage)
        assertEquals(1, state.totalPages)
    }
}
