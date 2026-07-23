package com.evangelidis.movieflix.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages saving and reading the user's favorite movie IDs.
 */
private val Context.dataStore by preferencesDataStore(name = "favorites_prefs")

interface FavoritesDataStore {
    val favoriteMovieIds: Flow<Set<Int>>
    suspend fun setFavorites(ids: Set<Int>)
}

@Singleton
class FavoritesDataStoreImpl @Inject constructor(
    private val context: Context
) : FavoritesDataStore {

    private val favoritesKey = stringSetPreferencesKey("favorite_movie_ids")

    override val favoriteMovieIds: Flow<Set<Int>> = context.dataStore.data.map { prefs ->
        prefs[favoritesKey]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    override suspend fun setFavorites(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[favoritesKey] = ids.map { it.toString() }.toSet()
        }
    }
}