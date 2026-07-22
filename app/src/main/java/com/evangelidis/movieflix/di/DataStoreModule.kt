package com.evangelidis.movieflix.di

import android.content.Context
import com.evangelidis.movieflix.data.local.FavoritesDataStore
import com.evangelidis.movieflix.data.local.FavoritesDataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideFavoritesDataStore(@ApplicationContext context: Context): FavoritesDataStore =
        FavoritesDataStoreImpl(context)
}