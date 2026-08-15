package com.example.checkpoint.data.repository

import android.util.Log
import com.example.checkpoint.data.model.Game
import com.example.checkpoint.ui.sections.search.components.filter.SearchFilterState
import com.example.checkpoint.ui.sections.search.components.filter.SortOption
import com.example.checkpoint.data.remote.igdb.IgdbApiService
import com.example.checkpoint.data.remote.igdb.IgdbGameDto
import com.example.checkpoint.data.remote.igdb.TwitchAuthApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IgdbRepository(
    private val authApiService: TwitchAuthApiService,
    private val igdbApiService: IgdbApiService,
    private val clientId: String,
    private val clientSecret: String
) {
    private var accessToken: String? = null

    private suspend fun getOrFetchToken(): String {
        if (accessToken == null) {
            runCatching {
                authApiService.getAccessToken(clientId, clientSecret).accessToken
            }.onSuccess { accessToken = it }
                .onFailure { error ->
                    if (error is HttpException) {
                        Log.e("IGDB_ERROR", "Errore Auth Twitch (${error.code()}): ${error.response()?.errorBody()?.string()}")
                    }
                    throw error
                }
        }
        return accessToken!!
    }

    suspend fun searchGames(
        queryText: String,
        filters: SearchFilterState = SearchFilterState()
    ): Result<List<Game>> {
        if (queryText.isBlank() && !filters.hasActiveFilters()) {
            return Result.success(emptyList())
        }

        return runCatching {
            val token = getOrFetchToken()
            val queryRaw = buildIgdbQuery(queryText, filters)

            val results = igdbApiService.getGames(
                clientId = clientId,
                authorization = "Bearer $token",
                query = queryRaw.toRequestBody("text/plain".toMediaType())
            )
            results.map { it.toSearchGameDomain() }
        }.onFailure { error ->
            Log.e("IGDB_ERROR", "Errore in searchGames: ${error.message}")
        }
    }

    suspend fun getGameById(gameId: String): Result<Game> {
        return runCatching {
            val token = getOrFetchToken()
            val queryRaw = """
                where id = $gameId; 
                fields name, summary, storyline, cover.image_id, platforms.name, 
                       genres.name, themes.name, first_release_date, 
                       involved_companies.developer, involved_companies.publisher, involved_companies.company.name,
                       external_games.category, external_games.uid, external_games.url; 
                limit 1;
            """.trimIndent()

            val results = igdbApiService.getGames(
                clientId = clientId,
                authorization = "Bearer $token",
                query = queryRaw.toRequestBody("text/plain".toMediaType())
            )
            results.first().toDetailGameDomain()
        }.onFailure { error ->
            Log.e("IGDB_ERROR", "Errore in getGameById($gameId): ${error.message}")
        }
    }

    private fun buildIgdbQuery(searchQuery: String, filters: SearchFilterState): String {
        val cleanQuery = searchQuery.trim().replace("\"", "\\\"")
        val whereConditions = mutableListOf<String>()

        if (filters.selectedConsoles.isNotEmpty()) {
            whereConditions.add("(${filters.selectedConsoles.joinToString(" | ") { "platforms.name = \"${it.igdbValue}\"" }})")
        }
        if (filters.selectedGenres.isNotEmpty()) {
            whereConditions.add("(${filters.selectedGenres.joinToString(" | ") { "genres.name = \"${it.igdbValue}\"" }})")
        }
        if (filters.selectedGameplay.isNotEmpty()) {
            whereConditions.add("(${filters.selectedGameplay.joinToString(" | ") { "(themes.name = \"${it.igdbValue}\" | game_modes.name = \"${it.igdbValue}\")" }})")
        }

        val whereClause = if (whereConditions.isNotEmpty()) "where " + whereConditions.joinToString(" & ") + ";" else ""
        val sortClause = when (filters.sortBy) {
            SortOption.NAME_ASC -> "sort name asc;"
            SortOption.RATING_DESC -> "sort rating desc;"
            SortOption.RELEASE_DATE -> "sort first_release_date desc;"
        }

        return if (cleanQuery.isNotBlank()) {
            """
                search "$cleanQuery";
                fields name, cover.image_id, platforms.name, first_release_date;
                $whereClause
                limit 40;
            """.trimIndent()
        } else {
            """
                fields name, cover.image_id, platforms.name, first_release_date;
                $whereClause
                $sortClause
                limit 40;
            """.trimIndent()
        }
    }

    // MAPPER FOR IGDB GAMES HELPERS
    private fun IgdbGameDto.toSearchGameDomain() = Game(
        id = id.toString(),
        title = name ?: "Titolo non disponibile",
        coverUrl = cover?.imageId?.let { "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg" } ?: "",
        releaseDate = firstReleaseDate?.let { SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(it * 1000L)) } ?: "",
        platforms = platforms?.mapNotNull { it.name } ?: emptyList()
    )

    private fun IgdbGameDto.toDetailGameDomain(): Game {
        val coverUrl = cover?.imageId?.let { "https://images.igdb.com/igdb/image/upload/t_cover_big/$it.jpg" } ?: ""
        val releaseDateFormatted = firstReleaseDate?.let { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(it * 1000L)) } ?: ""

        val developers = involvedCompanies?.filter { it.developer }?.mapNotNull { it.company?.name }?.joinToString(", ") ?: ""
        val publishers = involvedCompanies?.filter { it.publisher }?.mapNotNull { it.company?.name }?.joinToString(", ") ?: ""

        val extractedTags = mutableListOf<String>().apply {
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
                extGame.url?.contains("steampowered.com/app/") == true -> Regex("""app/(\d+)""").find(extGame.url)?.groupValues?.get(1)
                extGame.uid?.contains("steampowered.com/app/") == true -> Regex("""app/(\d+)""").find(extGame.uid)?.groupValues?.get(1)
                else -> null
            }
        }

        return Game(
            id = id.toString(),
            title = name ?: "Titolo non disponibile",
            coverUrl = coverUrl,
            description = fullDescription,
            releaseDate = releaseDateFormatted,
            developer = developers,
            publisher = publishers,
            platforms = platforms?.mapNotNull { it.name } ?: emptyList(),
            tags = extractedTags,
            steamAppId = extractedSteamAppId
        )
    }
}