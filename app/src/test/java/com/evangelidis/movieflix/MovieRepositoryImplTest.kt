package com.evangelidis.movieflix

import com.evangelidis.movieflix.data.local.MovieDao
import com.evangelidis.movieflix.data.local.MovieEntity
import com.evangelidis.movieflix.data.local.MovieImageCache
import com.evangelidis.movieflix.data.remote.api.TmdbApiService
import com.evangelidis.movieflix.data.remote.dto.MovieDto
import com.evangelidis.movieflix.data.remote.dto.MoviePageResponseDto
import com.evangelidis.movieflix.data.repository.MovieRepositoryImpl
import com.evangelidis.movieflix.domain.DataResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * Unit tests for MovieRepositoryImpl.
 *
 * Verifies successful API loading, local persistence, image prefetching,
 * and the offline Room fallback for page 1.
 */
class MovieRepositoryImplTest {

    private val api: TmdbApiService = mockk()
    private val movieDao: MovieDao = mockk(relaxed = true)
    private val movieImageCache: MovieImageCache = mockk(relaxed = true)

    private lateinit var repository: MovieRepositoryImpl

    @Before
    fun setUp() {
        repository = MovieRepositoryImpl(
            api = api,
            movieDao = movieDao,
            movieImageCache = movieImageCache
        )
    }

    @Test
    fun `getPopularMovies success returns API movies and caches page 1`() =
        runTest {
            // Given
            val expectedImageUrl =
                "https://image.tmdb.org/t/p/w500/inception.jpg"

            val movieDto = MovieDto(
                id = 1,
                title = "Inception",
                backdropPath = "/inception.jpg",
                releaseDate = "2010-07-16",
                voteAverage = 8.8
            )

            val response = MoviePageResponseDto(
                page = 1,
                results = listOf(movieDto),
                totalPages = 10
            )

            coEvery {
                api.getPopularMovies(page = 1)
            } returns response

            coEvery {
                movieDao.replaceAll(any())
            } returns Unit

            coEvery {
                movieImageCache.prefetch(any())
            } returns Unit

            // When
            val result = repository.getPopularMovies(page = 1)

            // Then
            assertTrue(result is DataResult.Success)

            val page = (result as DataResult.Success).data
            val movie = page.movies.first()

            assertEquals(1, page.page)
            assertEquals(10, page.totalPages)
            assertFalse(page.isFromCache)
            assertEquals(1, page.movies.size)

            assertEquals(1, movie.id)
            assertEquals("Inception", movie.title)
            assertEquals(expectedImageUrl, movie.imageUrl)
            assertEquals("2010-07-16", movie.releaseDate)
            assertEquals(8.8, movie.voteAverage)

            coVerify(exactly = 1) {
                movieDao.replaceAll(
                    match { entities ->
                        entities.size == 1 &&
                                entities.first().id == 1 &&
                                entities.first().title == "Inception" &&
                                entities.first().position == 0
                    }
                )
            }

            coVerify(exactly = 1) {
                movieImageCache.prefetch(
                    listOf(expectedImageUrl)
                )
            }
        }

    @Test
    fun `getPopularMovies page 1 failure returns cached Room movies`() =
        runTest {
            // Given
            val cachedMovie = MovieEntity(
                id = 1,
                title = "Inception Cached",
                imageUrl = "https://example.com/inception.jpg",
                releaseDate = "2010-07-16",
                voteAverage = 8.8,
                position = 0
            )

            coEvery {
                api.getPopularMovies(page = 1)
            } throws IOException("Network Error")

            coEvery {
                movieDao.getCachedMovies()
            } returns listOf(cachedMovie)

            // When
            val result = repository.getPopularMovies(page = 1)

            // Then
            assertTrue(result is DataResult.Success)

            val page = (result as DataResult.Success).data
            val movie = page.movies.first()

            assertTrue(page.isFromCache)
            assertEquals(1, page.page)
            assertEquals(1, page.totalPages)
            assertEquals(1, page.movies.size)
            assertEquals("Inception Cached", movie.title)

            coVerify(exactly = 1) {
                movieDao.getCachedMovies()
            }

            coVerify(exactly = 0) {
                movieDao.replaceAll(any())
            }

            coVerify(exactly = 0) {
                movieImageCache.prefetch(any())
            }
        }
}
