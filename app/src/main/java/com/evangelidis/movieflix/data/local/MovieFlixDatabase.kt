package com.evangelidis.movieflix.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [MovieEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MovieFlixDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
}