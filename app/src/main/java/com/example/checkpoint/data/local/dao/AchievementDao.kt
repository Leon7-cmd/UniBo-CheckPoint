package com.example.checkpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.checkpoint.data.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object managing local persistence, status updates, and synchronization for achievements.
 */
@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements WHERE gameId = :gameId")
    fun getAchievementsByGameId(gameId: String): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAchievements(achievements: List<AchievementEntity>)

    @Query("UPDATE achievements SET isCompleted = :isCompleted WHERE id = :achievementId")
    suspend fun updateAchievementStatus(achievementId: String, isCompleted: Boolean)

    @Query("SELECT COUNT(*) FROM achievements WHERE isCompleted = 1")
    fun getTotalCompletedAchievementsCount(): Flow<Int>

    @Query("DELETE FROM achievements")
    suspend fun clearAllAchievements()

    // Cloud synchronization queries
    @Query("SELECT * FROM achievements WHERE isCompleted = 1")
    suspend fun getAllCompletedAchievements(): List<AchievementEntity>

    @Query("UPDATE achievements SET isCompleted = 1 WHERE id IN (:achievementIds)")
    suspend fun markAchievementsCompleted(achievementIds: List<String>)
}