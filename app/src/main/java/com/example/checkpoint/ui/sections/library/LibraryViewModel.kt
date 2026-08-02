package com.example.checkpoint.ui.sections.library

import androidx.lifecycle.ViewModel
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LibraryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadMockGames()
    }

    private fun loadMockGames() {
        val mockList = listOf(
            Game(
                id = "1",
                title = "Elden Ring",
                coverUrl = "",
                platforms = listOf("PC", "PS5", "Xbox Series X"),
                userRating = 4.5f,
                status = GameStatus.COMPLETED,
                isFavorite = true,
                note = "Capolavoro assoluto, finito in 120 ore."
            ),
            Game(
                id = "2",
                title = "The Witcher 3: Wild Hunt",
                coverUrl = "",
                platforms = listOf("PC", "PS4", "Switch"),
                userRating = 5.0f,
                status = GameStatus.COMPLETED,
                isFavorite = true,
                note = "La miglior storia di sempre."
            ),
            Game(
                id = "3",
                title = "Hollow Knight",
                coverUrl = "",
                platforms = listOf("Nintendo Switch", "PC"),
                userRating = 4.0f,
                status = GameStatus.NOT_COMPLETED,
                isFavorite = false,
                note = "Molto difficile ma artisticamente incredibile."
            ),
            Game(
                id = "4",
                title = "Cyberpunk 2077",
                coverUrl = "",
                platforms = listOf("PC", "PS5"),
                userRating = 3.5f,
                status = GameStatus.TO_PLAY,
                isFavorite = false,
                note = "Da iniziare il DLC Phantom Liberty."
            )
        )

        _uiState.update { it.copy(games = mockList) }
    }
}