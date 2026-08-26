package com.example.checkpoint.ui.sections.search

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.Game

/**
 * UI State representation for the Search screen.
 */
@Immutable
sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val games: List<Game>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}