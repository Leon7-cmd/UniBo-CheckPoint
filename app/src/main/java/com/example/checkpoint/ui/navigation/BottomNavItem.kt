package com.example.checkpoint.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable


// Serializable routs for navigation
@Serializable object ProfileRoute
@Serializable object CatalogRoute
@Serializable object SearchRoute
@Serializable object FriendsRoute
@Serializable object SettingsRoute

enum class BottomNavItem(
    val title: String,
    val icon: ImageVector
) {
    PROFILE("Profilo", Icons.Default.Person),
    CATALOG("Libreria", Icons.Default.Home),
    SEARCH("Cerca", Icons.Default.Search),
    FRIENDS("Amici", Icons.Default.Face),
    SETTINGS("Opzioni", Icons.Default.Settings)
}