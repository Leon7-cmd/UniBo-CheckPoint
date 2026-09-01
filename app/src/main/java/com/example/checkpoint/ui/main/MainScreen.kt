package com.example.checkpoint.ui.main

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.checkpoint.BuildConfig
import com.example.checkpoint.data.local.AppDatabase
import com.example.checkpoint.data.model.Friend
import com.example.checkpoint.data.remote.NetworkClient
import com.example.checkpoint.data.repository.AchievementRepository
import com.example.checkpoint.data.repository.FriendsRepository
import com.example.checkpoint.data.repository.IgdbRepository
import com.example.checkpoint.data.repository.LocalGameRepository
import com.example.checkpoint.data.repository.SettingsRepository
import com.example.checkpoint.data.repository.UserProfileRepository
import com.example.checkpoint.ui.navigation.*
import com.example.checkpoint.ui.sections.detail.GameDetailScreen
import com.example.checkpoint.ui.sections.detail.GameDetailViewModel
import com.example.checkpoint.ui.sections.friends.FriendsScreen
import com.example.checkpoint.ui.sections.friends.FriendsViewModel
import com.example.checkpoint.ui.sections.friends.components.detail.FriendDetailScreen
import com.example.checkpoint.ui.sections.friends.components.request.FriendRequestsScreen
import com.example.checkpoint.ui.sections.library.LibraryScreen
import com.example.checkpoint.ui.sections.library.LibraryViewModel
import com.example.checkpoint.ui.sections.profile.ProfileScreen
import com.example.checkpoint.ui.sections.profile.ProfileViewModel
import com.example.checkpoint.ui.sections.profile.components.badge.BadgesListScreen
import com.example.checkpoint.ui.sections.search.SearchScreen
import com.example.checkpoint.ui.sections.search.SearchViewModel
import com.example.checkpoint.ui.sections.settings.SettingsScreen
import com.example.checkpoint.ui.sections.settings.SettingsViewModel
import kotlinx.serialization.Serializable

// Type-safe routes for sub-destinations
@Serializable data class GameDetailRoute(val gameId: String)
@Serializable object BadgesRoute
@Serializable data class FriendDetailRoute(val friendId: String)
@Serializable object FriendRequestsRoute

// Function made with the sole purpose of reducing the boilerplate code for view models (almost 80 lines of code)
@Suppress("UNCHECKED_CAST")
@Composable
private inline fun <reified VM : ViewModel> provideViewModel(crossinline creator: () -> VM): VM =
    viewModel(factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T = creator() as T
    })

/**
 * Main application host managing bottom bar navigation, top-level tabs, and secondary screen transitions.
 */
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val appContext = LocalContext.current.applicationContext

    // Repositories initialization
    val database = remember { AppDatabase.getDatabase(appContext) }
    val localGameRepository = remember { LocalGameRepository(database.gameDao(), database.achievementDao()) }
    val userProfileRepository = remember { UserProfileRepository(appContext) }
    val settingsRepository = remember { SettingsRepository(appContext) }
    val friendsRepository = remember { FriendsRepository() }

    val igdbRepository = remember {
        IgdbRepository(
            authApiService = NetworkClient.twitchAuthApiService,
            igdbApiService = NetworkClient.igdbApiService,
            clientId = BuildConfig.IGDB_CLIENT_ID,
            clientSecret = BuildConfig.IGDB_CLIENT_SECRET
        )
    }

    val achievementRepository = remember {
        AchievementRepository(
            steamApiService = NetworkClient.steamApiService,
            retroApiService = NetworkClient.retroApiService
        )
    }

    val isTopLevelScreen = currentDestination?.hierarchy?.any { dest ->
        dest.hasRoute(ProfileRoute::class) ||
                dest.hasRoute(LibraryRoute::class) ||
                dest.hasRoute(SearchRoute::class) ||
                dest.hasRoute(FriendsRoute::class) ||
                dest.hasRoute(SettingsRoute::class)
    } == true

    LaunchedEffect(Unit) {
        localGameRepository.syncWithCloud()
        localGameRepository.syncAchievementsWithCloud()
    }

    Scaffold(
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar {
                    BottomNavItem.entries.forEach { item ->
                        val isSelected = currentDestination.hierarchy.any { it.hasRoute(item.route::class) }
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title) },
                            selected = isSelected,
                            onClick = {
                                bottomNavController.navigate(item.route) {
                                    popUpTo(bottomNavController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val topPadding = innerPadding.calculateTopPadding()
        val bottomBarPadding = if (isTopLevelScreen) innerPadding.calculateBottomPadding() else 0.dp

        NavHost(
            navController = bottomNavController,
            startDestination = ProfileRoute,
            enterTransition = {
                fadeIn(tween(180, easing = FastOutSlowInEasing)) + scaleIn(initialScale = 0.98f, animationSpec = tween(180, easing = FastOutSlowInEasing))
            },
            exitTransition = { fadeOut(tween(120, easing = FastOutSlowInEasing)) },
            popEnterTransition = { fadeIn(tween(180, easing = FastOutSlowInEasing)) },
            popExitTransition = {
                fadeOut(tween(120, easing = FastOutSlowInEasing)) + scaleOut(targetScale = 0.98f, animationSpec = tween(120, easing = FastOutSlowInEasing))
            },
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile Tab
            composable<ProfileRoute> {
                val profileViewModel: ProfileViewModel = provideViewModel {
                    ProfileViewModel(localGameRepository, userProfileRepository)
                }
                val profileUiState by profileViewModel.uiState.collectAsState()

                ProfileScreen(
                    modifier = Modifier.padding(top = topPadding, bottom = bottomBarPadding),
                    uiState = profileUiState,
                    onSeeAllBadgesClick = { bottomNavController.navigate(BadgesRoute) },
                    onAvatarSelected = profileViewModel::onAvatarSelected
                )
            }

            // All Badges list
            composable<BadgesRoute>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200, easing = FastOutSlowInEasing)) }
            ) {
                val profileViewModel: ProfileViewModel = provideViewModel {
                    ProfileViewModel(localGameRepository, userProfileRepository)
                }
                val profileUiState by profileViewModel.uiState.collectAsState()

                BadgesListScreen(
                    badges = profileUiState.badges,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            // Library Tab
            composable<LibraryRoute> {
                val libraryViewModel: LibraryViewModel = provideViewModel {
                    LibraryViewModel(localGameRepository)
                }
                val uiState by libraryViewModel.uiState.collectAsState()

                LibraryScreen(
                    modifier = Modifier.padding(top = topPadding, bottom = bottomBarPadding),
                    uiState = uiState,
                    onSectionSelected = libraryViewModel::selectSection,
                    onBackToMainLibrary = libraryViewModel::clearSelectedSection,
                    onGameClick = { gameId -> bottomNavController.navigate(GameDetailRoute(gameId)) }
                )
            }

            // Search Tab
            composable<SearchRoute> {
                val searchViewModel: SearchViewModel = viewModel()
                SearchScreen(
                    modifier = Modifier.padding(top = topPadding, bottom = bottomBarPadding),
                    onGameClick = { gameId -> bottomNavController.navigate(GameDetailRoute(gameId)) },
                    viewModel = searchViewModel
                )
            }

            // Game Detail
            composable<GameDetailRoute>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200, easing = FastOutSlowInEasing)) }
            ) { backStackEntry ->
                val route: GameDetailRoute = backStackEntry.toRoute()
                val detailViewModel: GameDetailViewModel = provideViewModel {
                    GameDetailViewModel(igdbRepository, achievementRepository, localGameRepository, userProfileRepository)
                }
                val uiState by detailViewModel.uiState.collectAsState()

                LaunchedEffect(route.gameId) {
                    detailViewModel.loadGameDetails(route.gameId)
                }

                when {
                    uiState.isLoadingDetails -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.errorMessage != null && uiState.game == null -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = uiState.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                        }
                    }
                    else -> {
                        uiState.game?.let { currentGame ->
                            GameDetailScreen(
                                game = currentGame,
                                achievements = uiState.achievements,
                                communityReviews = uiState.communityReviews,
                                averageRating = uiState.averageRating,
                                totalReviewsCount = uiState.totalReviewsCount,
                                isLoadingAchievements = uiState.isLoadingAchievements,
                                isLoadingCommunityReviews = uiState.isLoadingCommunityReviews,
                                onToggleAchievement = detailViewModel::toggleAchievement,
                                onBackClick = { bottomNavController.popBackStack() },
                                onFavoriteToggle = detailViewModel::toggleFavorite,
                                onToPlayToggle = detailViewModel::toggleToPlay,
                                onSaveReview = detailViewModel::saveReview,
                                onDeleteReview = detailViewModel::deleteReview
                            )
                        }
                    }
                }
            }

            // Friends Tab
            composable<FriendsRoute> {
                val friendsViewModel: FriendsViewModel = provideViewModel {
                    FriendsViewModel(friendsRepository)
                }
                val uiState by friendsViewModel.uiState.collectAsState()

                FriendsScreen(
                    modifier = Modifier.padding(top = topPadding, bottom = bottomBarPadding),
                    uiState = uiState,
                    onSearchQueryChange = friendsViewModel::onSearchQueryChanged,
                    onOpenAddFriend = friendsViewModel::openAddFriendDialog,
                    onOpenRequestsClick = { bottomNavController.navigate(FriendRequestsRoute) },
                    onCloseAddFriend = friendsViewModel::closeAddFriendDialog,
                    onAddFriendConfirm = friendsViewModel::addFriend,
                    onFriendClick = { friendId -> bottomNavController.navigate(FriendDetailRoute(friendId)) }
                )
            }

            // Friend Requests Screen
            composable<FriendRequestsRoute>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200, easing = FastOutSlowInEasing)) }
            ) {
                val friendsViewModel: FriendsViewModel = provideViewModel {
                    FriendsViewModel(friendsRepository)
                }
                val uiState by friendsViewModel.uiState.collectAsState()

                FriendRequestsScreen(
                    requests = uiState.friendRequests,
                    onAccept = friendsViewModel::acceptRequest,
                    onReject = friendsViewModel::rejectRequest,
                    onBackClick = { bottomNavController.popBackStack() }
                )
            }

            // Friend Detail Screen
            composable<FriendDetailRoute>(
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(220, easing = FastOutSlowInEasing)) },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(200, easing = FastOutSlowInEasing)) }
            ) { backStackEntry ->
                val route: FriendDetailRoute = backStackEntry.toRoute()
                val friendsViewModel: FriendsViewModel = provideViewModel {
                    FriendsViewModel(friendsRepository)
                }

                var friend by remember { mutableStateOf<Friend?>(null) }
                var isLoadingFriend by remember { mutableStateOf(true) }

                LaunchedEffect(route.friendId) {
                    isLoadingFriend = true
                    friend = friendsRepository.getFriendById(route.friendId)
                    isLoadingFriend = false
                }

                if (isLoadingFriend) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    friend?.let { currentFriend ->
                        FriendDetailScreen(
                            friend = currentFriend,
                            onBackClick = { bottomNavController.popBackStack() },
                            onRemoveFriend = { friendId ->
                                friendsViewModel.removeFriend(friendId) {
                                    bottomNavController.popBackStack()
                                }
                            },
                            onGameClick = { gameId -> bottomNavController.navigate(GameDetailRoute(gameId)) }
                        )
                    } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Impossibile caricare il profilo dell'amico.", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Settings Tab
            composable<SettingsRoute> {
                val settingsViewModel: SettingsViewModel = provideViewModel {
                    SettingsViewModel(settingsRepository)
                }
                val uiState by settingsViewModel.uiState.collectAsState()

                SettingsScreen(
                    modifier = Modifier.padding(top = topPadding, bottom = bottomBarPadding),
                    uiState = uiState,
                    onThemeChange = settingsViewModel::updateTheme,
                    onAccentColorChange = settingsViewModel::updateAccentColor,
                    onStatsPrivacyChange = settingsViewModel::updateStatsPrivacy,
                    onBadgesPrivacyChange = settingsViewModel::updateBadgesPrivacy,
                    onLibraryPrivacyChange = settingsViewModel::updateLibraryPrivacy,
                    onSaveClick = settingsViewModel::saveAllSettings,
                    onRevertSettings = settingsViewModel::revertSettings,
                    onDismissBanner = settingsViewModel::dismissSuccessBanner,
                    onLogoutClick = {
                        settingsRepository.resetOnLogout()
                        onLogout()
                    }
                )
            }
        }
    }
}