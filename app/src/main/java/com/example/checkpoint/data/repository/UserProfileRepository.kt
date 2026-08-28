package com.example.checkpoint.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.core.content.edit
import com.example.checkpoint.data.model.ProfileBadge
import com.example.checkpoint.data.model.UserStats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Immutable representation of the cached and synchronized user profile information.
 */
@Immutable
data class UserProfileData(
    val username: String,
    val email: String,
    val friendCode: String,
    val avatarUri: String?
)

/**
 * Repository responsible for user profile management, avatar file caching, and remote Firestore synchronization.
 */
class UserProfileRepository(
    private val context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val currentUid: String?
        get() = auth.currentUser?.uid

    // Local internal storage directory dedicated to avatar files
    private val avatarsDir: File
        get() = File(context.filesDir, "avatars").apply { if (!exists()) mkdirs() }

    // Fetch and sync user profile from Firestore into local cache
    suspend fun syncUserProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext null
        val uid = user.uid
        val userDocRef = firestore.collection("users").document(uid)

        try {
            val snapshot = userDocRef.get().await()

            if (snapshot.exists()) {
                val savedFriendCode = snapshot.getString("friendCode") ?: generateFriendCode(uid)
                val savedUsername = snapshot.getString("username")
                    ?: user.displayName?.ifBlank { null }
                    ?: user.email?.substringBefore("@")
                    ?: "Player"

                val avatarFileName = snapshot.getString("avatarFileName")
                val localFile = avatarFileName?.let { File(avatarsDir, it) }
                val validAvatarPath = if (localFile?.exists() == true) localFile.absolutePath else null

                prefs.edit {
                    putString(keyUsername(uid), savedUsername)
                    putString(keyFriendCode(uid), savedFriendCode)
                    putString(keyAvatar(uid), validAvatarPath)
                }

                UserProfileData(
                    username = savedUsername,
                    email = user.email.orEmpty(),
                    friendCode = savedFriendCode,
                    avatarUri = validAvatarPath
                )
            } else {
                createInitialUserProfile(
                    uid = uid,
                    email = user.email.orEmpty(),
                    username = user.displayName.orEmpty()
                )
                getCachedUserProfile()
            }
        } catch (_: Exception) {
            getCachedUserProfile()
        }
    }

    // Sync gamification statistics, XP, level, and earned badges to Firestore
    suspend fun syncProfileStatsAndBadges(
        level: Int,
        currentXp: Int,
        nextLevelXp: Int,
        stats: UserStats,
        badges: List<ProfileBadge> = emptyList()
    ) = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext

        val updateData = mapOf(
            "level" to level,
            "currentXp" to currentXp,
            "nextLevelXp" to nextLevelXp,
            "gamesPlayed" to stats.gamesPlayed,
            "gamesCompleted" to stats.gamesCompleted,
            "gamesReviewed" to stats.gamesReviewed,
            "totalAchievements" to stats.totalAchievements,
            "badges" to badges.map { it.id },
            "updatedAt" to System.currentTimeMillis()
        )

        try {
            firestore.collection("users")
                .document(uid)
                .set(updateData, SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Silently fail or log in production
        }
    }

    // Persist new avatar image locally and associate its file name on Firestore
    suspend fun updateAvatarUri(uri: Uri?): String? = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext null
        if (uri == null) return@withContext null

        try {
            val fileName = "avatar_$uid.jpg"
            val destFile = File(avatarsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }

            val absolutePath = destFile.absolutePath

            // 1. Save file reference in Firestore
            firestore.collection("users")
                .document(uid)
                .set(mapOf("avatarFileName" to fileName), SetOptions.merge())
                .await()

            // 2. Persist local absolute path in SharedPreferences
            prefs.edit {
                putString(keyAvatar(uid), absolutePath)
            }

            absolutePath
        } catch (_: Exception) {
            null
        }
    }

    // Synchronous read from local cache
    fun getCachedUserProfile(): UserProfileData {
        val user = auth.currentUser
        val uid = user?.uid.orEmpty()

        val savedPath = prefs.getString(keyAvatar(uid), null)
        val finalAvatar = if (savedPath != null && File(savedPath).exists()) {
            savedPath
        } else {
            val fallbackFile = File(avatarsDir, "avatar_$uid.jpg")
            if (fallbackFile.exists()) fallbackFile.absolutePath else null
        }

        return UserProfileData(
            username = prefs.getString(keyUsername(uid), user?.displayName ?: "Player") ?: "Player",
            email = user?.email.orEmpty(),
            friendCode = prefs.getString(keyFriendCode(uid), "CKP-000000") ?: "CKP-000000",
            avatarUri = finalAvatar
        )
    }

    private fun generateFriendCode(uid: String): String {
        val clean = uid.filter { it.isLetterOrDigit() }.uppercase()
        return "CKP-" + clean.take(6).padEnd(6, 'X')
    }

    // Initialize initial user document upon registration
    suspend fun createInitialUserProfile(
        uid: String,
        email: String,
        username: String
    ) = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().ifBlank { email.substringBefore("@") }
        val friendCode = generateFriendCode(uid)

        val initialUserData = mapOf(
            "uid" to uid,
            "email" to email.trim(),
            "username" to cleanUsername,
            "friendCode" to friendCode,
            "avatarFileName" to null,
            "level" to 1,
            "currentXp" to 0,
            "nextLevelXp" to 500,
            "gamesPlayed" to 0,
            "gamesCompleted" to 0,
            "gamesReviewed" to 0,
            "totalAchievements" to 0,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .set(initialUserData, SetOptions.merge())
            .await()

        prefs.edit {
            putString(keyUsername(uid), cleanUsername)
            putString(keyFriendCode(uid), friendCode)
            putString(keyAvatar(uid), null)
        }
    }

    // Clear local database, preferences, and terminate Firebase session
    suspend fun logoutAndClear(
        localGameRepository: LocalGameRepository,
        settingsRepository: SettingsRepository? = null
    ) = withContext(Dispatchers.IO) {
        try {
            localGameRepository.clearLocalData()
            prefs.edit { clear() }
        } catch (_: Exception) {
            // Suppress exception during local teardown
        } finally {
            settingsRepository?.resetOnLogout()
            auth.signOut()
        }
    }

    companion object {
        private const val PREFS_NAME = "checkpoint_user_prefs"
        private fun keyUsername(uid: String) = "key_username_$uid"
        private fun keyFriendCode(uid: String) = "key_friend_code_$uid"
        private fun keyAvatar(uid: String) = "key_avatar_$uid"
    }
}