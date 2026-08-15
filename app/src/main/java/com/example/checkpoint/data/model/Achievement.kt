package com.example.checkpoint.data.model

enum class AchievementSource {
    STEAM,
    RETRO_ACHIEVEMENTS,
    SYSTEM_DEFAULT
}

// Class used to map the structure of an achievement
data class Achievement(
    val id: String,
    val gameId: String,
    val title: String,
    val description: String,
    val iconUrl: String,
    val isCompleted: Boolean = false,
    val source: AchievementSource = AchievementSource.SYSTEM_DEFAULT
)