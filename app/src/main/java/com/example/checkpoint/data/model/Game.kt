package com.example.checkpoint.data.model

import kotlinx.serialization.Serializable

enum class GameStatus(val label: String) {
    NONE(""),
    FAVORITES("PREFERITI ♡"),
    COMPLETED("COMPLETATI ★"),
    NOT_COMPLETED("NON-COMPLETATI"),
    TO_PLAY("DA GIOCARE")
}

// Class used to map the structure of a game
@Serializable
data class Game(
    val id: String,
    val title: String,
    val coverUrl: String = "",
    val releaseDate: String = "",
    val description: String = "",

    // Platform and Tags
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),

    // Info Development
    val developer: String = "",
    val publisher: String = "",

    // ID External Services
    val steamAppId: String? = null,
    val retroGameId: String? = null,

    // State ed User Experience (Locale)
    val status: GameStatus = GameStatus.NONE,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val userReview: String = "",
    val note: String = ""
)