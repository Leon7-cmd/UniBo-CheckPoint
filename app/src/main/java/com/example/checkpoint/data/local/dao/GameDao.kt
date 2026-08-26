package com.example.checkpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.checkpoint.data.entity.GameEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local persistence and queries on the games table.
 */
@Dao
interface GameDao {
    @Query("SELECT * FROM games ORDER BY title ASC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games")
    suspend fun getAllGamesList(): List<GameEntity>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun getGameById(id: String): Flow<GameEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Query("DELETE FROM games")
    suspend fun clearAllGames()

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteGameById(id: String)
}