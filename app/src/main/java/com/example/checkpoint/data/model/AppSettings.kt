package com.example.checkpoint.data.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

/**
 * Accent color palette options with defined light and dark secondary/tertiary colors.
 */
@Immutable
@Serializable
enum class AppAccentColor(
    val label: String,
    val lightSecondary: Color,
    val darkSecondary: Color,
    val lightTertiary: Color,
    val darkTertiary: Color
) {
    PURPLE(
        label = "Viola",
        lightSecondary = Color(0xFF625B71),
        darkSecondary = Color(0xFFCCC2DC),
        lightTertiary = Color(0xFF7D5260),
        darkTertiary = Color(0xFFEFB8C8)
    ),
    CYAN(
        label = "Ciano",
        lightSecondary = Color(0xFF006874),
        darkSecondary = Color(0xFF4FD8EB),
        lightTertiary = Color(0xFF006A60),
        darkTertiary = Color(0xFF53DBC9)
    ),
    EMERALD(
        label = "Smeraldo",
        lightSecondary = Color(0xFF006C4C),
        darkSecondary = Color(0xFF5BDBA8),
        lightTertiary = Color(0xFF386567),
        darkTertiary = Color(0xFFA0CFD0)
    ),
    AMBER(
        label = "Ambra",
        lightSecondary = Color(0xFF7A5900),
        darkSecondary = Color(0xFFF2BF48),
        lightTertiary = Color(0xFF7E570F),
        darkTertiary = Color(0xFFF3BD71)
    ),
    ROSE(
        label = "Rosa",
        lightSecondary = Color(0xFF8F4C5C),
        darkSecondary = Color(0xFFFFB2BF),
        lightTertiary = Color(0xFF7D5260),
        darkTertiary = Color(0xFFEFB8C8)
    ),
    ORANGE(
        label = "Arancione",
        lightSecondary = Color(0xFF8B5000),
        darkSecondary = Color(0xFFFFB870),
        lightTertiary = Color(0xFF775930),
        darkTertiary = Color(0xFFE7BF8F)
    )
}

/**
 * Application visual theme modes.
 */
@Immutable
@Serializable
enum class AppThemeMode(val label: String) {
    DARK("Scuro"),
    LIGHT("Chiaro"),
    SYSTEM("Sistema")
}

/**
 * Privacy visibility levels for profile sections.
 */
@Immutable
@Serializable
enum class PrivacyLevel(val label: String) {
    PUBLIC("Pubblico"),
    FRIENDS_ONLY("Solo Amici"),
    PRIVATE("Privato")
}

/**
 * Domain model representing application settings, appearance preferences, and privacy rules.
 */
@Immutable
@Serializable
data class AppSettings(
    val language: String = "Italiano",
    val notificationsEnabled: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val accentColor: AppAccentColor = AppAccentColor.PURPLE,
    val showStatsPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val showBadgesPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC,
    val showLibraryPrivacy: PrivacyLevel = PrivacyLevel.PUBLIC
)