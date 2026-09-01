package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable

enum class AchievementSource {
    STEAM,
    RETRO_ACHIEVEMENTS,
    SYSTEM_DEFAULT
}

/**
 * Domain model used to map the structure of an achievement
*/
@Immutable
data class Achievement(
    val id: String,
    val gameId: String,
    val title: String,
    val description: String,
    val iconUrl: String,
    val isCompleted: Boolean = false,
    val source: AchievementSource = AchievementSource.SYSTEM_DEFAULT
)