package com.example.checkpoint.ui.sections.friends.components.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Friend
import com.example.checkpoint.ui.sections.friends.components.FriendAvatar

/**
 * Grid cell component displaying friend avatar, username, and level.
 */
@Composable
fun FriendGridItem(
    friend: Friend,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Friend avatar circle
        FriendAvatar(
            username = friend.username,
            avatarUrl = friend.avatarUrl,
            size = 68.dp,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            borderWidth = 2.dp,
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Username
        Text(
            text = friend.username,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        // Level indicator
        Text(
            text = "Lv. ${friend.level}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}