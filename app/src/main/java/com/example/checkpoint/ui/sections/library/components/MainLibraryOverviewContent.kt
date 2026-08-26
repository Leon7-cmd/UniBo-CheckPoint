package com.example.checkpoint.ui.sections.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.ui.sections.library.LibraryOverviewData
import com.example.checkpoint.ui.sections.library.LibrarySectionType

/**
 * Main library dashboard displaying preview rows for each category status.
 */
@Composable
fun MainLibraryOverviewContent(
    overview: LibraryOverviewData,
    onSeeAllClick: (LibrarySectionType) -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item(key = "header_title") {
            Text(
                text = "LIBRERIA",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Empty library fallback
        if (overview.all.isEmpty()) {
            item(key = "empty_library_placeholder") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp, start = 16.dp, end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "La tua libreria è vuota. Aggiungi dei giochi dalla ricerca!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Favorites category row
        if (overview.favorites.isNotEmpty()) {
            item(key = "row_fav", contentType = "category_row") {
                LibraryCategoryRow(
                    title = "PREFERITI",
                    games = overview.favorites,
                    onSeeAllClick = { onSeeAllClick(LibrarySectionType.FAVORITES) },
                    onGameClick = onGameClick
                )
            }
        }

        // Completed games category row
        if (overview.completed.isNotEmpty()) {
            item(key = "row_comp", contentType = "category_row") {
                LibraryCategoryRow(
                    title = "COMPLETATI",
                    games = overview.completed,
                    onSeeAllClick = { onSeeAllClick(LibrarySectionType.COMPLETED) },
                    onGameClick = onGameClick
                )
            }
        }

        // In-progress games category row
        if (overview.inProgress.isNotEmpty()) {
            item(key = "row_prog", contentType = "category_row") {
                LibraryCategoryRow(
                    title = "IN CORSO",
                    games = overview.inProgress,
                    onSeeAllClick = { onSeeAllClick(LibrarySectionType.NOT_COMPLETED) },
                    onGameClick = onGameClick
                )
            }
        }

        // To-play backlog category row
        if (overview.toPlay.isNotEmpty()) {
            item(key = "row_play", contentType = "category_row") {
                LibraryCategoryRow(
                    title = "DA GIOCARE",
                    games = overview.toPlay,
                    onSeeAllClick = { onSeeAllClick(LibrarySectionType.TO_PLAY) },
                    onGameClick = onGameClick
                )
            }
        }

        // All games category row
        if (overview.all.isNotEmpty()) {
            item(key = "row_all", contentType = "category_row") {
                LibraryCategoryRow(
                    title = "TUTTI I GIOCHI",
                    games = overview.all,
                    onSeeAllClick = { onSeeAllClick(LibrarySectionType.ALL) },
                    onGameClick = onGameClick
                )
            }
        }
    }
}