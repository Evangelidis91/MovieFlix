package com.evangelidis.movieflix.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoviePageResponseDto(
    @SerialName("page") val page: Int = 1,
    @SerialName("results") val results: List<MovieDto> = emptyList(),
    @SerialName("total_pages") val totalPages: Int = 1
)

@Serializable
data class MovieDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0
)

@Serializable
data class MovieDetailsDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String = "",
    @SerialName("overview") val overview: String = "",
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("vote_average") val voteAverage: Double = 0.0,
    @SerialName("runtime") val runtime: Int? = null,
    @SerialName("homepage") val homepage: String? = null,
    @SerialName("genres") val genres: List<GenreDto> = emptyList(),
    @SerialName("credits") val credits: CreditsDto? = null
)

@Serializable
data class GenreDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = ""
)

@Serializable
data class CreditsDto(
    @SerialName("cast") val cast: List<CastDto> = emptyList()
)

@Serializable
data class CastDto(
    @SerialName("id") val id: Int,
    @SerialName("name") val name: String = "",
    @SerialName("character") val character: String = "",
    @SerialName("profile_path") val profilePath: String? = null,
    @SerialName("order") val order: Int = 0
)

@Serializable
data class ReviewsResponseDto(
    @SerialName("results") val results: List<ReviewDto> = emptyList()
)

@Serializable
data class ReviewDto(
    @SerialName("id") val id: String,
    @SerialName("author") val author: String = "",
    @SerialName("author_details") val authorDetails: AuthorDetailsDto? = null,
    @SerialName("content") val content: String = ""
)

@Serializable
data class AuthorDetailsDto(
    @SerialName("rating") val rating: Double? = null
)
