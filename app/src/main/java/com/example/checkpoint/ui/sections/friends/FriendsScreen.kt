package com.example.checkpoint.ui.sections.friends

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.ui.sections.friends.components.main.AddFriendDialog
import com.example.checkpoint.ui.sections.friends.components.main.FriendGridItem

/**
 * Main social screen displaying friends grid, search bar, and actions for incoming requests and adding friends.
 */
@Composable
fun FriendsScreen(
    modifier: Modifier = Modifier,
    uiState: FriendsUiState,
    onSearchQueryChange: (String) -> Unit,
    onOpenAddFriend: () -> Unit,
    onOpenRequestsClick: () -> Unit,
    onCloseAddFriend: () -> Unit,
    onAddFriendConfirm: (String) -> Unit,
    onFriendClick: (String) -> Unit
) {
    // Add friend dialog prompt
    AddFriendDialog(
        isOpen = uiState.isAddFriendDialogOpen,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onDismiss = onCloseAddFriend,
        onConfirm = onAddFriendConfirm
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Amici",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenAddFriend,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "Aggiungi amico",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = onOpenRequestsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    BadgedBox(
                        badge = {
                            if (uiState.friendRequests.isNotEmpty()) {
                                Badge {
                                    Text(uiState.friendRequests.size.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Richieste di amicizia",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Search bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Cerca amico per nome o codice...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cerca",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancella testo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Friends 3-column grid or empty state
        if (uiState.filteredFriends.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (uiState.searchQuery.isBlank()) "Nessun amico aggiunto ancora." else "Nessun amico trovato.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(
                    items = uiState.filteredFriends,
                    key = { it.id },
                    contentType = { "friend_grid_item" }
                ) { friend ->
                    FriendGridItem(
                        friend = friend,
                        onClick = { onFriendClick(friend.id) }
                    )
                }
            }
        }
    }
}