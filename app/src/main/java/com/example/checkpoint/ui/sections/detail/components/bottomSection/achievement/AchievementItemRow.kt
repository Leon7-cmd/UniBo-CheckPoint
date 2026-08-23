package com.example.checkpoint.ui.sections.detail.components.bottomSection.achievement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.checkpoint.data.model.Achievement

@Composable
fun AchievementItemRow(
    achievement: Achievement,
    isReadOnly: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = achievement.isCompleted
    val cardColors = CardDefaults.cardColors(
        containerColor = if (isDone)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceContainerLow
    )

    val cardContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon
            if (achievement.iconUrl.isNotEmpty()) {
                AsyncImage(
                    model = achievement.iconUrl,
                    contentDescription = achievement.title,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title and Description
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (achievement.description.isNotEmpty()) {
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Checkbox or Lock Icon
            if (isReadOnly) {
                if (isDone) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sbloccato",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            } else {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }

    if (isReadOnly) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (isDone) 1f else 0.55f),
            colors = cardColors
        ) {
            cardContent()
        }
    } else {
        Card(
            onClick = onToggle,
            modifier = modifier
                .fillMaxWidth()
                .alpha(if (isDone) 1f else 0.55f),
            colors = cardColors
        ) {
            cardContent()
        }
    }
}