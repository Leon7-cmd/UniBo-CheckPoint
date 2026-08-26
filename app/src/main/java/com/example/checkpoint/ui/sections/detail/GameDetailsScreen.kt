package com.example.checkpoint.ui.sections.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.Review
import com.example.checkpoint.ui.sections.detail.components.bottomSection.DetailsTabContent
import com.example.checkpoint.ui.sections.detail.components.bottomSection.achievement.AchievementsTabContent
import com.example.checkpoint.ui.sections.detail.components.bottomSection.review.ReviewsTabContent
import com.example.checkpoint.ui.sections.detail.components.upperSection.DetailHeaderBanner
import com.example.checkpoint.ui.sections.detail.components.upperSection.DetailInfoSection

/**
 * Available tabs within the Game Detail screen.
 */
@Immutable
enum class DetailTab(val title: String) {
    DETAILS("DETTAGLI"),
    ACHIEVEMENTS("ACHIEVEMENTS"),
    REVIEWS("RECENSIONI")
}

/**
 * Comprehensive game detail screen showcasing media banner, metadata, tabs for info, achievements, and reviews.
 */
@Composable
fun GameDetailScreen(
    game: Game,
    achievements: List<Achievement>,
    communityReviews: List<Review>,
    averageRating: Float?,
    totalReviewsCount: Int,
    isLoadingAchievements: Boolean,
    isLoadingCommunityReviews: Boolean,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onToPlayToggle: () -> Unit,
    onSaveReview: (rating: Float, reviewText: String) -> Unit,
    onDeleteReview: () -> Unit,
    onToggleAchievement: (String) -> Unit,
    modifier: Modifier = Modifier,
    isUserLoggedInToPlatform: Boolean = false,
    onConnectPlatformClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(DetailTab.DETAILS) }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header banner with game cover and back navigation
            item(key = "detail_banner", contentType = "header_banner") {
                DetailHeaderBanner(
                    coverUrl = game.coverUrl,
                    title = game.title,
                    onBackClick = onBackClick
                )
            }

            // Game metadata, rating, favorite and backlog toggles
            item(key = "detail_info", contentType = "header_info") {
                DetailInfoSection(
                    game = game,
                    averageRating = averageRating,
                    totalReviewsCount = totalReviewsCount,
                    onFavoriteToggle = onFavoriteToggle,
                    onToPlayToggle = onToPlayToggle
                )
            }

            // Tab navigation selector
            item(key = "detail_tabs", contentType = "tabs_row") {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DetailTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = { Text(tab.title, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Dynamic tab content section
            item(key = "tab_content_${selectedTab.name}", contentType = "tab_content") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTab) {
                        DetailTab.DETAILS -> {
                            DetailsTabContent(game = game)
                        }

                        DetailTab.ACHIEVEMENTS -> {
                            if (isLoadingAchievements) {
                                CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                            } else {
                                AchievementsTabContent(
                                    achievements = achievements,
                                    isUserLoggedInToPlatform = isUserLoggedInToPlatform,
                                    onConnectPlatformClick = onConnectPlatformClick,
                                    onToggleAchievement = onToggleAchievement
                                )
                            }
                        }

                        DetailTab.REVIEWS -> {
                            ReviewsTabContent(
                                game = game,
                                communityReviews = communityReviews,
                                isLoadingCommunityReviews = isLoadingCommunityReviews,
                                onSaveReview = onSaveReview,
                                onDeleteReview = onDeleteReview
                            )
                        }
                    }
                }
            }
        }
    }
}