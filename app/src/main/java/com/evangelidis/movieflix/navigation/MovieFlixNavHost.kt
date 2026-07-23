package com.evangelidis.movieflix.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.evangelidis.movieflix.presentation.details.DetailsRoute
import com.evangelidis.movieflix.presentation.home.HomeRoute

@Composable
fun MovieFlixNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Route.Home) {
        composable<Route.Home> {
            HomeRoute(
                onMovieClick = { movieId ->
                    navController.navigate(Route.Details(movieId))
                }
            )
        }

        composable<Route.Details> {
            DetailsRoute(
                onBackClick = {
                    navController.popBackStack()
                },
                onSimilarMovieClick = { movieId ->
                    navController.navigate(Route.Details(movieId))
                }
            )
        }
    }
}