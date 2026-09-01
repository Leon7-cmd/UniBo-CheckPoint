package com.example.checkpoint.data.repository

import android.util.Log
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.data.remote.igdb.IgdbApiService
import com.example.checkpoint.data.remote.igdb.IgdbGameDto
import com.example.checkpoint.data.remote.igdb.TwitchAuthApiService
import com.example.checkpoint.ui.sections.search.components.filter.SearchFilterState
import com.example.checkpoint.ui.sections.search.components.filter.SortOption
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "IGDB_REPOSITORY"
private const val COVER_BASE_URL = "https://images.igdb.com/igdb/image/upload/t_cover_big/"
private val MEDIA_TYPE_TEXT = "text/plain".toMediaType()
private val STEAM_APP_ID_REGEX = Regex("""app/(\d+)""")

private fun formatYear(timestampSeconds: Long?): String {
    if (timestampSeconds == null) return ""
    return SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(timestampSeconds * 1000L))
}

private fun formatFullDate(timestampSeconds: Long?): String {
    if (timestampSeconds == null) return ""
    return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(timestampSeconds * 1000L))
}

/**
 * Repository responsible for querying Twitch OAuth and IGDB game catalog API.
 */
class IgdbRepository(
    private val authApiService: TwitchAuthApiService,
    private val igdbApiService: IgdbApiService,
    private val clientId: String,
    private val clientSecret: String
) {
    private var accessToken: String? = null
    private val authMutex = Mutex()

    // Thread-safe Twitch OAuth token retrieval
    private suspend fun getOrFetchToken(forceRefresh: Boolean = false): String {
        return authMutex.withLock {
            if (forceRefresh || accessToken == null) {
                runCatching {
                    authApiService.getAccessToken(clientId, clientSecret).accessToken
                }.onSuccess {
                    accessToken = it
                }.onFailure { error ->
                    if (error is HttpException) {
                        Log.e(TAG, "Twitch Auth Error (${error.code()}): ${error.response()?.errorBody()?.string()}")
                    }
                    throw error
                }.getOrThrow()
            } else {
                accessToken!!
            }
        }
    }

    // Search games catalog with multi-field filters and automatic 401 retry
    suspend fun searchGames(
        queryText: String,
        filters: SearchFilterState = SearchFilterState()
    ): Result<List<Game>> {
        if (queryText.isBlank() && !filters.hasActiveFilters()) {
            return Result.success(emptyList())
        }

        return runCatching {
            executeWithRetry { token ->
                val queryRaw = buildIgdbQuery(queryText, filters)
                igdbApiService.getGames(
                    clientId = clientId,
                    authorization = "Bearer $token",
                    query = queryRaw.toRequestBody(MEDIA_TYPE_TEXT)
                ).map { it.toSearchGameDomain() }
            }
        }.onFailure { error ->
            Log.e(TAG, "Error in searchGames: ${error.message}")
        }
    }

    // Fetch comprehensive details for a single game
    suspend fun getGameById(gameId: String): Result<Game> {
        return runCatching {
            executeWithRetry { token ->
                val queryRaw = """
                    where id = $gameId; 
                    fields name, summary, storyline, cover.image_id, platforms.name, platforms.abbreviation, 
                           genres.name, themes.name, first_release_date, 
                           involved_companies.developer, involved_companies.publisher, involved_companies.company.name,
                           external_games.category, external_games.uid, external_games.url; 
                    limit 1;
                """.trimIndent()

                val results = igdbApiService.getGames(
                    clientId = clientId,
                    authorization = "Bearer $token",
                    query = queryRaw.toRequestBody(MEDIA_TYPE_TEXT)
                )

                val gameDto = results.firstOrNull() ?: throw NoSuchElementException("No game found for ID: $gameId")
                gameDto.toDetailGameDomain()
            }
        }.onFailure { error ->
            Log.e(TAG, "Error in getGameById($gameId): ${error.message}")
        }
    }

    // Execute API block with automatic retry upon token expiration (HTTP 401)
    private suspend fun <T> executeWithRetry(block: suspend (token: String) -> T): T {
        val currentToken = getOrFetchToken()
        return try {
            block(currentToken)
        } catch (e: HttpException) {
            if (e.code() == 401) {
                val refreshedToken = getOrFetchToken(forceRefresh = true)
                block(refreshedToken)
            } else {
                throw e
            }
        }
    }

    private fun buildIgdbQuery(searchQuery: String, filters: SearchFilterState): String {
        val cleanQuery = searchQuery.trim().replace("\"", "\\\"")

        val whereConditions = buildList {
            if (filters.selectedConsoles.isNotEmpty()) {
                add("(${filters.selectedConsoles.joinToString(" | ") { "platforms.name = \"${it.igdbValue}\"" }})")
            }
            if (filters.selectedGenres.isNotEmpty()) {
                add("(${filters.selectedGenres.joinToString(" | ") { "genres.name = \"${it.igdbValue}\"" }})")
            }
            if (filters.selectedGameplay.isNotEmpty()) {
                add("(${filters.selectedGameplay.joinToString(" | ") { "(themes.name = \"${it.igdbValue}\" | game_modes.name = \"${it.igdbValue}\")" }})")
            }
        }

        val whereClause = if (whereConditions.isNotEmpty()) {
            "where " + whereConditions.joinToString(" & ") + ";"
        } else {
            ""
        }

        return if (cleanQuery.isNotBlank()) {
            """
                search "$cleanQuery";
                fields name, cover.image_id, platforms.name, platforms.abbreviation, first_release_date;
                $whereClause
                limit 40;
            """.trimIndent()
        } else {
            val sortClause = when (filters.sortBy) {
                SortOption.NAME_ASC -> "sort name asc;"
                SortOption.RATING_DESC -> "sort rating desc;"
                SortOption.RELEASE_DATE -> "sort first_release_date desc;"
            }
            """
                fields name, cover.image_id, platforms.name, platforms.abbreviation, first_release_date;
                $whereClause
                $sortClause
                limit 40;
            """.trimIndent()
        }
    }

    // --- MAPPERS ---

    private fun IgdbGameDto.toSearchGameDomain(): Game {
        val extractedPlatforms = platforms?.mapNotNull {
            it.abbreviation?.ifBlank { null } ?: it.name?.ifBlank { null }
        }.orEmpty()

        return Game(
            id = id.toString(),
            title = name ?: "Titolo non disponibile",
            coverUrl = cover?.imageId?.let { "$COVER_BASE_URL$it.jpg" }.orEmpty(),
            releaseDate = formatYear(firstReleaseDate),
            platforms = extractedPlatforms
        )
    }

    private fun IgdbGameDto.toDetailGameDomain(): Game {
        val coverUrl = cover?.imageId?.let { "$COVER_BASE_URL$it.jpg" }.orEmpty()
        val releaseDateFormatted = formatFullDate(firstReleaseDate)

        val developers = involvedCompanies
            ?.filter { it.developer }
            ?.mapNotNull { it.company?.name }
            ?.joinToString(", ")
            .orEmpty()

        val publishers = involvedCompanies
            ?.filter { it.publisher }
            ?.mapNotNull { it.company?.name }
            ?.joinToString(", ")
            .orEmpty()

        val extractedTags = buildList {
            genres?.mapNotNull { it.name }?.let { addAll(it) }
            themes?.mapNotNull { it.name }?.let { addAll(it) }
        }

        val fullDescription = when {
            !summary.isNullOrBlank() -> summary
            !storyline.isNullOrBlank() -> storyline
            else -> "Nessuna descrizione disponibile per questo titolo."
        }

        val extractedSteamAppId = externalGames?.firstNotNullOfOrNull { extGame ->
            when {
                extGame.category == 1 && !extGame.uid.isNullOrBlank() -> extGame.uid
                else -> {
                    val candidate = extGame.url ?: extGame.uid
                    candidate?.let { STEAM_APP_ID_REGEX.find(it)?.groupValues?.get(1) }
                }
            }
        }

        val extractedPlatforms = platforms?.mapNotNull {
            it.abbreviation?.ifBlank { null } ?: it.name?.ifBlank { null }
        }.orEmpty()

        return Game(
            id = id.toString(),
            title = name ?: "Titolo non disponibile",
            coverUrl = coverUrl,
            description = fullDescription,
            releaseDate = releaseDateFormatted,
            developer = developers,
            publisher = publishers,
            platforms = extractedPlatforms,
            tags = extractedTags,
            steamAppId = extractedSteamAppId
        )
    }
}