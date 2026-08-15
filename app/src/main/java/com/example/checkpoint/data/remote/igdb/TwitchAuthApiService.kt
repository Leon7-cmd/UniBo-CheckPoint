package com.example.checkpoint.data.remote.igdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class TwitchAuthResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("token_type") val tokenType: String
)

// API call used to get the access token from Twitch
interface TwitchAuthApiService {
    @POST("oauth2/token")
    suspend fun getAccessToken(
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String,
        @Query("grant_type") grantType: String = "client_credentials"
    ): TwitchAuthResponse
}