package com.example.checkpoint.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.serialization.Serializable

// Type-safe serializable destinations for Navigation Compose
@Serializable object ProfileRoute
@Serializable object LibraryRoute
@Serializable object SearchRoute
@Serializable object FriendsRoute
@Serializable object SettingsRoute

/**
 * Bottom navigation bar destinations mapping labels, icons, and type-safe routes.
 */
@Immutable
enum class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
) {
    PROFILE("Profilo", Icons.Default.Person, ProfileRoute),
    LIBRARY("Libreria", Icons.Default.SportsEsports, LibraryRoute),
    SEARCH("Cerca", Icons.Default.Search, SearchRoute),
    FRIENDS("Amici", Icons.Default.Group, FriendsRoute),
    SETTINGS("Settings", Icons.Default.Settings, SettingsRoute)
}