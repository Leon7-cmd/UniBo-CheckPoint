package com.example.checkpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.checkpoint.data.model.AppThemeMode
import com.example.checkpoint.data.repository.globalAppSettings
import com.example.checkpoint.data.repository.initAppSettings
import com.example.checkpoint.ui.navigation.AppNavigation
import com.example.checkpoint.ui.theme.CheckpointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        initAppSettings(applicationContext)
        enableEdgeToEdge()

        setContent {
            val currentSettings by globalAppSettings.collectAsState()

            // Resolve theme mode
            val isDarkTheme = when (currentSettings.themeMode) {
                AppThemeMode.DARK -> true
                AppThemeMode.LIGHT -> false
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            CheckpointTheme(
                darkTheme = isDarkTheme,
                accentColor = currentSettings.accentColor
            ) {
                AppNavigation()
            }
        }
    }
}