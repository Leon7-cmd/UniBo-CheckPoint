package com.example.checkpoint.ui.sections.friends.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.Friend
import com.example.checkpoint.data.model.PrivacyLevel
import com.example.checkpoint.ui.components.GameCard
import com.example.checkpoint.ui.sections.profile.components.StatsSummaryCard
import com.example.checkpoint.ui.sections.profile.components.badge.BadgesSection

/**
 * Detailed profile view for a friend, respecting individual privacy settings for stats, badges, and library.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDetailScreen(
    friend: Friend,
    onBackClick: () -> Unit,
    onRemoveFriend: (String) -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(friend.username, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Torna indietro"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = "Rimuovi amico",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Level and avatar card
            FriendDetailHeaderCard(friend = friend)

            // Stats summary (if not private)
            if (friend.statsPrivacy != PrivacyLevel.PRIVATE) {
                StatsSummaryCard(stats = friend.stats)
            }

            // Badges showcase (if not private)
            if (friend.badgesPrivacy != PrivacyLevel.PRIVATE && friend.badges.isNotEmpty()) {
                BadgesSection(
                    badges = friend.badges,
                    onBadgeClick = { }
                )
            }

            // Games library preview (if not private)
            if (friend.libraryPrivacy != PrivacyLevel.PRIVATE && friend.games.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "LIBRERIA (${friend.games.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = friend.games,
                                key = { it.id },
                                contentType = { "friend_game_card" }
                            ) { game ->
                                GameCard(
                                    game = game,
                                    onClick = { onGameClick(game.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Confirmation dialog for removing friend
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Rimuovi amico") },
                text = { Text("Vuoi davvero rimuovere ${friend.username} dalla tua lista amici?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onRemoveFriend(friend.id)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Rimuovi", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { }) {
                        Text("Annulla")
                    }
                }
            )
        }
    }
}