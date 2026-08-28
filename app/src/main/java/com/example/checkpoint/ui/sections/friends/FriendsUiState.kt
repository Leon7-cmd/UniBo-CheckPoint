package com.example.checkpoint.ui.sections.friends

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.Friend
import com.example.checkpoint.data.model.FriendRequest

/**
 * UI state representation for the Friends screen and social hub.
 */
@Immutable
data class FriendsUiState(
    val searchQuery: String = "",
    val filteredFriends: List<Friend> = emptyList(),
    val friendRequests: List<FriendRequest> = emptyList(),
    val isAddFriendDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoading: Boolean = false
)

/**
 * Internal state representation for the Add Friend dialog prompt.
 */
@Immutable
data class DialogState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)