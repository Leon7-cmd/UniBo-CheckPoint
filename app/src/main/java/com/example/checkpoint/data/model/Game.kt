package com.example.checkpoint.data.model

enum class GameStatus(val label: String) {
    COMPLETED("COMPLETATI"),
    NOT_COMPLETED("NON-COMPLETATI"),
    TO_PLAY("DA GIOCARE")
}

data class Game(
    val id: String,
    val title: String,
    val coverUrl: String,
    val platforms: List<String>,
    val userRating: Float,
    val status: GameStatus,
    val isFavorite: Boolean = false,
    val note: String = ""
)