package com.example.checkpoint.ui.sections.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.model.FriendRequest
import com.example.checkpoint.data.repository.FriendsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FriendsViewModel(
    private val friendsRepository: FriendsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _dialogState = MutableStateFlow(DialogState())

    // Combine search query, friends list, pending requests, and dialog state
    val uiState: StateFlow<FriendsUiState> = combine(
        _searchQuery,
        friendsRepository.getFriendsFlow(),
        friendsRepository.getFriendRequestsFlow(),
        _dialogState
    ) { query, friends, requests, dialog ->
        val filtered = if (query.isBlank()) {
            friends
        } else {
            val clean = query.trim()
            friends.filter {
                it.username.contains(clean, ignoreCase = true) ||
                        it.friendCode.contains(clean, ignoreCase = true)
            }
        }

        FriendsUiState(
            searchQuery = query,
            filteredFriends = filtered,
            friendRequests = requests,
            isAddFriendDialogOpen = dialog.isOpen,
            isLoading = dialog.isLoading,
            errorMessage = dialog.errorMessage,
            successMessage = dialog.successMessage
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FriendsUiState()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun openAddFriendDialog() {
        _dialogState.update {
            it.copy(isOpen = true, errorMessage = null, successMessage = null)
        }
    }

    fun closeAddFriendDialog() {
        _dialogState.update {
            it.copy(isOpen = false, errorMessage = null)
        }
    }

    // Send friend request by friend code
    fun addFriend(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode.isBlank()) {
            _dialogState.update { it.copy(errorMessage = "Inserisci un codice valido.") }
            return
        }

        viewModelScope.launch {
            _dialogState.update { it.copy(isLoading = true, errorMessage = null) }
            friendsRepository.sendFriendRequestByCode(trimmedCode)
                .onSuccess {
                    _dialogState.update {
                        it.copy(
                            isLoading = false,
                            isOpen = false,
                            successMessage = "Richiesta inviata con successo!"
                        )
                    }
                }
                .onFailure { error ->
                    _dialogState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.localizedMessage ?: "Errore durante l'invio della richiesta."
                        )
                    }
                }
        }
    }

    fun acceptRequest(request: FriendRequest) {
        viewModelScope.launch {
            friendsRepository.acceptFriendRequest(request)
        }
    }

    fun rejectRequest(senderUid: String) {
        viewModelScope.launch {
            friendsRepository.rejectFriendRequest(senderUid)
        }
    }

    fun removeFriend(friendId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            friendsRepository.removeFriend(friendId)
                .onSuccess { onComplete() }
        }
    }
}