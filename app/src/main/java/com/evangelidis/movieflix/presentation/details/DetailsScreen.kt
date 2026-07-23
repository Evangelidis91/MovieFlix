package com.evangelidis.movieflix.presentation.details

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.evangelidis.movieflix.presentation.home.MovieCard
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun DetailsRoute(
    onBackClick: () -> Unit,
    onSimilarMovieClick: (Int) -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    DetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onShareClick = { movie ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, movie.title)
                putExtra(Intent.EXTRA_TEXT, movie.homepageUrl)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Movie"))
        },
        onFavoriteClick = viewModel::toggleFavorite,
        onRetry = viewModel::retry,
        onSimilarMovieClick = onSimilarMovieClick
    )
}

@Composable
fun DetailsScreen(
    uiState: DetailsScreenState,
    onBackClick: () -> Unit,
    onShareClick: (UiMovieDetails) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onRetry: () -> Unit,
    onSimilarMovieClick: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                if (uiState is DetailsScreenState.Content && uiState.movie.isShareable) {
                    IconButton(onClick = { onShareClick(uiState.movie) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is DetailsScreenState.Loading -> CircularProgressIndicator()

                is DetailsScreenState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(text = "Something went wrong", style = MaterialTheme.typography.titleMedium)

                        Spacer(Modifier.height(4.dp))

                        Text(text = uiState.message, color = Color.Gray)

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                is DetailsScreenState.Content -> {
                    DetailsContent(
                        movie = uiState.movie,
                        onFavoriteClick = onFavoriteClick,
                        onSimilarMovieClick = onSimilarMovieClick
                    )
                }
            }
        }
    }
}

@Composable
fun DetailsContent(
    movie: UiMovieDetails,
    onFavoriteClick: (Int) -> Unit,
    onSimilarMovieClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Image Header with Favorite Overlay
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)) {
            AsyncImage(
                model = movie.imageUrl,
                contentDescription = movie.title,
                placeholder = ColorPainter(Color(0xFF2B2B2B)),
                error = ColorPainter(Color(0xFF2B2B2B)),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = { onFavoriteClick(movie.id) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (movie.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (movie.isFavorite) "Remove from favorites" else "Add to favorites",
                    tint = if (movie.isFavorite) Color.Red else Color.White
                )
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            // Title
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            // Genres
            if (movie.genres.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items = movie.genres, key = { it }) { genre ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = genre,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Date / Runtime / Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = movie.releaseDateFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Text(
                    text = movie.runtimeFormatted,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = movie.ratingFormatted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Overview
            Text(
                text = "Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(24.dp))

            // Cast
            if (movie.cast.isNotEmpty()) {
                Text(
                    text = "Cast",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = movie.cast, key = { it.id }) { member ->
                        CastMemberCard(member = member)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Reviews (Capped at max 3 items)
            if (movie.reviews.isNotEmpty()) {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                movie.reviews.forEach { review ->
                    ReviewCard(review = review)

                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(16.dp))
            }

            // Similar Movies (Capped at max 6 items)
            if (movie.similarMovies.isNotEmpty()) {
                Text(
                    text = "Similar Movies",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(items = movie.similarMovies, key = { it.id }) { similar ->
                        Box(modifier = Modifier.width(220.dp)) {
                            MovieCard(
                                movie = similar,
                                onClick = { onSimilarMovieClick(similar.id) },
                                onFavoriteClick = { onFavoriteClick(similar.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CastMemberCard(member: UiCastMember) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        AsyncImage(
            model = member.profileUrl,
            contentDescription = member.name,
            placeholder = ColorPainter(Color(0xFF2B2B2B)),
            error = ColorPainter(Color(0xFF2B2B2B)),
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = member.name,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = member.character,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ReviewCard(review: UiReview) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = review.authorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                review.ratingFormatted?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(14.dp)
                        )

                        Spacer(Modifier.width(2.dp))

                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = review.content,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Previews
@Preview(showBackground = true)
@Composable
fun DetailsContentPreview() {
    MaterialTheme {
        DetailsContent(
            movie = UiMovieDetails(
                id = 1,
                title = "Turbo Kid",
                overview = "In a post-apocalyptic wasteland in 1997, a comic book fan adopts the persona of his favourite hero to save his enthusiastic friend and fight a tyrannical overlord.",
                imageUrl = null,
                releaseDateFormatted = "Aug 28, 2015",
                ratingFormatted = "6.7",
                runtimeFormatted = "1h 33m",
                genres = listOf("Action", "Adventure", "Comedy", "Sci-Fi").toImmutableList(),
                homepageUrl = "",
                isFavorite = true,
                isShareable = true,
                cast = listOf(
                    UiCastMember(1, "Munro Chambers", "The Kid", null),
                    UiCastMember(2, "Laurence Leboeuf", "Apple", null)
                ).toImmutableList(),
                reviews = listOf(
                    UiReview("r1", "Kostas", "9.0", "A masterpiece of modern cinema!")
                ).toImmutableList(),
                similarMovies = persistentListOf()
            ),
            onFavoriteClick = {},
            onSimilarMovieClick = {}
        )
    }
}
