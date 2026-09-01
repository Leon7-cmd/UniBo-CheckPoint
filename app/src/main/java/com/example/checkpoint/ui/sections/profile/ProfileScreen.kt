package com.example.checkpoint.ui.sections.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.ui.sections.profile.components.ProfileHeaderCard
import com.example.checkpoint.ui.sections.profile.components.StatsSummaryCard
import com.example.checkpoint.ui.sections.profile.components.badge.BadgesSection

/**
 * Main user profile screen showing user level, badges, and gaming statistics.
 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    uiState: ProfileUiState,
    onSeeAllBadgesClick: () -> Unit,
    onAvatarSelected: (Uri?) -> Unit
) {
    // Activity launcher for photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = onAvatarSelected
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "header_title") {
            Text(
                text = "Profilo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // User info, level progression, and avatar
        item(key = "profile_header_card", contentType = "profile_header") {
            ProfileHeaderCard(
                username = uiState.username,
                friendCode = uiState.friendCode,
                avatarUrl = uiState.avatarUrl,
                level = uiState.level,
                currentXp = uiState.currentXp,
                nextLevelXp = uiState.nextLevelXp,
                onAvatarClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        // Badges and achievements showcase
        item(key = "badges_section", contentType = "badges_section") {
            BadgesSection(
                badges = uiState.badges,
                onSeeAllClick = onSeeAllBadgesClick,
                onBadgeClick = { onSeeAllBadgesClick() }
            )
        }

        // Personal gaming statistics summary
        item(key = "stats_summary_card", contentType = "stats_section") {
            StatsSummaryCard(stats = uiState.stats)
        }
    }
}