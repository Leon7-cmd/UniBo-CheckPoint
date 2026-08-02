package com.example.checkpoint.ui.sections.library

import com.example.checkpoint.data.model.Game

data class LibraryUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false
)