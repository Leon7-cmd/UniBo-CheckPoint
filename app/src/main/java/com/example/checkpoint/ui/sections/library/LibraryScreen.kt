@file:OptIn(ExperimentalLayoutApi::class)

package com.example.checkpoint.ui.sections.library

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.ui.components.GameCard
import com.example.checkpoint.ui.components.SectionRow

@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "LIBRERIA PERSONALE",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (expandedCategory != null) {
            // EXPANDED VIEW ("SEE ALL")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expandedCategory!!,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = { expandedCategory = null }) {
                    Text("<- TORNA INDIETRO", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val filteredList = remember(expandedCategory, uiState.games) {
                when (expandedCategory) {
                    "PREFERITI ♡" -> uiState.games.filter { it.isFavorite }
                    else -> uiState.games.filter { it.status.label == expandedCategory }
                }
            }

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (game in filteredList) {
                            GameCard(
                                game = game,
                                onClick = { onGameClick(game.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // STANDARD SECTIONS VIEW
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Favorites
                item {
                    SectionRow(
                        title = "PREFERITI ♡",
                        games = remember(uiState.games) { uiState.games.filter { it.isFavorite } },
                        onSeeAll = { expandedCategory = "PREFERITI ♡" },
                        onGameClick = onGameClick
                    )
                }

                // 2. Completed
                item {
                    SectionRow(
                        title = "COMPLETATI ★",
                        games = remember(uiState.games) { uiState.games.filter { it.status == GameStatus.COMPLETED } },
                        onSeeAll = { expandedCategory = GameStatus.COMPLETED.label },
                        onGameClick = onGameClick
                    )
                }

                // 3. Not Completed
                item {
                    SectionRow(
                        title = "NON-COMPLETATI",
                        games = remember(uiState.games) { uiState.games.filter { it.status == GameStatus.NOT_COMPLETED } },
                        onSeeAll = { expandedCategory = GameStatus.NOT_COMPLETED.label },
                        onGameClick = onGameClick
                    )
                }

                // 4. To Play
                item {
                    SectionRow(
                        title = "DA GIOCARE",
                        games = remember(uiState.games) { uiState.games.filter { it.status == GameStatus.TO_PLAY } },
                        onSeeAll = { expandedCategory = GameStatus.TO_PLAY.label },
                        onGameClick = onGameClick
                    )
                }

                // 5. All Games
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = "TUTTI I GIOCHI",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        for (game in uiState.games) {
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
}