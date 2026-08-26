package com.example.checkpoint.ui.sections.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.data.repository.LocalGameRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class LibraryViewModel(
    localGameRepository: LocalGameRepository
) : ViewModel() {

    private val _currentSection = MutableStateFlow<LibrarySectionType?>(null)
    private val _isLoading = MutableStateFlow(false)

    // Reactive library UI state aggregation computed off the main thread
    val uiState: StateFlow<LibraryUiState> = combine(
        localGameRepository.allGames,
        _currentSection,
        _isLoading
    ) { allGames, currentSection, isLoading ->
        val activeGames = ArrayList<Game>()
        val favorites = ArrayList<Game>()
        val completed = ArrayList<Game>()
        val inProgress = ArrayList<Game>()
        val toPlay = ArrayList<Game>()

        // Single-pass partitioning for library subsections
        for (game in allGames) {
            val isActive = game.isFavorite || game.status != GameStatus.NONE || game.rating > 0f
            if (isActive) {
                activeGames.add(game)
                if (game.isFavorite) favorites.add(game)
                when (game.status) {
                    GameStatus.COMPLETED -> completed.add(game)
                    GameStatus.NOT_COMPLETED -> inProgress.add(game)
                    GameStatus.TO_PLAY -> toPlay.add(game)
                    GameStatus.NONE -> { /* no-op */ }
                }
            }
        }

        val overviewData = LibraryOverviewData(
            favorites = favorites,
            completed = completed,
            inProgress = inProgress,
            toPlay = toPlay,
            all = activeGames
        )

        LibraryUiState(
            overview = overviewData,
            games = activeGames,
            currentSection = currentSection,
            isLoading = isLoading
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LibraryUiState(isLoading = true)
        )

    fun selectSection(section: LibrarySectionType?) {
        _currentSection.value = section
    }

    fun clearSelectedSection() {
        _currentSection.value = null
    }
}