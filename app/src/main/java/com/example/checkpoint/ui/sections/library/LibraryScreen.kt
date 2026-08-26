package com.example.checkpoint.ui.sections.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.checkpoint.ui.sections.library.components.MainLibraryOverviewContent
import com.example.checkpoint.ui.sections.library.components.SpecificSectionDetailContent

/**
 * Root library screen handling navigation between the main overview and specific section views.
 */
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onSectionSelected: (LibrarySectionType?) -> Unit,
    onBackToMainLibrary: () -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back navigation when browsing inside a specific section
    BackHandler(enabled = uiState.currentSection != null) {
        onBackToMainLibrary()
    }

    Box(modifier = modifier.fillMaxSize()) {
        val currentSection = uiState.currentSection

        if (currentSection == null) {
            // Main library dashboard showing category previews
            MainLibraryOverviewContent(
                overview = uiState.overview,
                onSeeAllClick = onSectionSelected,
                onGameClick = onGameClick
            )
        } else {
            // Retrieve pre-filtered list directly from state overview
            val sectionGames = when (currentSection) {
                LibrarySectionType.FAVORITES -> uiState.overview.favorites
                LibrarySectionType.COMPLETED -> uiState.overview.completed
                LibrarySectionType.NOT_COMPLETED -> uiState.overview.inProgress
                LibrarySectionType.TO_PLAY -> uiState.overview.toPlay
                LibrarySectionType.ALL -> uiState.overview.all
            }

            // Detailed grid/list view for the selected section
            SpecificSectionDetailContent(
                sectionType = currentSection,
                games = sectionGames,
                onBackClick = onBackToMainLibrary,
                onGameClick = onGameClick
            )
        }
    }
}