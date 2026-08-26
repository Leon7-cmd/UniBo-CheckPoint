package com.example.checkpoint.ui.sections.library

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.Game

/**
 * Filter sections available within the user library.
 */
@Immutable
enum class LibrarySectionType(val title: String) {
    FAVORITES("PREFERITI"),
    COMPLETED("COMPLETATI"),
    NOT_COMPLETED("IN CORSO"),
    TO_PLAY("DA GIOCARE"),
    ALL("TUTTI I GIOCHI")
}

/**
 * Aggregated collections partitioned by game status and favorites.
 */
@Immutable
data class LibraryOverviewData(
    val favorites: List<Game> = emptyList(),
    val completed: List<Game> = emptyList(),
    val inProgress: List<Game> = emptyList(),
    val toPlay: List<Game> = emptyList(),
    val all: List<Game> = emptyList()
)

/**
 * UI State representation for the Library screen.
 */
@Immutable
data class LibraryUiState(
    val overview: LibraryOverviewData = LibraryOverviewData(),
    val games: List<Game> = emptyList(),
    val currentSection: LibrarySectionType? = null,
    val isLoading: Boolean = false
)