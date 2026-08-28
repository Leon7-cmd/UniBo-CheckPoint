package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Aggregated statistics representing user gaming activity and milestones.
 */
@Immutable
@Serializable
data class UserStats(
    val gamesPlayed: Int = 0,
    val gamesCompleted: Int = 0,
    val gamesReviewed: Int = 0,
    val totalAchievements: Int = 0
)