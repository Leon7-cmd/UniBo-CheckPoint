package com.example.checkpoint.data.repository

import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.model.Achievement
import com.example.checkpoint.data.model.AchievementSource
import com.example.checkpoint.data.remote.retro.RetroAchievementDto
import com.example.checkpoint.data.remote.retro.RetroAchievementsApiService
import com.example.checkpoint.data.remote.retro.util.RetroConsoleMapper
import com.example.checkpoint.data.remote.retro.util.sanitizeForMatching
import com.example.checkpoint.data.remote.steam.SteamAchievementDto
import com.example.checkpoint.data.remote.steam.SteamApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_ACHIEVEMENTS = 700
private val SEQUEL_REGEX = Regex("""^(2|3|4|5|6|7|8|9|ii|iii|iv|v|vi|2nd|3rd).*""", RegexOption.IGNORE_CASE)

/**
 * Repository responsible for aggregating achievements from Steam, RetroAchievements, or generating system fallbacks.
 */
class AchievementRepository(
    private val steamApiService: SteamApiService,
    private val retroApiService: RetroAchievementsApiService,
    private val retroUsername: String = BuildConfig.RETRO_USERNAME.trim(),
    private val retroApiKey: String = BuildConfig.RETRO_API_KEY.trim(),
    private val steamApiKey: String = BuildConfig.STEAM_API_KEY.trim()
) {

    // Retrieve achievements querying Steam first, then RetroAchievements, falling back to system defaults
    suspend fun getAchievements(
        gameId: String,
        gameTitle: String,
        platforms: List<String>? = null,
        steamAppId: String? = null,
        retroGameId: String? = null
    ): List<Achievement> = withContext(Dispatchers.IO) {

        // 1. Steam integration
        if (!steamAppId.isNullOrEmpty()) {
            val steamList = fetchSteamAchievements(gameId, steamAppId)
            if (steamList.isNotEmpty()) return@withContext steamList
        }

        // 2. RetroAchievements by direct Game ID
        if (!retroGameId.isNullOrEmpty()) {
            val retroList = fetchRetroAchievements(gameId, retroGameId)
            if (retroList.isNotEmpty()) return@withContext retroList
        }

        // 3. RetroAchievements resolution by Title and Platform matching
        if (gameTitle.isNotBlank()) {
            val consoleIds = RetroConsoleMapper.getRetroConsoleIds(platforms)

            for (consoleId in consoleIds) {
                val foundRetroId = findRetroGameId(gameTitle, consoleId) ?: continue
                val retroList = fetchRetroAchievements(gameId, foundRetroId)
                if (retroList.isNotEmpty()) return@withContext retroList
            }
        }

        // 4. Fallback system achievements
        createDefaultSystemAchievements(gameId)
    }

    private suspend fun findRetroGameId(igdbTitle: String, consoleId: Int): String? {
        if (retroUsername.isBlank() || retroApiKey.isBlank()) return null

        return runCatching {
            val gameList = retroApiService.getGameListByConsole(retroUsername, retroApiKey, consoleId)
            val cleanIgdbTitle = igdbTitle.sanitizeForMatching()

            // Exact match
            var matched = gameList.firstOrNull {
                it.title?.sanitizeForMatching() == cleanIgdbTitle
            }

            // Prefix match with sequel filtering
            if (matched == null) {
                matched = gameList.firstOrNull { game ->
                    val cleanRetro = game.title?.sanitizeForMatching() ?: ""
                    if (cleanRetro.startsWith(cleanIgdbTitle)) {
                        val extra = cleanRetro.removePrefix(cleanIgdbTitle).trim()
                        !extra.matches(SEQUEL_REGEX)
                    } else {
                        false
                    }
                }
            }

            matched?.id?.toString()
        }.getOrNull()
    }

    private suspend fun fetchSteamAchievements(gameId: String, steamAppId: String): List<Achievement> {
        return runCatching {
            val response = steamApiService.getSchemaForGame(apiKey = steamApiKey, appId = steamAppId)
            response.game?.availableGameStats?.achievements
                ?.take(MAX_ACHIEVEMENTS)
                ?.mapIndexed { index, dto -> dto.toDomain(gameId, index) }
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    private suspend fun fetchRetroAchievements(gameId: String, retroGameId: String): List<Achievement> {
        return runCatching {
            val response = retroApiService.getGameExtended(retroUsername, retroApiKey, retroGameId)
            response.achievements?.values
                ?.take(MAX_ACHIEVEMENTS)
                ?.map { it.toDomain(gameId) }
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun createDefaultSystemAchievements(gameId: String) = listOf(
        Achievement(
            id = "sys_${gameId}_started",
            gameId = gameId,
            title = "Gioco Iniziato",
            description = "Spunta per spostare il gioco in 'In Corso'",
            source = AchievementSource.SYSTEM_DEFAULT,
            iconUrl = "",
            isCompleted = false
        ),
        Achievement(
            id = "sys_${gameId}_completed",
            gameId = gameId,
            title = "Storia Finita",
            description = "Spunta quando hai terminato la storia principale",
            source = AchievementSource.SYSTEM_DEFAULT,
            iconUrl = "",
            isCompleted = false
        ),
        Achievement(
            id = "sys_${gameId}_100",
            gameId = gameId,
            title = "100%",
            description = "Spunta quando hai terminato il gioco al 100%",
            source = AchievementSource.SYSTEM_DEFAULT,
            iconUrl = "",
            isCompleted = false
        )
    )

    // DTO to Domain Mappers
    private fun SteamAchievementDto.toDomain(gameId: String, index: Int) = Achievement(
        id = "steam_${gameId}_${name ?: index}",
        gameId = gameId,
        title = displayName ?: name ?: "Achievement #$index",
        description = description ?: "Nessuna descrizione disponibile.",
        iconUrl = icon ?: "",
        source = AchievementSource.STEAM
    )

    private fun RetroAchievementDto.toDomain(gameId: String) = Achievement(
        id = "retro_${gameId}_$id",
        gameId = gameId,
        title = title ?: "Obiettivo",
        description = description ?: "",
        iconUrl = badgeName?.let {
            if (it.startsWith("http")) it else "https://media.retroachievements.org/Badge/$it.png"
        } ?: "",
        source = AchievementSource.RETRO_ACHIEVEMENTS
    )
}