package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Progression and library categorization status for a game.
 */
@Immutable
@Serializable
enum class GameStatus(val label: String) {
    NONE("Nessuno"),
    TO_PLAY("DA GIOCARE"),
    NOT_COMPLETED("NON-COMPLETATI"),
    COMPLETED("COMPLETATI ★")
}

/**
 * Core domain model representing a video game, its metadata, user library state, and ratings.
 */
@Immutable
@Serializable
data class Game(
    val id: String,
    val title: String,
    val coverUrl: String = "",
    val releaseDate: String = "",
    val description: String = "",
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val developer: String = "",
    val publisher: String = "",
    val steamAppId: String? = null,
    val retroGameId: String? = null,
    val status: GameStatus = GameStatus.NONE,
    val isFavorite: Boolean = false,
    val rating: Float = 0f,
    val communityRating: Float? = null,
    val userReview: String = "",
    val note: String = ""
)