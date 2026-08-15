package com.example.checkpoint.data.remote.retro

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Response DTO for RetroAchievements API
@Serializable
data class RetroGameExtendedResponse(
    @SerialName("ID")
    val id: Long = 0L,

    @SerialName("Title")
    val title: String? = null,

    // Dynamic map for achievements
    @SerialName("Achievements")
    val achievements: Map<String, RetroAchievementDto>? = emptyMap()
)

@Serializable
data class RetroAchievementDto(
    @SerialName("ID")
    val id: Long,

    @SerialName("Title")
    val title: String? = null,

    @SerialName("Description")
    val description: String? = null,

    @SerialName("Points")
    val points: Int = 0,

    @SerialName("BadgeName")
    val badgeName: String? = null
)