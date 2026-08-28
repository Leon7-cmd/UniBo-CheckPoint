package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Domain model representing a connected friend's profile, stats, unlocked badges, and visibility settings.
 */
@Immutable
@Serializable
data class Friend(
    val id: String,
    val username: String,
    val friendCode: String,
    val avatarUrl: String? = null,
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 500,
    val stats: UserStats = UserStats(),
    val badges: List<ProfileBadge> = emptyList(),
    val games: List<Game> = emptyList(),
    val statsPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val badgesPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val libraryPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC
)

/**
 * Domain model representing an incoming friend invitation request.
 */
@Immutable
@Serializable
data class FriendRequest(
    val senderUid: String = "",
    val senderUsername: String = "Player",
    val senderAvatarUrl: String? = null,
    val senderLevel: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)