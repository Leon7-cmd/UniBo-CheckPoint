package com.example.checkpoint.data.remote.retro

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class RetroGameListDto(
    @SerialName("ID") val id: Long? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("ConsoleID") val consoleId: Long? = null,
    @SerialName("ConsoleName") val consoleName: String? = null,
    @SerialName("ImageIcon") val imageIcon: String? = null
)

interface RetroAchievementsApiService {
    // API call that returns a Game List for a specific Console
    @GET("API_GetGameList.php")
    suspend fun getGameListByConsole(
        @Query("z") username: String,
        @Query("y") apiKey: String,
        @Query("i") consoleId: Int
    ): List<RetroGameListDto>

    // API call that returns Game Details and Achievements given a specific gameId
    @GET("API_GetGameExtended.php")
    suspend fun getGameExtended(
        @Query("z") username: String,
        @Query("y") apiKey: String,
        @Query("i") gameId: String
    ): RetroGameExtendedResponse
}