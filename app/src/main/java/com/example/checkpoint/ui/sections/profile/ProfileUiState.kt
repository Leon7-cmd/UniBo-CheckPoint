package com.example.checkpoint.ui.sections.profile

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.ProfileBadge
import com.example.checkpoint.data.model.UserStats

/**
 * UI State representation for the user Profile screen.
 */
@Immutable
data class ProfileUiState(
    val username: String = "Player",
    val email: String = "",
    val friendCode: String = "CKP-000000",
    val avatarUrl: String? = null,
    val level: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 500,
    val stats: UserStats = UserStats(),
    val badges: List<ProfileBadge> = emptyList(),
    val isLoading: Boolean = false
)