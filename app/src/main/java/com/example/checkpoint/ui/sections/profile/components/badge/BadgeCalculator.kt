package com.example.checkpoint.domain

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.model.GameStatus
import com.example.checkpoint.data.model.ProfileBadge
import com.example.checkpoint.data.model.UserStats

/**
 * Domain model representing calculated user level progression, statistics, and unlocked badges.
 */
@Immutable
data class UserProgression(
    val level: Int,
    val currentXp: Int,
    val nextLevelXp: Int,
    val stats: UserStats,
    val badges: List<ProfileBadge>
)

/**
 * Pure calculator for computing experience points, levels, and badge unlock conditions.
 */
object BadgeCalculator {
    const val XP_PER_LEVEL = 500

    fun calculateProgression(
        games: List<Game>,
        completedAchievementsCount: Int
    ): UserProgression {
        val totalGames = games.size

        // Single-pass iteration to aggregate all metrics efficiently
        var played = 0
        var completed = 0
        var reviewed = 0
        var favoritesCount = 0
        var notesCount = 0
        var fullReviewedCount = 0

        for (game in games) {
            val isPlayed = game.status == GameStatus.NOT_COMPLETED || game.status == GameStatus.COMPLETED
            val isCompleted = game.status == GameStatus.COMPLETED
            val hasRating = game.rating > 0f
            val hasReview = game.userReview.isNotBlank()

            if (isPlayed) played++
            if (isCompleted) completed++
            if (hasRating || hasReview) reviewed++
            if (hasRating && hasReview) fullReviewedCount++
            if (game.isFavorite) favoritesCount++
            if (game.note.isNotBlank()) notesCount++
        }

        // Evaluate badge unlock conditions
        val badges = listOf(
            ProfileBadge("b1", "Game On", "Aggiungi il tuo primo gioco alla libreria.", 50, totalGames >= 1),
            ProfileBadge("b2", "Collector Novice", "Raggiungi almeno 5 giochi nella libreria.", 100, totalGames >= 5),
            ProfileBadge("b3", "Hoarder", "Raggiungi almeno 15 giochi nella tua libreria.", 200, totalGames >= 15),
            ProfileBadge("b4", "Archivist", "Raggiungi almeno 30 giochi salvati.", 400, totalGames >= 30),
            ProfileBadge("b5", "Curator", "Aggiungi almeno 3 giochi tra i tuoi preferiti.", 75, favoritesCount >= 3),
            ProfileBadge("b6", "Top Tier", "Aggiungi almeno 8 giochi tra i preferiti.", 150, favoritesCount >= 8),
            ProfileBadge("b7", "Critic", "Vota o recensisci almeno un gioco.", 75, reviewed >= 1),
            ProfileBadge("b8", "Journalist", "Lascia sia voto che recensione a 3 giochi diversi.", 150, fullReviewedCount >= 3),
            ProfileBadge("b9", "Note Keeper", "Scrivi note o appunti personali per almeno 2 giochi.", 80, notesCount >= 2),
            ProfileBadge("b10", "First Victory", "Porta a termine il tuo primo gioco.", 100, completed >= 1),
            ProfileBadge("b11", "Master", "Completa almeno 3 giochi.", 250, completed >= 3),
            ProfileBadge("b12", "Veteran", "Completa 10 giochi e accumula esperienza.", 500, completed >= 10),
            ProfileBadge("b13", "Legendary Finisher", "Completa 20 giochi registrati.", 800, completed >= 20),
            ProfileBadge("b14", "Achievement Hunter", "Sblocca almeno 5 achievement.", 150, completedAchievementsCount >= 5),
            ProfileBadge("b15", "Trophy Master", "Sblocca almeno 25 achievement.", 350, completedAchievementsCount >= 25),
            ProfileBadge("b16", "Completionist God", "Sblocca 50 o più achievement complessivi.", 700, completedAchievementsCount >= 50)
        )

        // Calculate total XP and current level progress
        val badgeXp = badges.filter { it.isUnlocked }.sumOf { it.xpValue }
        val actionsXp = (completed * 100) + (played * 50) + (reviewed * 25) + (completedAchievementsCount * 20)
        val totalXp = actionsXp + badgeXp

        val level = (totalXp / XP_PER_LEVEL) + 1
        val currentLevelXp = totalXp % XP_PER_LEVEL

        val stats = UserStats(
            gamesPlayed = played,
            gamesCompleted = completed,
            gamesReviewed = reviewed,
            totalAchievements = completedAchievementsCount
        )

        return UserProgression(
            level = level,
            currentXp = currentLevelXp,
            nextLevelXp = XP_PER_LEVEL,
            stats = stats,
            badges = badges
        )
    }
}