package com.example.checkpoint.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.checkpoint.data.model.Game
import java.util.Locale

/**
 * Reusable vertical game card presenting the cover poster, title, primary platform, and rating badge.
 */
@Composable
fun GameCard(
    game: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val displayRating = game.communityRating ?: if (game.rating > 0f) game.rating else null
    val ratingText = displayRating?.let { String.format(Locale.US, "%.1f", it) } ?: "-"
    val primaryPlatform = game.platforms.firstOrNull().orEmpty()

    // Constrain image decoding dimensions for performance
    val imageRequest = remember(game.coverUrl) {
        game.coverUrl.ifBlank { null }?.let { url ->
            ImageRequest.Builder(context)
                .data(url)
                .size(240, 320)
                .scale(Scale.FILL)
                .crossfade(true)
                .build()
        }
    }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = modifier.width(140.dp)
    ) {
        Column {
            // Game poster image or placeholder
            AsyncImage(
                model = imageRequest,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            // Info column: title, platform, rating
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = game.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = primaryPlatform,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (displayRating != null) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = ratingText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (displayRating != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}