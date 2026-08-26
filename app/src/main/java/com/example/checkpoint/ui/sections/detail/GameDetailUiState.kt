package com.example.checkpoint.ui.sections.detail

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.Review

/**
 * UI State representation for the Game Detail screen.
 */
@Immutable
data class GameDetailUiState(
    val game: Game? = null,
    val achievements: List<Achievement> = emptyList(),
    val communityReviews: List<Review> = emptyList(),
    val averageRating: Float? = null,
    val totalReviewsCount: Int = 0,
    val isLoadingDetails: Boolean = true,
    val isLoadingAchievements: Boolean = false,
    val isLoadingCommunityReviews: Boolean = false,
    val errorMessage: String? = null
)