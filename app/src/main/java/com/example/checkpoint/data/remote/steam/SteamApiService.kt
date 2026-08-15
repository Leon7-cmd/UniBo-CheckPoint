package com.example.checkpoint.data.remote.steam

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

// Steam game data structure
@Serializable
data class SteamSchemaResponse(
    val game: SteamGameSchema? = null
)

@Serializable
data class SteamGameSchema(
    @SerialName("gameName") val gameName: String? = null,
    @SerialName("availableGameStats") val availableGameStats: SteamAvailableGameStats? = null
)

// Steam achievement data structure
@Serializable
data class SteamAvailableGameStats(
    val achievements: List<SteamAchievementDto>? = null
)

@Serializable
data class SteamAchievementDto(
    val name: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    val description: String? = null,
    val icon: String? = null,
    @SerialName("icongray") val iconGray: String? = null
)

// API call to get achievements given a specific appId
interface SteamApiService {
    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getSchemaForGame(
        @Query("key") apiKey: String,
        @Query("appid") appId: String,
        @Query("l") language: String = "italian"
    ): SteamSchemaResponse
}