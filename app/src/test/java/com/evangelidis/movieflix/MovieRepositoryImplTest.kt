package com.evangelidis.movieflix

import com.evangelidis.movieflix.data.local.MovieDao
import com.evangelidis.movieflix.data.local.MovieEntity
import com.evangelidis.movieflix.data.remote.api.TmdbApiService
import com.evangelidis.movieflix.data.remote.dto.MovieDto
import com.evangelidis.movieflix.data.remote.dto.MoviePageResponseDto
import com.evangelidis.movieflix.data.repository.MovieRepositoryImpl
import com.evangelidis.movieflix.domain.DataResult
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for MovieRepositoryImpl.
 * Verifies offline-first behavior: fetching fresh data from the API,
 * saving it to the local Room database, and falling back to cache when offline.
 */

class MovieRepositoryImplTest {

    private val api: TmdbApiService = mockk()
    private val movieDao: MovieDao = mockk(relaxed = true)

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        repository = MovieRepositoryImpl(api, movieDao)
    }

    @Test
    fun `getPopularMovies success returns movies from API`() = runTest {
        // Given
        val mockDto = MovieDto(1, "Inception", null, "2010-07-16", 8.8)
        val mockResponse = MoviePageResponseDto(1, listOf(mockDto), 10)
        coEvery { api.getPopularMovies(1) } returns mockResponse

        // When
        val result = repository.getPopularMovies(1)

        // Then
        assertTrue(result is DataResult.Success)
        val successData = (result as DataResult.Success).data
        assertEquals("Inception", successData.movies[0].title)
    }

    @Test
    fun `getPopularMovies page 1 error falls back to Room database`() = runTest {
        // Given
        val mockEntity = MovieEntity(1, "Inception Cached", null, "2010-07-16", 8.8, 0)
        coEvery { api.getPopularMovies(1) } throws IOException("Network Error")
        coEvery { movieDao.getCachedMovies() } returns listOf(mockEntity)

        // When
        val result = repository.getPopularMovies(1)

        // Then
        assertTrue(result is DataResult.Success)
        val successData = (result as DataResult.Success).data
        assertEquals("Inception Cached", successData.movies[0].title)
        assertTrue(successData.isFromCache)
    }
}
