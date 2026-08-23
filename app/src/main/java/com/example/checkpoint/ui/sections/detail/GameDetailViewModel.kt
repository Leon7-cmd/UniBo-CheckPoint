package com.example.checkpoint.ui.sections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.AchievementSource
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.data.repository.AchievementRepository
import com.example.checkpoint.data.repository.IgdbRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameDetailViewModel(
    private val igdbRepository: IgdbRepository,
    private val achievementRepository: AchievementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _isLoadingAchievements = MutableStateFlow(false)
    val isLoadingAchievements: StateFlow<Boolean> = _isLoadingAchievements.asStateFlow()

    fun loadGameDetails(gameId: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            igdbRepository.getGameById(gameId)
                .onSuccess { game ->
                    _uiState.value = DetailUiState.Success(game)
                    loadAchievements(
                        game = game,
                        steamAppId = game.steamAppId,
                        retroGameId = game.retroGameId
                    )
                }
                .onFailure { error ->
                    _uiState.value = DetailUiState.Error(
                        error.localizedMessage ?: "Impossibile caricare i dettagli del gioco."
                    )
                }
        }
    }

    fun loadAchievements(game: Game, steamAppId: String? = null, retroGameId: String? = null) {
        viewModelScope.launch {
            _isLoadingAchievements.value = true
            val effectiveSteamId = steamAppId ?: game.steamAppId
            val effectiveRetroId = retroGameId ?: game.retroGameId

            val results = achievementRepository.getAchievements(
                gameId = game.id,
                gameTitle = game.title,
                platforms = game.platforms,
                steamAppId = effectiveSteamId,
                retroGameId = effectiveRetroId
            )
            _achievements.value = results
            _isLoadingAchievements.value = false
        }
    }

    fun toggleAchievement(achievementId: String) {
        _achievements.update { currentList ->
            currentList.map { item ->
                if (item.id == achievementId && item.source == AchievementSource.SYSTEM_DEFAULT) {
                    item.copy(isCompleted = !item.isCompleted)
                } else {
                    item
                }
            }
        }
    }

    fun toggleFavorite() {
        updateGameSuccess { it.copy(isFavorite = !it.isFavorite) }
    }

    fun toggleToPlay() {
        updateGameSuccess { game ->
            val newStatus = if (game.status == GameStatus.TO_PLAY) {
                GameStatus.NONE
            } else {
                GameStatus.TO_PLAY
            }
            game.copy(status = newStatus)
        }
    }

    fun updateRating(newRating: Float) {
        updateGameSuccess { it.copy(rating = newRating) }
    }

    // Helper function to update the game state with a transformation
    private inline fun updateGameSuccess(transform: (Game) -> Game) {
        val currentState = _uiState.value
        if (currentState is DetailUiState.Success) {
            _uiState.value = DetailUiState.Success(transform(currentState.game))
        }
    }
}