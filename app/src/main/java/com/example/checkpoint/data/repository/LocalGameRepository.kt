package com.example.checkpoint.data.repository

import com.example.checkpoint.data.entity.GameEntity
import com.example.checkpoint.data.entity.toDomain
import com.example.checkpoint.data.entity.toEntity
import com.example.checkpoint.data.local.dao.AchievementDao
import com.example.checkpoint.data.local.dao.GameDao
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository orchestrating local Room persistence with background Firestore cloud synchronizations.
 */
class LocalGameRepository(
    private val gameDao: GameDao,
    private val achievementDao: AchievementDao,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    // SupervisorJob CoroutineScope for resilient background Firestore operations
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val currentUid: String? get() = auth.currentUser?.uid

    // Reactive streams from Room database
    val allGames: Flow<List<Game>> = gameDao.getAllGames().map { list ->
        list.map { it.toDomain() }
    }

    val totalCompletedAchievementsCount: Flow<Int> = achievementDao.getTotalCompletedAchievementsCount()

    fun getGameById(id: String): Flow<Game?> = gameDao.getGameById(id).map { it?.toDomain() }

    // Achievements local data access
    fun getLocalAchievements(gameId: String): Flow<List<Achievement>> =
        achievementDao.getAchievementsByGameId(gameId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveAchievements(gameId: String, achievements: List<Achievement>) = withContext(Dispatchers.IO) {
        val entities = achievements.map { it.toEntity(fallbackGameId = gameId) }
        achievementDao.insertOrUpdateAchievements(entities)
    }

    // Bidirectional synchronization between local Room and remote Firestore library
    suspend fun syncWithCloud() = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext
        try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("library")
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val cloudGames = snapshot.documents.mapNotNull { documentToGameEntity(it) }
                gameDao.insertAll(cloudGames)
            } else {
                val localGames = gameDao.getAllGamesList()
                if (localGames.isNotEmpty()) {
                    val batch = firestore.batch()
                    localGames.forEach { entity ->
                        val docRef = firestore.collection("users")
                            .document(uid)
                            .collection("library")
                            .document(entity.id)
                        batch.set(docRef, entityToMap(entity), SetOptions.merge())
                    }
                    batch.commit().await()
                }
            }
        } catch (_: Exception) {
            // Suppress sync failure during offline state
        }
    }

    // Persist locally and dispatch background update to Firestore
    suspend fun saveGame(game: Game) = withContext(Dispatchers.IO) {
        val entity = game.toEntity()
        gameDao.insertGame(entity)

        val uid = currentUid ?: return@withContext
        repositoryScope.launch {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("library")
                    .document(game.id)
                    .set(entityToMap(entity), SetOptions.merge())
                    .await()
            } catch (_: Exception) {
                // Suppress background sync errors
            }
        }
    }

    suspend fun removeGame(gameId: String) = deleteGame(gameId)

    suspend fun deleteGame(gameId: String) = withContext(Dispatchers.IO) {
        gameDao.deleteGameById(gameId)

        val uid = currentUid ?: return@withContext
        repositoryScope.launch {
            try {
                firestore.collection("users")
                    .document(uid)
                    .collection("library")
                    .document(gameId)
                    .delete()
                    .await()
            } catch (_: Exception) {
                // Suppress background sync errors
            }
        }
    }

    // Synchronize completed achievement IDs from Firestore
    suspend fun syncAchievementsWithCloud() = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext
        try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("achievements")
                .whereEqualTo("isCompleted", true)
                .get()
                .await()

            val unlockedIds = snapshot.documents.mapNotNull { it.getString("achievementId") ?: it.id }
            if (unlockedIds.isNotEmpty()) {
                achievementDao.markAchievementsCompleted(unlockedIds)
            }
        } catch (_: Exception) {
            // Suppress sync failure during offline state
        }
    }

    suspend fun toggleAchievement(achievementId: String, gameId: String, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        achievementDao.updateAchievementStatus(achievementId, isCompleted)

        val uid = currentUid ?: return@withContext
        repositoryScope.launch {
            try {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("achievements")
                    .document(achievementId)

                if (isCompleted) {
                    val data = mapOf(
                        "achievementId" to achievementId,
                        "gameId" to gameId,
                        "isCompleted" to true,
                        "unlockedAt" to System.currentTimeMillis()
                    )
                    docRef.set(data, SetOptions.merge()).await()
                } else {
                    docRef.delete().await()
                }
            } catch (_: Exception) {
                // Suppress background sync errors
            }
        }
    }

    suspend fun getCloudCompletedAchievementIds(): Set<String> = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext emptySet()
        try {
            val snapshot = firestore.collection("users")
                .document(uid)
                .collection("achievements")
                .whereEqualTo("isCompleted", true)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.getString("achievementId") ?: it.id }.toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    suspend fun clearLocalData() = withContext(Dispatchers.IO) {
        gameDao.clearAllGames()
        achievementDao.clearAllAchievements()
    }

    // Mapping helpers: Firestore <-> Entity
    private fun documentToGameEntity(doc: DocumentSnapshot): GameEntity? {
        val id = doc.getString("id") ?: doc.id
        val title = doc.getString("title") ?: return null

        val platformsList = (doc.get("platforms") as? List<*>)?.filterIsInstance<String>().orEmpty()
        val tagsList = (doc.get("tags") as? List<*>)?.filterIsInstance<String>().orEmpty()

        return GameEntity(
            id = id,
            title = title,
            coverUrl = doc.getString("coverUrl").orEmpty(),
            releaseDate = doc.getString("releaseDate").orEmpty(),
            developer = doc.getString("developer").orEmpty(),
            publisher = doc.getString("publisher").orEmpty(),
            description = doc.getString("description").orEmpty(),
            isFavorite = doc.getBoolean("isFavorite") ?: false,
            status = doc.getString("status")?.let { statusStr ->
                runCatching { GameStatus.valueOf(statusStr) }.getOrDefault(GameStatus.NONE)
            } ?: GameStatus.NONE,
            rating = (doc.getDouble("rating") ?: 0.0).toFloat(),
            communityRating = doc.getDouble("communityRating")?.toFloat(),
            userReview = doc.getString("userReview").orEmpty(),
            note = doc.getString("note").orEmpty(),
            platforms = platformsList,
            tags = tagsList,
            steamAppId = doc.getString("steamAppId"),
            retroGameId = doc.getString("retroGameId")
        )
    }

    private fun entityToMap(entity: GameEntity): Map<String, Any?> = mapOf(
        "id" to entity.id,
        "title" to entity.title,
        "coverUrl" to entity.coverUrl,
        "releaseDate" to entity.releaseDate,
        "developer" to entity.developer,
        "publisher" to entity.publisher,
        "description" to entity.description,
        "platforms" to entity.platforms,
        "tags" to entity.tags,
        "isFavorite" to entity.isFavorite,
        "status" to entity.status.name,
        "rating" to entity.rating.toDouble(),
        "communityRating" to entity.communityRating?.toDouble(),
        "userReview" to entity.userReview,
        "note" to entity.note,
        "steamAppId" to entity.steamAppId,
        "retroGameId" to entity.retroGameId,
        "updatedAt" to System.currentTimeMillis()
    )
}