package com.example.checkpoint.ui.sections.friends.components.request

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.FriendRequest
import com.example.checkpoint.ui.sections.friends.components.FriendAvatar

/**
 * Compact list item card for incoming friend requests with accept and reject buttons.
 */
@Composable
fun FriendRequestItemCard(
    request: FriendRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            FriendAvatar(
                username = request.senderUsername,
                avatarUrl = request.senderAvatarUrl,
                size = 44.dp,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Sender information
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = request.senderUsername,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Livello ${request.senderLevel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Accept and reject action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledIconButton(
                    onClick = onAccept,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Accetta richiesta",
                        modifier = Modifier.size(16.dp)
                    )
                }

                FilledIconButton(
                    onClick = onReject,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFFC62828),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Rifiuta richiesta",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}