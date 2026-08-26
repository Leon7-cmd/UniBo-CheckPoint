package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Domain model representing an individual game review and rating submitted by a user.
 */
@Immutable
@Serializable
data class Review(
    val userId: String = "",
    val username: String = "Player",
    val userAvatarFileName: String? = null,
    val rating: Float = 0f,
    val reviewText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)