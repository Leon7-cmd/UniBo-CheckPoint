package com.example.checkpoint.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Game

@Composable
fun SectionRow(
    title: String,
    games: List<Game>,
    onSeeAll: () -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            TextButton(onClick = onSeeAll) {
                Text("VEDI TUTTI ->", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (games.isEmpty()) {
            Text(
                text = "Nessun gioco in questa lista",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (game in games) {
                        GameCard(
                            game = game,
                            onClick = { onGameClick(game.id) }
                        )
                    }
                }
            }
        }
    }
}