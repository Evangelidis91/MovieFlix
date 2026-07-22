package com.evangelidis.movieflix.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.evangelidis.movieflix.data.local.MovieEntity

@Dao
interface MovieDao {

    @Query("SELECT * FROM cached_movies ORDER BY position ASC")
    suspend fun getCachedMovies(): List<MovieEntity>

    @Upsert
    suspend fun upsertAll(movies: List<MovieEntity>)

    @Query("DELETE FROM cached_movies")
    suspend fun clearAll()

    /** Atomically replaces page 1 cache with new results */
    @Transaction
    suspend fun replaceAll(movies: List<MovieEntity>) {
        clearAll()
        upsertAll(movies)
    }
}