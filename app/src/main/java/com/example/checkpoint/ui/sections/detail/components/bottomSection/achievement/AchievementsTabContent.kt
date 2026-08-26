package com.example.checkpoint.ui.sections.detail.components.bottomSection.achievement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Achievement

/**
 * Tab content displaying achievement sync status banner, overall progress bar, and list of trophies.
 */
@Composable
fun AchievementsTabContent(
    achievements: List<Achievement>,
    isUserLoggedInToPlatform: Boolean,
    onConnectPlatformClick: () -> Unit,
    onToggleAchievement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Platform synchronization banner
        if (!isUserLoggedInToPlatform) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Collega il tuo account per sincronizzare i trofei automaticamente.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConnectPlatformClick) {
                        Text("Accedi")
                    }
                }
            }
        }

        if (achievements.isEmpty()) {
            Text(
                text = "Nessun obiettivo disponibile per questo gioco.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val totalCount = achievements.size
            val completedCount = remember(achievements) { achievements.count { it.isCompleted } }
            val progress = remember(completedCount, totalCount) {
                completedCount.toFloat() / totalCount
            }
            val progressPercentage = remember(progress) { (progress * 100).toInt() }

            Column(modifier = Modifier.fillMaxWidth()) {
                // Progress stats header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trofei ottenuti: $completedCount / $totalCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$progressPercentage%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Trophy list items
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    achievements.forEach { achievement ->
                        key(achievement.id) {
                            AchievementItemRow(
                                achievement = achievement,
                                isReadOnly = isUserLoggedInToPlatform,
                                onToggle = { onToggleAchievement(achievement.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}