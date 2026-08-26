package com.example.checkpoint.data.remote.igdb

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

/**
 * Retrofit interface for querying the IGDB v4 Games API.
 */
interface IgdbApiService {
    @Headers("Content-Type: text/plain")
    @POST("v4/games")
    suspend fun getGames(
        @Header("Client-ID") clientId: String,
        @Header("Authorization") authorization: String,
        @Body query: RequestBody
    ): List<IgdbGameDto>
}

// DTO MODELS

@Immutable
@Serializable
data class IgdbGameDto(
    val id: Long = 0L,
    val name: String? = null,
    val summary: String? = null,
    val storyline: String? = null,
    val cover: IgdbCoverDto? = null,
    val platforms: List<PlatformDto>? = null,
    val genres: List<IgdbNamedEntityDto>? = null,
    val themes: List<IgdbNamedEntityDto>? = null,
    @SerialName("first_release_date") val firstReleaseDate: Long? = null,
    @SerialName("involved_companies") val involvedCompanies: List<IgdbInvolvedCompanyDto>? = null,
    @SerialName("external_games") val externalGames: List<IgdbExternalGameDto>? = null
)

@Immutable
@Serializable
data class PlatformDto(
    val id: Long? = null,
    val name: String? = null,
    val abbreviation: String? = null
)

@Immutable
@Serializable
data class IgdbCoverDto(
    @SerialName("image_id") val imageId: String? = null
)

@Immutable
@Serializable
data class IgdbNamedEntityDto(
    val name: String? = null
)

@Immutable
@Serializable
data class IgdbInvolvedCompanyDto(
    val id: Long = 0L,
    val company: IgdbNamedEntityDto? = null,
    val developer: Boolean = false,
    val publisher: Boolean = false
)

@Immutable
@Serializable
data class IgdbExternalGameDto(
    val id: Long = 0L,
    val category: Int = 0,
    val uid: String? = null,
    val url: String? = null
)