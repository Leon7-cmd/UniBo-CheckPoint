package com.example.checkpoint.ui.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.remote.NetworkClient
import com.example.checkpoint.data.repository.AchievementRepository
import com.example.checkpoint.data.repository.IgdbRepository
import com.example.checkpoint.ui.navigation.*
import com.example.checkpoint.ui.sections.detail.DetailUiState
import com.example.checkpoint.ui.sections.detail.GameDetailScreen
import com.example.checkpoint.ui.sections.detail.GameDetailViewModel
import com.example.checkpoint.ui.sections.library.LibraryScreen
import com.example.checkpoint.ui.sections.library.LibraryViewModel
import com.example.checkpoint.ui.sections.search.SearchScreen
import kotlinx.serialization.Serializable

@Serializable
data class GameDetailRoute(val gameId: String)

@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 1. Define the top-level routes
    val topLevelRoutes = listOf(
        ProfileRoute::class,
        LibraryRoute::class,
        SearchRoute::class,
        FriendsRoute::class,
        SettingsRoute::class
    )

    // 2. Check if the destination is one of the top-level routes
    val shouldShowBottomBar = topLevelRoutes.any { routeClass ->
        currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
    }

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar) {
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
                        val isSelected = currentDestination?.hierarchy?.any { it.hasRoute(route::class) } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = navigate
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = ProfileRoute,
            // Pass innerPadding to NavHost
            modifier = Modifier.padding(if (shouldShowBottomBar) innerPadding else PaddingValues(0.dp))
        ) {
            composable<ProfileRoute> {
                PlaceholderScreen(title = "Schermata Profilo (XP, Level, Badge)")
            }

            composable<LibraryRoute>(
                popEnterTransition = { fadeIn(animationSpec = tween(200)) }
            ) {
                val viewModel: LibraryViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                LibraryScreen(
                    uiState = uiState,
                    onGameClick = { gameId ->
                        bottomNavController.navigate(GameDetailRoute(gameId))
                    }
                )
            }

            composable<SearchRoute> {
                SearchScreen(
                    onGameClick = { gameId ->
                        bottomNavController.navigate(GameDetailRoute(gameId = gameId))
                    }
                )
            }

            composable<GameDetailRoute>(
                popExitTransition = { fadeOut(animationSpec = tween(150)) },
                popEnterTransition = { EnterTransition.None },
            ) { backStackEntry ->
                val route: GameDetailRoute = backStackEntry.toRoute()

                // 1. Inizializziamo il ViewModel usando viewModelFactory per passare i Repository
                val detailViewModel: GameDetailViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            // Recupera le istanze dei repository dal tuo NetworkClient / App module
                            val igdbRepository = IgdbRepository(
                                authApiService = NetworkClient.twitchAuthApiService,
                                igdbApiService = NetworkClient.igdbApiService,
                                clientId = BuildConfig.IGDB_CLIENT_ID,
                                clientSecret = BuildConfig.IGDB_CLIENT_SECRET
                            )
                            val achievementRepository = AchievementRepository(
                                steamApiService = NetworkClient.steamApiService,
                                retroApiService = NetworkClient.retroApiService
                            )

                            GameDetailViewModel(
                                igdbRepository = igdbRepository,
                                achievementRepository = achievementRepository
                            )
                        }
                    }
                )

                val uiState by detailViewModel.uiState.collectAsState()
                val achievements by detailViewModel.achievements.collectAsState()
                val isLoadingAchievements by detailViewModel.isLoadingAchievements.collectAsState()

                // 2. Un unico LaunchedEffect basato sul gameId
                LaunchedEffect(route.gameId) {
                    detailViewModel.loadGameDetails(route.gameId)
                }

                // 3. Rendering in base allo stato
                when (val state = uiState) {
                    is DetailUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is DetailUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is DetailUiState.Success -> {
                        GameDetailScreen(
                            game = state.game,
                            achievements = achievements,
                            isLoadingAchievements = isLoadingAchievements,
                            onToggleAchievement = { achievementId ->
                                detailViewModel.toggleAchievement(achievementId)
                            },
                            onBackClick = { bottomNavController.popBackStack() },
                            onFavoriteToggle = { detailViewModel.toggleFavorite() },
                            onToPlayToggle = { detailViewModel.toggleToPlay() },
                            onRatingChange = { newRating -> detailViewModel.updateRating(newRating) }
                        )
                    }
                }
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
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