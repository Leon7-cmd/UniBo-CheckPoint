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

@Composable
fun AchievementsTabContent(
    achievements: List<Achievement>,
    isUserLoggedInToPlatform: Boolean,
    onConnectPlatformClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleAchievement: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        // Banner for connecting platform
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
            // Achievement counter
            val completedCount = remember(achievements) { achievements.count { it.isCompleted } }
            val progress = remember(achievements) {
                if (achievements.isNotEmpty()) completedCount.toFloat() / achievements.size else 0f
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trofei ottenuti: $completedCount / ${achievements.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Achievement list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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