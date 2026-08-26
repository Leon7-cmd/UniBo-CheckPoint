package com.example.checkpoint.ui.sections.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.AchievementSource
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.data.model.Review
import com.example.checkpoint.data.repository.AchievementRepository
import com.example.checkpoint.data.repository.IgdbRepository
import com.example.checkpoint.data.repository.LocalGameRepository
import com.example.checkpoint.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class GameDetailViewModel(
    private val igdbRepository: IgdbRepository,
    private val achievementRepository: AchievementRepository,
    private val localGameRepository: LocalGameRepository,
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    // Load game details from local database or remote IGDB API
    fun loadGameDetails(gameId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDetails = true, errorMessage = null) }
            loadCommunityReviews(gameId)

            val localGame = localGameRepository.getGameById(gameId).firstOrNull()

            if (localGame != null && localGame.description.isNotBlank()) {
                applyLoadedGame(localGame)
                loadAchievements(localGame)
                return@launch
            }

            igdbRepository.getGameById(gameId)
                .onSuccess { remoteGame ->
                    val mergedGame = localGame?.let { local ->
                        remoteGame.copy(
                            isFavorite = local.isFavorite,
                            status = local.status,
                            rating = local.rating,
                            userReview = local.userReview,
                            note = local.note,
                            steamAppId = local.steamAppId ?: remoteGame.steamAppId,
                            retroGameId = local.retroGameId ?: remoteGame.retroGameId
                        )
                    } ?: remoteGame

                    applyLoadedGame(mergedGame)

                    if (localGame != null) {
                        localGameRepository.saveGame(mergedGame)
                    }

                    loadAchievements(mergedGame)
                }
                .onFailure { error ->
                    if (localGame != null) {
                        applyLoadedGame(localGame)
                        loadAchievements(localGame)
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoadingDetails = false,
                                errorMessage = error.localizedMessage ?: "Impossibile caricare i dettagli del gioco."
                            )
                        }
                    }
                }
        }
    }

    private fun applyLoadedGame(game: Game) {
        val (avg, count) = computeRatingStats(game.rating, _uiState.value.communityReviews)
        val gameWithRating = if (game.communityRating != avg) game.copy(communityRating = avg) else game

        _uiState.update {
            it.copy(
                game = gameWithRating,
                averageRating = avg,
                totalReviewsCount = count,
                isLoadingDetails = false
            )
        }
    }

    // Fetch community reviews from Firestore
    private fun loadCommunityReviews(gameId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCommunityReviews = true) }
            val currentUid = auth.currentUser?.uid
            try {
                val snapshot = withContext(Dispatchers.IO) {
                    firestore.collection("games_reviews")
                        .document(gameId)
                        .collection("reviews")
                        .get()
                        .await()
                }

                val list = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: doc.id
                    if (uid == currentUid) return@mapNotNull null

                    Review(
                        userId = uid,
                        username = doc.getString("username") ?: "Player",
                        userAvatarFileName = doc.getString("userAvatarFileName"),
                        rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                        reviewText = doc.getString("reviewText") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }

                val currentLocalRating = _uiState.value.game?.rating ?: 0f
                val (avg, count) = computeRatingStats(currentLocalRating, list)

                _uiState.update {
                    it.copy(
                        communityReviews = list,
                        averageRating = avg,
                        totalReviewsCount = count,
                        isLoadingCommunityReviews = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingCommunityReviews = false) }
            }
        }
    }

    private fun computeRatingStats(userRating: Float, community: List<Review>): Pair<Float?, Int> {
        val allRatings = buildList {
            if (userRating > 0f) add(userRating)
            community.forEach { if (it.rating > 0f) add(it.rating) }
        }

        if (allRatings.isEmpty()) return null to 0

        val avg = (allRatings.average() * 10.0).roundToInt() / 10f
        return avg to allRatings.size
    }

    // Save user review and sync with Firestore
    fun saveReview(rating: Float, reviewText: String) {
        val currentGame = _uiState.value.game ?: return
        val (avg, count) = computeRatingStats(rating, _uiState.value.communityReviews)
        val updatedGame = currentGame.copy(rating = rating, userReview = reviewText, communityRating = avg)

        _uiState.update {
            it.copy(game = updatedGame, averageRating = avg, totalReviewsCount = count)
        }

        viewModelScope.launch {
            if (shouldKeepInLibrary(updatedGame, _uiState.value.achievements)) {
                localGameRepository.saveGame(updatedGame)
            }

            val user = auth.currentUser ?: return@launch
            val cachedProfile = userProfileRepository.getCachedUserProfile()
            val usernameToUse = cachedProfile.username.ifBlank {
                user.displayName?.ifBlank { null } ?: user.email?.substringBefore("@") ?: "Player"
            }

            val reviewData = hashMapOf(
                "userId" to user.uid,
                "username" to usernameToUse,
                "rating" to rating,
                "reviewText" to reviewText,
                "timestamp" to System.currentTimeMillis()
            )

            withContext(Dispatchers.IO) {
                runCatching {
                    firestore.collection("games_reviews")
                        .document(currentGame.id)
                        .collection("reviews")
                        .document(user.uid)
                        .set(reviewData, SetOptions.merge())
                        .await()
                }
            }
        }
    }

    // Delete user review and remove from Firestore
    fun deleteReview() {
        val currentGame = _uiState.value.game ?: return
        val (avg, count) = computeRatingStats(0f, _uiState.value.communityReviews)
        val updatedGame = currentGame.copy(rating = 0f, userReview = "", communityRating = avg)

        _uiState.update {
            it.copy(game = updatedGame, averageRating = avg, totalReviewsCount = count)
        }

        viewModelScope.launch {
            if (shouldKeepInLibrary(updatedGame, _uiState.value.achievements)) {
                localGameRepository.saveGame(updatedGame)
            } else {
                localGameRepository.removeGame(updatedGame.id)
            }

            val user = auth.currentUser ?: return@launch
            withContext(Dispatchers.IO) {
                runCatching {
                    firestore.collection("games_reviews")
                        .document(currentGame.id)
                        .collection("reviews")
                        .document(user.uid)
                        .delete()
                        .await()
                }
            }
        }
    }

    // Load game achievements and merge with unlocked status
    private fun loadAchievements(game: Game) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAchievements = true) }

            val unlockedCloudIds = localGameRepository.getCloudCompletedAchievementIds()
            val localEntities = localGameRepository.getLocalAchievements(game.id).firstOrNull() ?: emptyList()
            val unlockedLocalIds = localEntities.filter { it.isCompleted }.map { it.id }.toSet()
            val allUnlockedIds = unlockedCloudIds + unlockedLocalIds

            val hasFullLocalData = localEntities.isNotEmpty() && localEntities.none { it.title == "Obiettivo" }

            val finalAchievements = if (hasFullLocalData) {
                localEntities.map { ach -> ach.copy(isCompleted = allUnlockedIds.contains(ach.id)) }
            } else {
                val fetched = achievementRepository.getAchievements(
                    gameId = game.id,
                    gameTitle = game.title,
                    platforms = game.platforms,
                    steamAppId = game.steamAppId,
                    retroGameId = game.retroGameId
                )

                val baseAchievements = fetched.ifEmpty { createDefaultAchievements(game.id) }
                baseAchievements.map { ach -> ach.copy(isCompleted = allUnlockedIds.contains(ach.id)) }
            }

            _uiState.update {
                it.copy(achievements = finalAchievements, isLoadingAchievements = false)
            }

            val isSavedLocally = localGameRepository.getGameById(game.id).firstOrNull() != null
            if (isSavedLocally) {
                localGameRepository.saveAchievements(game.id, finalAchievements)
            }
        }
    }

    private fun createDefaultAchievements(gameId: String): List<Achievement> {
        return listOf(
            Achievement(
                id = "${gameId}_started",
                gameId = gameId,
                title = "Iniziato",
                description = "Hai iniziato a giocare a questo titolo.",
                iconUrl = "",
                isCompleted = false,
                source = AchievementSource.SYSTEM_DEFAULT
            ),
            Achievement(
                id = "${gameId}_completed",
                gameId = gameId,
                title = "Completato",
                description = "Hai portato a termine la storia o i contenuti principali.",
                iconUrl = "",
                isCompleted = false,
                source = AchievementSource.SYSTEM_DEFAULT
            )
        )
    }

    // Toggle achievement completion and adjust overall game progress status
    fun toggleAchievement(achievementId: String) {
        val currentGame = _uiState.value.game ?: return
        val currentAchievements = _uiState.value.achievements

        val target = currentAchievements.find { it.id == achievementId } ?: return
        val newStatus = !target.isCompleted

        val updatedList = currentAchievements.map { item ->
            if (item.id == achievementId) item.copy(isCompleted = newStatus) else item
        }

        val completedCount = updatedList.count { it.isCompleted }
        val totalCount = updatedList.size
        val isSystemDefault = updatedList.any { it.source == AchievementSource.SYSTEM_DEFAULT }

        val newGameStatus = when {
            isSystemDefault -> {
                val isCompletedAchUnlocked = updatedList.any { ach ->
                    (ach.id.endsWith("_completed") || ach.id.contains("completed", ignoreCase = true)) && ach.isCompleted
                } || (completedCount == totalCount && totalCount > 0)

                when {
                    isCompletedAchUnlocked -> GameStatus.COMPLETED
                    completedCount > 0 -> GameStatus.NOT_COMPLETED
                    else -> if (currentGame.status == GameStatus.TO_PLAY) GameStatus.TO_PLAY else GameStatus.NONE
                }
            }
            completedCount == totalCount && totalCount > 0 -> GameStatus.COMPLETED
            completedCount > 0 -> GameStatus.NOT_COMPLETED
            else -> if (currentGame.status == GameStatus.TO_PLAY) GameStatus.TO_PLAY else GameStatus.NONE
        }

        val updatedGame = currentGame.copy(status = newGameStatus)

        _uiState.update {
            it.copy(game = updatedGame, achievements = updatedList)
        }

        viewModelScope.launch {
            if (shouldKeepInLibrary(updatedGame, updatedList)) {
                localGameRepository.saveGame(updatedGame)
                localGameRepository.saveAchievements(currentGame.id, updatedList)
                localGameRepository.toggleAchievement(achievementId, currentGame.id, newStatus)
            } else {
                localGameRepository.removeGame(currentGame.id)
            }
        }
    }

    fun toggleFavorite() {
        updateAndPersistGame { it.copy(isFavorite = !it.isFavorite) }
    }

    fun toggleToPlay() {
        updateAndPersistGame { game ->
            val newStatus = if (game.status == GameStatus.TO_PLAY) GameStatus.NONE else GameStatus.TO_PLAY
            game.copy(status = newStatus)
        }
    }

    private fun updateAndPersistGame(transform: (Game) -> Game) {
        val currentGame = _uiState.value.game ?: return
        val updated = transform(currentGame)

        _uiState.update { it.copy(game = updated) }

        viewModelScope.launch {
            val achievements = _uiState.value.achievements
            if (shouldKeepInLibrary(updated, achievements)) {
                localGameRepository.saveGame(updated)
                if (achievements.isNotEmpty()) {
                    localGameRepository.saveAchievements(updated.id, achievements)
                }
            } else {
                localGameRepository.removeGame(updated.id)
            }
        }
    }

    private fun shouldKeepInLibrary(game: Game, currentAchievements: List<Achievement>): Boolean {
        return game.status != GameStatus.NONE ||
                game.isFavorite ||
                currentAchievements.any { it.isCompleted } ||
                game.rating > 0f ||
                game.userReview.isNotBlank() ||
                game.note.isNotBlank()
    }
}