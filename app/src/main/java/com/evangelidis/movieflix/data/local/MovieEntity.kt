package com.evangelidis.movieflix.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Offline snapshot of the Home screen's */
@Entity(tableName = "cached_movies")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    val backdropUrl: String?,
    val posterUrl: String?,
    val releaseDate: String?,
    val voteAverage: Double,
    val position: Int
)