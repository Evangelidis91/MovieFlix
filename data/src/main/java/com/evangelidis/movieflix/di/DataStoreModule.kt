package com.evangelidis.movieflix.di

import android.content.Context
import com.evangelidis.movieflix.data.local.FavoritesDataStoreImpl
import com.evangelidis.movieflix.data.local.FavoritesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds DataStore implementation to its interface for managing local preferences.
 */

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideFavoritesDataStore(@ApplicationContext context: Context): FavoritesRepository =
        FavoritesDataStoreImpl(context)
}
