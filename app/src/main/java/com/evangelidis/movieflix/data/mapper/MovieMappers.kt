package com.evangelidis.movieflix.data.mapper

import com.evangelidis.movieflix.data.NetworkConstants.IMAGE_BASE_URL
import com.evangelidis.movieflix.data.local.MovieEntity
import com.evangelidis.movieflix.data.remote.dto.*
import com.evangelidis.movieflix.domain.model.*

/**
 * Mappers to convert between Network DTOs, Room Entities, and Domain Models.
 */

private const val BACKDROP_SIZE = "w780"
private const val POSTER_SIZE = "w342"
private const val PROFILE_SIZE = "w185"
private const val MAX_SIMILAR_MOVIES = 6
private const val MAX_REVIEWS = 3

private fun String?.toTmdbUrl(size: String = BACKDROP_SIZE): String? {
    return if (!this.isNullOrBlank()) "$IMAGE_BASE_URL$size$this" else null
}

/** Converts raw API DTOs into clean domain models. */
fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropPath.toTmdbUrl(),
    posterUrl = posterPath.toTmdbUrl(POSTER_SIZE),
    releaseDate = releaseDate,
    voteAverage = voteAverage
)

fun MoviePageResponseDto.toMoviesPage(): MoviesPage = MoviesPage(
    movies = results.map { it.toDomain() },
    page = page,
    totalPages = totalPages
)

/** Get 6 similar movies */
fun MoviePageResponseDto.toSimilarMovies(): List<Movie> =
    results.take(MAX_SIMILAR_MOVIES).map { it.toDomain() }

fun MovieDetailsDto.toDomain(
    reviews: List<Review>,
    similarMovies: List<Movie>
): MovieDetails = MovieDetails(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropPath.toTmdbUrl(),
    posterUrl = posterPath.toTmdbUrl(POSTER_SIZE),
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    runtimeMinutes = runtime,
    genres = genres.map { it.name },
    homepageUrl = homepage,
    cast = credits?.cast.orEmpty().sortedBy { it.order }.map { it.toDomain() },
    reviews = reviews,
    similarMovies = similarMovies
)

fun CastDto.toDomain(): CastMember = CastMember(
    id = id,
    name = name,
    character = character,
    profileUrl = profilePath.toTmdbUrl(PROFILE_SIZE)
)

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    authorName = author,
    avatarUrl = authorDetails?.avatarPath.toTmdbUrl(PROFILE_SIZE),
    rating = authorDetails?.rating,
    content = content
)

/** Get 3 reviews */
fun ReviewsResponseDto.toDomain(): List<Review> =
    results.take(MAX_REVIEWS).map { it.toDomain() }

/** Room Mappers */
fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropUrl,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage
)

fun Movie.toCachedEntity(position: Int): MovieEntity = MovieEntity(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropUrl,
    posterUrl = posterUrl,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    position = position
)