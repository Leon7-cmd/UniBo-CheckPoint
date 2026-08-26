package com.example.checkpoint.ui.sections.detail.components.bottomSection.achievement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.example.checkpoint.data.model.Achievement

/**
 * Individual achievement list item row featuring trophy icon, title, description, and completion toggle.
 */
@Composable
fun AchievementItemRow(
    achievement: Achievement,
    isReadOnly: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDone = achievement.isCompleted

    // Constrain Coil decoding bitmap size to avoid memory overhead
    val imageRequest = remember(achievement.iconUrl) {
        if (achievement.iconUrl.isNotBlank()) {
            ImageRequest.Builder(context)
                .data(achievement.iconUrl)
                .size(100, 100)
                .scale(Scale.FILL)
                .build()
        } else {
            null
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = if (isDone) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }
    )

    Card(
        onClick = onToggle,
        enabled = !isReadOnly,
        shape = RoundedCornerShape(10.dp),
        colors = cardColors,
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDone) 1f else 0.55f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Trophy badge icon or fallback status icon
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = achievement.title,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
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

            // Title and description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
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

            // Status indicator or interactive completion checkbox
            if (isReadOnly) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.Lock,
                    contentDescription = if (isDone) "Sbloccato" else "Bloccato",
                    tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Checkbox(
                    checked = isDone,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    }
}