@file:OptIn(ExperimentalLayoutApi::class)

package com.example.checkpoint.ui.sections.detail.components.upperSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus

@Composable
fun DetailInfoSection(
    game: Game,
    onFavoriteToggle: () -> Unit,
    onToPlayToggle: () -> Unit,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Title
        Text(
            text = game.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Action button
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (game.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Preferito",
                        tint = if (game.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onToPlayToggle) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Da Giocare",
                        tint = if (game.status == GameStatus.TO_PLAY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Rating stars
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "VOTO ${game.rating.toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= game.rating) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Voto $i",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onRatingChange(i.toFloat()) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Developer, publisher, release date
            Column(modifier = Modifier.weight(1f)) {
                MetadataRow(label = "Data", value = game.releaseDate)
                MetadataRow(label = "Developer", value = game.developer)
                MetadataRow(label = "Publisher", value = game.publisher)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Playable platforms
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.widthIn(max = 170.dp)
            ) {
                val displayPlatforms = game.platforms.ifEmpty { listOf("N/D") }
                displayPlatforms.forEach { platform ->
                    SuggestionChip(
                        onClick = { },
                        label = { Text(platform, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Text(
        text = "$label: ${value.ifEmpty { "N/D" }}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}