package com.example.checkpoint.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus

/**
 * Room database entity representing a stored game record and its library metadata.
 */
@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val coverUrl: String = "",
    val releaseDate: String = "",
    val developer: String = "",
    val publisher: String = "",
    val description: String = "",
    val platforms: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val rating: Float = 0f,
    val communityRating: Float? = null,
    val userReview: String = "",
    val note: String = "",
    val isFavorite: Boolean = false,
    val status: GameStatus = GameStatus.NONE,
    val steamAppId: String? = null,
    val retroGameId: String? = null
)

// Mapping helpers: Entity <-> Domain Model

fun GameEntity.toDomain(): Game = Game(
    id = id,
    title = title,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    developer = developer,
    publisher = publisher,
    description = description,
    platforms = platforms,
    tags = tags,
    rating = rating,
    communityRating = communityRating,
    userReview = userReview,
    note = note,
    isFavorite = isFavorite,
    status = status,
    steamAppId = steamAppId,
    retroGameId = retroGameId
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    title = title,
    coverUrl = coverUrl,
    releaseDate = releaseDate,
    developer = developer,
    publisher = publisher,
    description = description,
    platforms = platforms,
    tags = tags,
    rating = rating,
    communityRating = communityRating,
    userReview = userReview,
    note = note,
    isFavorite = isFavorite,
    status = status,
    steamAppId = steamAppId,
    retroGameId = retroGameId
)