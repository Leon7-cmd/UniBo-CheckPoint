package com.example.checkpoint.data.repository

import com.example.checkpoint.data.model.Friend
import com.example.checkpoint.data.model.FriendRequest
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.data.model.PrivacyLevel
import com.example.checkpoint.data.model.UserStats
import com.example.checkpoint.domain.BadgeCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Repository managing friends lists, friendship requests, and remote profiles synchronization via Firestore.
 */
class FriendsRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val currentUid: String? get() = auth.currentUser?.uid

    // Real-time flow of friends list synchronized with their respective user documents
    fun getFriendsFlow(): Flow<List<Friend>> = callbackFlow {
        val uid = currentUid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var fetchJob: Job? = null

        val listener = firestore.collection("users")
            .document(uid)
            .collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val friendIds = snapshot?.documents?.map { it.id }.orEmpty()
                if (friendIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Fetch full user profiles in chunks to retrieve fresh usernames, avatars, and levels
                fetchJob?.cancel()
                fetchJob = launch {
                    try {
                        val friendsList = mutableListOf<Friend>()

                        // Firestore whereIn supports up to 30 elements per query
                        friendIds.chunked(30).forEach { chunk ->
                            val usersSnapshot = firestore.collection("users")
                                .whereIn(FieldPath.documentId(), chunk)
                                .get()
                                .await()

                            usersSnapshot.documents.forEach { doc ->
                                friendsList.add(
                                    Friend(
                                        id = doc.id,
                                        username = doc.getString("username") ?: "Player",
                                        friendCode = doc.getString("friendCode").orEmpty(),
                                        avatarUrl = doc.getString("avatarFileName") ?: doc.getString("avatarUrl"),
                                        level = doc.getLong("level")?.toInt() ?: 1,
                                        currentXp = doc.getLong("currentXp")?.toInt() ?: 0,
                                        nextLevelXp = doc.getLong("nextLevelXp")?.toInt() ?: BadgeCalculator.XP_PER_LEVEL,
                                        stats = doc.extractUserStats(),
                                        badges = emptyList(),
                                        games = emptyList(),
                                        statsPrivacy = parsePrivacy(doc.getString("statsPrivacy")),
                                        badgesPrivacy = parsePrivacy(doc.getString("badgesPrivacy")),
                                        libraryPrivacy = parsePrivacy(doc.getString("libraryPrivacy"))
                                    )
                                )
                            }
                        }
                        trySend(friendsList)
                    } catch (_: Exception) {
                        trySend(emptyList())
                    }
                }
            }

        awaitClose {
            listener.remove()
            fetchJob?.cancel()
        }
    }

    // Real-time flow of incoming friend requests
    fun getFriendRequestsFlow(): Flow<List<FriendRequest>> = callbackFlow {
        val uid = currentUid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("friend_requests")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val requests = snapshot?.documents?.mapNotNull { doc ->
                    FriendRequest(
                        senderUid = doc.id,
                        senderUsername = doc.getString("senderUsername") ?: "Player",
                        senderAvatarUrl = doc.getString("senderAvatarUrl"),
                        senderLevel = doc.getLong("senderLevel")?.toInt() ?: 1,
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }.orEmpty()
                trySend(requests)
            }

        awaitClose { listener.remove() }
    }

    // Send a new friend request using a unique friend code
    suspend fun sendFriendRequestByCode(friendCode: String): Result<Unit> = runCatching {
        val myUid = currentUid ?: throw IllegalStateException("Utente non autenticato")

        val querySnapshot = firestore.collection("users")
            .whereEqualTo("friendCode", friendCode.trim().uppercase())
            .get()
            .await()

        if (querySnapshot.isEmpty) {
            throw NoSuchElementException("Nessun utente trovato con questo codice!")
        }

        val targetUid = querySnapshot.documents.first().id
        if (targetUid == myUid) {
            throw IllegalArgumentException("Non puoi inviare una richiesta a te stesso!")
        }

        val myProfileDoc = firestore.collection("users").document(myUid).get().await()
        val myUsername = myProfileDoc.getString("username") ?: "Player"
        val myAvatar = myProfileDoc.getString("avatarFileName") ?: myProfileDoc.getString("avatarUrl")
        val myLevel = myProfileDoc.getLong("level")?.toInt() ?: 1

        val requestData = mapOf(
            "senderUid" to myUid,
            "senderUsername" to myUsername,
            "senderAvatarUrl" to myAvatar,
            "senderLevel" to myLevel,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(targetUid)
            .collection("friend_requests")
            .document(myUid)
            .set(requestData)
            .await()
    }

    // Accept an incoming friend request and establish mutual connection
    suspend fun acceptFriendRequest(request: FriendRequest): Result<Unit> = runCatching {
        val myUid = currentUid ?: throw IllegalStateException("Utente non autenticato")
        val batch = firestore.batch()

        // 1. Add friend to current user's friends collection
        val myFriendRef = firestore.collection("users").document(myUid).collection("friends").document(request.senderUid)
        batch.set(myFriendRef, mapOf("addedAt" to System.currentTimeMillis()))

        // 2. Add current user to friend's friends collection
        val otherFriendRef = firestore.collection("users").document(request.senderUid).collection("friends").document(myUid)
        batch.set(otherFriendRef, mapOf("addedAt" to System.currentTimeMillis()))

        // 3. Remove pending request
        val requestRef = firestore.collection("users").document(myUid).collection("friend_requests").document(request.senderUid)
        batch.delete(requestRef)

        batch.commit().await()
    }

    // Reject and delete an incoming friend request
    suspend fun rejectFriendRequest(senderUid: String): Result<Unit> = runCatching {
        val myUid = currentUid ?: throw IllegalStateException("Utente non autenticato")
        firestore.collection("users")
            .document(myUid)
            .collection("friend_requests")
            .document(senderUid)
            .delete()
            .await()
    }

    // Mutual friend removal
    suspend fun removeFriend(friendUid: String): Result<Unit> = runCatching {
        val myUid = currentUid ?: throw IllegalStateException("Utente non autenticato")
        val batch = firestore.batch()

        val mySideRef = firestore.collection("users").document(myUid).collection("friends").document(friendUid)
        val otherSideRef = firestore.collection("users").document(friendUid).collection("friends").document(myUid)

        batch.delete(mySideRef)
        batch.delete(otherSideRef)

        batch.commit().await()
    }

    // Fetch complete friend profile including library and unlocked trophies
    suspend fun getFriendById(friendId: String): Friend? = runCatching {
        val userDoc = firestore.collection("users").document(friendId).get().await()
        if (!userDoc.exists()) return null

        val librarySnapshot = firestore.collection("users").document(friendId).collection("library").get().await()
        val allGames = librarySnapshot.documents.map { doc ->
            Game(
                id = doc.id,
                title = doc.getString("title").orEmpty(),
                coverUrl = doc.getString("coverUrl").orEmpty(),
                rating = doc.getDouble("rating")?.toFloat() ?: 0f,
                isFavorite = doc.getBoolean("isFavorite") ?: false,
                status = doc.getString("status")?.let { runCatching { GameStatus.valueOf(it) }.getOrNull() } ?: GameStatus.NONE
            )
        }

        val achievementsSnapshot = runCatching {
            firestore.collection("users").document(friendId).collection("achievements").whereEqualTo("isCompleted", true).get().await()
        }.getOrNull()
        val actualTotalAchievements = achievementsSnapshot?.size() ?: 0

        mapDocToFriend(userDoc, allGames, actualTotalAchievements)
    }.getOrNull()

    private fun mapDocToFriend(
        doc: DocumentSnapshot,
        games: List<Game> = emptyList(),
        computedAchievementsCount: Int = 0
    ): Friend {
        val progression = BadgeCalculator.calculateProgression(games, computedAchievementsCount)

        return Friend(
            id = doc.id,
            username = doc.getString("username") ?: "Player",
            friendCode = doc.getString("friendCode").orEmpty(),
            avatarUrl = doc.getString("avatarFileName") ?: doc.getString("avatarUrl"),
            level = doc.getLong("level")?.toInt() ?: progression.level,
            currentXp = doc.getLong("currentXp")?.toInt() ?: progression.currentXp,
            nextLevelXp = doc.getLong("nextLevelXp")?.toInt() ?: progression.nextLevelXp,
            stats = progression.stats,
            badges = progression.badges,
            games = games,
            statsPrivacy = parsePrivacy(doc.getString("statsPrivacy")),
            badgesPrivacy = parsePrivacy(doc.getString("badgesPrivacy")),
            libraryPrivacy = parsePrivacy(doc.getString("libraryPrivacy"))
        )
    }

    private fun DocumentSnapshot.extractUserStats(): UserStats {
        val statsMap = get("stats") as? Map<*, *>
        return UserStats(
            gamesPlayed = (statsMap?.get("gamesPlayed") as? Number)?.toInt() ?: getLong("gamesPlayed")?.toInt() ?: 0,
            gamesCompleted = (statsMap?.get("gamesCompleted") as? Number)?.toInt() ?: getLong("gamesCompleted")?.toInt() ?: 0,
            gamesReviewed = (statsMap?.get("gamesReviewed") as? Number)?.toInt() ?: getLong("gamesReviewed")?.toInt() ?: 0,
            totalAchievements = (statsMap?.get("totalAchievements") as? Number)?.toInt() ?: getLong("totalAchievements")?.toInt() ?: 0
        )
    }

    private fun parsePrivacy(value: String?): PrivacyLevel =
        value?.let { runCatching { PrivacyLevel.valueOf(it) }.getOrNull() } ?: PrivacyLevel.PUBLIC
}