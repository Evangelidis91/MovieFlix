package com.evangelidis.movieflix.data.remote.api

import com.evangelidis.movieflix.data.NetworkConstants.DEFAULT_LANGUAGE
import com.evangelidis.movieflix.data.remote.dto.MovieDetailsDto
import com.evangelidis.movieflix.data.remote.dto.MoviePageResponseDto
import com.evangelidis.movieflix.data.remote.dto.ReviewsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int,
        @Query("language") language: String = DEFAULT_LANGUAGE
    ): MoviePageResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits",
        @Query("language") language: String = DEFAULT_LANGUAGE
    ): MovieDetailsDto

    @GET("movie/{movie_id}/reviews")
    suspend fun getMovieReviews(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = DEFAULT_LANGUAGE
    ): ReviewsResponseDto

    @GET("movie/{movie_id}/similar")
    suspend fun getSimilarMovies(
        @Path("movie_id") movieId: Int,
        @Query("page") page: Int = 1,
        @Query("language") language: String = DEFAULT_LANGUAGE
    ): MoviePageResponseDto

}