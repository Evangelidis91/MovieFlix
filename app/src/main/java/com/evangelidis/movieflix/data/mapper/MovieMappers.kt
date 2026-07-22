package com.evangelidis.movieflix.data.mapper

import com.evangelidis.movieflix.data.local.MovieEntity
import com.evangelidis.movieflix.data.remote.dto.*
import com.evangelidis.movieflix.domain.model.*

private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"

private fun String?.toTmdbUrl(size: String = "w780"): String? {
    return if (!this.isNullOrBlank()) "$IMAGE_BASE_URL$size$this" else null
}

/** Converts raw API DTOs into clean domain models used by the rest of the app. */
fun MovieDto.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropPath.toTmdbUrl("w780"),
    posterUrl = posterPath.toTmdbUrl("w342"),
    releaseDate = releaseDate,
    voteAverage = voteAverage
)

fun MoviePageResponseDto.toMoviesPage(): MoviesPage = MoviesPage(
    movies = results.map { it.toDomain() },
    page = page,
    totalPages = totalPages
)

/** Enforces the assignment's "up to 6" similar-movies cap */
fun MoviePageResponseDto.toSimilarMovies(limit: Int = 6): List<Movie> =
    results.take(limit).map { it.toDomain() }

fun MovieDetailsDto.toDomain(
    reviews: List<Review>,
    similarMovies: List<Movie>
): MovieDetails = MovieDetails(
    id = id,
    title = title,
    overview = overview,
    backdropUrl = backdropPath.toTmdbUrl("w780"),
    posterUrl = posterPath.toTmdbUrl("w342"),
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
    profileUrl = profilePath.toTmdbUrl("w185")
)

fun ReviewDto.toDomain(): Review = Review(
    id = id,
    authorName = author,
    avatarUrl = authorDetails?.avatarPath.toTmdbUrl("w185"),
    rating = authorDetails?.rating,
    content = content
)

/** Enforces the assignment's "up to 3" reviews cap */
fun ReviewsResponseDto.toDomain(limit: Int = 3): List<Review> =
    results.take(limit).map { it.toDomain() }

// Room Mappers
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