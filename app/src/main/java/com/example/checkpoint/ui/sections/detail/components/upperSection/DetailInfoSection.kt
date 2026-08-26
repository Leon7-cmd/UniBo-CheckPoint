package com.example.checkpoint.ui.sections.detail.components.upperSection

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import java.util.Locale

/**
 * Upper detail section with the game title, quick actions (favorite, backlog), metadata, rating, and platform tags.
 */
@Composable
fun DetailInfoSection(
    game: Game,
    averageRating: Float?,
    totalReviewsCount: Int,
    onFavoriteToggle: () -> Unit,
    onToPlayToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayPlatforms = game.platforms.ifEmpty { listOf("N/D") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title and quick action buttons (Favorite & Backlog)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = game.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (game.isFavorite) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                        tint = if (game.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onToPlayToggle) {
                    Icon(
                        imageVector = Icons.Default.WatchLater,
                        contentDescription = "Da Giocare",
                        tint = if (game.status == GameStatus.TO_PLAY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Development metadata & Rating summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                MetadataRow(label = "Data", value = game.releaseDate)
                MetadataRow(label = "Developer", value = game.developer)
                MetadataRow(label = "Publisher", value = game.publisher)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (averageRating != null) {
                        String.format(Locale.US, "VOTO %.1f/5 (%d)", averageRating, totalReviewsCount)
                    } else {
                        "NON VOTATO"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (averageRating != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                RatingStarsDisplay(rating = averageRating)
            }
        }

        // Horizontal scrollable platform chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            displayPlatforms.forEach { platform ->
                SuggestionChip(
                    onClick = { },
                    label = { Text(platform, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
private fun RatingStarsDisplay(
    rating: Float?,
    modifier: Modifier = Modifier
) {
    val currentRating = rating?.coerceIn(0f, 5f) ?: 0f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..5) {
            val icon = when {
                currentRating >= i -> Icons.Default.Star
                currentRating >= i - 0.5f -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Outlined.Star
            }
            val isHighlighted = currentRating >= i - 0.5f

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Text(
        text = "$label: ${value.ifEmpty { "N/D" }}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}