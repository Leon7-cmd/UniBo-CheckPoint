package com.example.checkpoint.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.AchievementSource

/**
 * Room database entity representing an achievement associated with a game.
 */
@Entity(
    tableName = "achievements",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["gameId"])]
)
data class AchievementEntity(
    @PrimaryKey val id: String,
    val gameId: String,
    val title: String = "",
    val description: String = "",
    val iconUrl: String = "",
    val isCompleted: Boolean = false,
    val source: String = AchievementSource.SYSTEM_DEFAULT.name
)

// Mapping helpers: Entity <-> Domain Model

fun AchievementEntity.toDomain(): Achievement = Achievement(
    id = id,
    gameId = gameId,
    title = title,
    description = description,
    iconUrl = iconUrl,
    isCompleted = isCompleted,
    source = runCatching { AchievementSource.valueOf(source) }
        .getOrDefault(AchievementSource.SYSTEM_DEFAULT)
)

fun Achievement.toEntity(fallbackGameId: String = ""): AchievementEntity = AchievementEntity(
    id = id,
    gameId = gameId.ifBlank { fallbackGameId },
    title = title,
    description = description,
    iconUrl = iconUrl,
    isCompleted = isCompleted,
    source = source.name
)