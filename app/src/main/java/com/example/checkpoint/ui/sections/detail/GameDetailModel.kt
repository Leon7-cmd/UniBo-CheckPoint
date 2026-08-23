package com.example.checkpoint.ui.sections.detail

import com.example.checkpoint.data.model.Game

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val game: Game) : DetailUiState
    data class Error(val message: String) : DetailUiState
}
enum class DetailTab(val title: String) {
    DETAILS("DETTAGLI"),
    ACHIEVEMENTS("ACHIEVEMENTS"),
    REVIEWS("RECENSIONI")
}