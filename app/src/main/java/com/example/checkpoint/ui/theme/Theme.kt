package com.example.checkpoint.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.checkpoint.data.model.AppAccentColor

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    surfaceVariant = Color(0xFF1E1E1E),
    surfaceContainerHighest = Color(0xFF2C2C2C),
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFF3A3A3C)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    background = Color(0xFFF8F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF0F2F5),
    surfaceContainerHighest = Color(0xFFE5E7EB),
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFFD1D5DB)
)

@Composable
fun CheckpointTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AppAccentColor = AppAccentColor.PURPLE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    // Select base color scheme (Dynamic, Dark, or Light)
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply custom accent colors without recalculating on every recomposition
    val customColorScheme = remember(darkTheme, accentColor, baseScheme) {
        val selectedSecondary = if (darkTheme) accentColor.darkSecondary else accentColor.lightSecondary
        val selectedTertiary = if (darkTheme) accentColor.darkTertiary else accentColor.lightTertiary

        baseScheme.copy(
            primary = selectedSecondary,
            onPrimary = if (darkTheme) Color.Black else Color.White,
            primaryContainer = selectedSecondary.copy(alpha = 0.22f),
            onPrimaryContainer = if (darkTheme) Color.White else selectedSecondary,
            secondary = selectedSecondary,
            onSecondary = if (darkTheme) Color.Black else Color.White,
            secondaryContainer = selectedSecondary.copy(alpha = 0.18f),
            onSecondaryContainer = if (darkTheme) Color.White else selectedSecondary,
            tertiary = selectedTertiary,
            tertiaryContainer = selectedTertiary.copy(alpha = 0.18f)
        )
    }

    MaterialTheme(
        colorScheme = customColorScheme,
        typography = Typography,
        content = content
    )
}