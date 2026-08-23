@file:OptIn(ExperimentalLayoutApi::class)

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
import com.example.checkpoint.ui.sections.detail.components.bottomSection.achievement.AchievementsTabContent
import com.example.checkpoint.ui.sections.detail.components.bottomSection.DetailsTabContent
import com.example.checkpoint.ui.sections.detail.components.bottomSection.ReviewsTabContent
import com.example.checkpoint.ui.sections.detail.components.upperSection.DetailHeaderBanner
import com.example.checkpoint.ui.sections.detail.components.upperSection.DetailInfoSection

@Composable
fun GameDetailScreen(
    game: Game,
    achievements: List<Achievement>,
    isLoadingAchievements: Boolean,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onToPlayToggle: () -> Unit,
    onRatingChange: (Float) -> Unit,
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
            // 1. Top Banner + Background image
            item {
                DetailHeaderBanner(
                    coverUrl = game.coverUrl,
                    title = game.title,
                    onBackClick = onBackClick
                )
            }

            // 2. Title, rating and info
            item {
                DetailInfoSection(
                    game = game,
                    onFavoriteToggle = onFavoriteToggle,
                    onToPlayToggle = onToPlayToggle,
                    onRatingChange = onRatingChange
                )
            }

            // 3. Tab Selection
            item {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
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

            // 4. Active Tab Content
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedTab) {
                        DetailTab.DETAILS -> DetailsTabContent(game = game)
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

                        DetailTab.REVIEWS -> ReviewsTabContent(game = game)
                    }
                }
            }
        }
    }
}