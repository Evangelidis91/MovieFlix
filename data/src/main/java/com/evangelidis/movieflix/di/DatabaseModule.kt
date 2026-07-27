package com.evangelidis.movieflix.di

import android.content.Context
import androidx.room.Room
import com.evangelidis.movieflix.data.local.MovieDao
import com.evangelidis.movieflix.data.local.MovieFlixDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Provides Room database instance and Data Access Object dependencies.
 */

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MovieFlixDatabase =
        Room.databaseBuilder(context, MovieFlixDatabase::class.java, "movieflix.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideMovieDao(database: MovieFlixDatabase): MovieDao =
        database.movieDao()
}
