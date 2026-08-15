package com.example.checkpoint.data.remote

import com.example.checkpoint.data.remote.igdb.IgdbApiService
import com.example.checkpoint.data.remote.igdb.TwitchAuthApiService
import com.example.checkpoint.data.remote.retro.RetroAchievementsApiService
import com.example.checkpoint.data.remote.steam.SteamApiService
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object NetworkClient {

    private val json = Json { ignoreUnknownKeys = true }
    private val contentType = "application/json".toMediaType()

    // 1. Retrofit instance for Twitch Auth
    val twitchAuthApiService: TwitchAuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://id.twitch.tv/")
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(TwitchAuthApiService::class.java)
    }

    // 2. Retrofit instance for IGDB API
    val igdbApiService: IgdbApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.igdb.com/")
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(IgdbApiService::class.java)
    }

    // 3. Retrofit instance for Steam API
    val steamApiService: SteamApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(SteamApiService::class.java)
    }

    // 4. Retrofit instance for RetroAchievements API
    val retroApiService: RetroAchievementsApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://retroachievements.org/API/")
            .client(OkHttpClient())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(RetroAchievementsApiService::class.java)
    }
}