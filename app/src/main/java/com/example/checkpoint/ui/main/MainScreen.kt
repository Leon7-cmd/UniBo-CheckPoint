package com.example.checkpoint.ui.main

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.checkpoint.ui.sections.library.LibraryScreen
import com.example.checkpoint.ui.navigation.*
import com.example.checkpoint.ui.sections.library.LibraryViewModel

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                val items = listOf(
                    Triple(BottomNavItem.PROFILE, ProfileRoute) {
                        bottomNavController.navigate(ProfileRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    Triple(BottomNavItem.LIBRARY, LibraryRoute) {
                        bottomNavController.navigate(LibraryRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    Triple(BottomNavItem.SEARCH, SearchRoute) {
                        bottomNavController.navigate(SearchRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    Triple(BottomNavItem.FRIENDS, FriendsRoute) {
                        bottomNavController.navigate(FriendsRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    Triple(BottomNavItem.SETTINGS, SettingsRoute) {
                        bottomNavController.navigate(SettingsRoute) {
                            popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                items.forEach { (item, route, navigate) ->
                    val isSelected = currentDestination?.route?.contains(item.name, ignoreCase = true) == true ||
                            (currentDestination?.route == null && item == BottomNavItem.PROFILE)

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = navigate
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = ProfileRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ProfileRoute> {
                PlaceholderScreen(title = "Schermata Profilo (XP, Level, Badge)")
            }
            composable<LibraryRoute> {
                val viewModel: LibraryViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                LibraryScreen(
                    uiState = uiState,
                    onGameClick = { gameId ->
                        //TODO: show game card
                    }
                )
            }
            composable<SearchRoute> {
                PlaceholderScreen(title = "Schermata Ricerca Giochi (API RAWG/IGDB)")
            }
            composable<FriendsRoute> {
                PlaceholderScreen(title = "Schermata Amici & Codice Amico")
            }
            composable<SettingsRoute> {
                PlaceholderScreen(title = "Impostazioni Tema & Account", onLogout = onLogout)
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String, onLogout: (() -> Unit)? = null) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            if (onLogout != null) {
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = onLogout) {
                    Text("Logout")
                }
            }
        }
    }
}