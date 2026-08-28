package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Domain model representing an unlockable user achievement badge and its associated experience points.
 */
@Immutable
@Serializable
data class ProfileBadge(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val xpValue: Int = 0,
    val isUnlocked: Boolean = false
)