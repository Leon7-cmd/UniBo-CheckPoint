package com.example.checkpoint.ui.sections.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.repository.LocalGameRepository
import com.example.checkpoint.data.repository.UserProfileRepository
import com.example.checkpoint.domain.BadgeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val localGameRepository: LocalGameRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCachedProfile()
        observeStatsAndBadges()
        syncProfile()
    }

    // Load local cached profile data immediately
    private fun loadCachedProfile() {
        val cached = userProfileRepository.getCachedUserProfile()
        _uiState.update {
            it.copy(
                username = cached.username,
                email = cached.email,
                friendCode = cached.friendCode,
                avatarUrl = cached.avatarUri
            )
        }
    }

    // Fetch and sync user profile from cloud
    private fun syncProfile() {
        viewModelScope.launch {
            val synced = userProfileRepository.syncUserProfile() ?: return@launch
            _uiState.update {
                it.copy(
                    username = synced.username,
                    email = synced.email,
                    friendCode = synced.friendCode,
                    avatarUrl = synced.avatarUri
                )
            }
        }
    }

    // Update avatar image URI locally and sync
    fun onAvatarSelected(uri: Uri?) {
        if (uri == null) return

        viewModelScope.launch {
            val savedPathOrUrl = userProfileRepository.updateAvatarUri(uri) ?: return@launch
            _uiState.update { it.copy(avatarUrl = savedPathOrUrl) }

            val current = _uiState.value
            userProfileRepository.syncProfileStatsAndBadges(
                level = current.level,
                currentXp = current.currentXp,
                nextLevelXp = current.nextLevelXp,
                stats = current.stats,
                badges = current.badges
            )
        }
    }

    // Reactively compute level progression, badges, and stats off the main thread
    private fun observeStatsAndBadges() {
        combine(
            localGameRepository.allGames,
            localGameRepository.totalCompletedAchievementsCount
        ) { games, completedAchievementsCount ->
            BadgeCalculator.calculateProgression(games, completedAchievementsCount)
        }
            .flowOn(Dispatchers.Default)
            .distinctUntilChanged()
            .onEach { progression ->
                _uiState.update { current ->
                    current.copy(
                        level = progression.level,
                        currentXp = progression.currentXp,
                        nextLevelXp = progression.nextLevelXp,
                        badges = progression.badges,
                        stats = progression.stats
                    )
                }

                // Background sync for updated progression
                userProfileRepository.syncProfileStatsAndBadges(
                    level = progression.level,
                    currentXp = progression.currentXp,
                    nextLevelXp = progression.nextLevelXp,
                    stats = progression.stats,
                    badges = progression.badges
                )
            }
            .launchIn(viewModelScope)
    }
}