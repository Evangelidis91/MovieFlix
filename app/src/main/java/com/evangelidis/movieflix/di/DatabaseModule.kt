package com.evangelidis.movieflix.di

import android.content.Context
import androidx.room.Room
import com.evangelidis.movieflix.data.local.MovieFlixDatabase
import com.evangelidis.movieflix.data.local.dao.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MovieFlixDatabase =
        Room.databaseBuilder(context, MovieFlixDatabase::class.java, "movieflix.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMovieDao(database: MovieFlixDatabase): MovieDao =
        database.movieDao()
}