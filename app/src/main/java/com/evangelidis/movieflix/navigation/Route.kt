package com.evangelidis.movieflix.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object Home : Route

    @Serializable
    data class Details(val movieId: Int) : Route
}
