package com.evangelidis.movieflix.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.evangelidis.movieflix.R

@Composable
fun HomeRoute(
    onMovieClick: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetails -> onMovieClick(effect.movieId)
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun HomeScreen(uiState: HomeState, onIntent: (HomeIntent) -> Unit) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isInitialLoading -> CircularProgressIndicator()

                uiState.errorMessage != null && uiState.movies.isEmpty() -> {
                    HomeErrorContent(
                        message = uiState.errorMessage,
                        onRetry = {
                            onIntent(HomeIntent.Retry)
                        }
                    )
                }

                uiState.isEmpty -> {
                    Text(
                        text = stringResource(R.string.no_movies_found),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                else -> {
                    HomeContent(
                        uiState = uiState,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.error_something_went_wrong),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun HomeContent(
    uiState: HomeState,
    onIntent: (HomeIntent) -> Unit
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisibleItem != null && totalItems > 0 && lastVisibleItem.index >= totalItems - 4
        }
    }

    // Run the pagination check again when the current page changes
    LaunchedEffect(
        shouldLoadMore,
        uiState.currentPage,
        uiState.isOffline,
        uiState.totalPages,
        uiState.isRefreshing,
        uiState.loadMoreError
    ) {
        if (shouldLoadMore) {
            onIntent(HomeIntent.LoadNextPage)
        }
    }

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { onIntent(HomeIntent.Refresh) },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isOffline) {
                item(key = "offline_message") {
                    OfflineMessage()
                }
            }

            if (uiState.errorMessage != null) {
                item(key = "refresh_error") {
                    ErrorMessage(
                        message = uiState.errorMessage,
                        actionText = stringResource(R.string.retry),
                        onAction = {
                            onIntent(HomeIntent.Refresh)
                        }
                    )
                }
            }

            items(items = uiState.movies, key = UiMovie::id) { movie ->
                MovieCard(
                    movie = movie,
                    onClick = { onIntent(HomeIntent.MovieClicked(movie.id)) },
                    onFavoriteClick = { onIntent(HomeIntent.ToggleFavorite(movie.id)) }
                )
            }

            val loadMoreError = uiState.loadMoreError

            if (loadMoreError != null) {
                item(key = "pagination_error") {
                    ErrorMessage(
                        message = loadMoreError,
                        actionText = stringResource(R.string.retry),
                        onAction = {
                            onIntent(HomeIntent.LoadNextPage)
                        },
                        isLoading = uiState.isLoadingNextPage
                    )
                }
            } else if (uiState.isLoadingNextPage) {
                item(key = "pagination_loader") {
                    PaginationLoader()
                }
            }
        }
    }
}

@Composable
private fun OfflineMessage() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.offline_message),
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    isLoading: Boolean = false
) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(8.dp))

            Button(onClick = onAction, enabled = !isLoading) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun PaginationLoader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun MovieCard(
    movie: UiMovie,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                AsyncImage(
                    model = movie.imageUrl,
                    contentDescription = movie.title,
                    placeholder = ColorPainter(Color(0xFF2B2B2B)),
                    error = ColorPainter(Color(0xFF2B2B2B)),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (movie.isFavorite) { Icons.Filled.Favorite } else { Icons.Filled.FavoriteBorder },
                        contentDescription = if (movie.isFavorite) { stringResource(R.string.remove_from_favorites) } else { stringResource(R.string.add_to_favorites) },
                        tint = if (movie.isFavorite) { MaterialTheme.colorScheme.error } else { Color.White }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (movie.releaseDateFormatted.isNotEmpty()) {
                        Text(
                            text = movie.releaseDateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (movie.ratingFormatted.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(Modifier.width(4.dp))

                            Text(
                                text = movie.ratingFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun MovieCardPreview() {
    MaterialTheme {
        MovieCard(
            movie = UiMovie(
                id = 1,
                title = "Inception",
                imageUrl = null,
                releaseDateFormatted = "16 Jul 2010",
                ratingFormatted = "8.4",
                isFavorite = false
            ),
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavoriteMovieCardPreview() {
    MaterialTheme {
        MovieCard(
            movie = UiMovie(
                id = 1,
                title = "Inception",
                imageUrl = null,
                releaseDateFormatted = "16 Jul 2010",
                ratingFormatted = "8.4",
                isFavorite = true
            ),
            onClick = {},
            onFavoriteClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeErrorContentPreview() {
    MaterialTheme {
        HomeErrorContent(
            message = "Unable to connect to the server.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun OfflineMessagePreview() {
    MaterialTheme {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            OfflineMessage()
        }
    }
}
