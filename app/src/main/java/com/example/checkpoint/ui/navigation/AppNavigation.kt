package com.example.checkpoint.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkpoint.data.local.AppDatabase
import com.example.checkpoint.data.repository.LocalGameRepository
import com.example.checkpoint.data.repository.UserProfileRepository
import com.example.checkpoint.ui.main.MainScreen
import com.example.checkpoint.ui.sections.auth.AuthScreen
import com.example.checkpoint.ui.sections.auth.AuthUiState
import com.example.checkpoint.ui.sections.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Type-safe root destinations for app-level navigation
@Serializable object AuthRoute
@Serializable object MainAppRoute

/**
 * Root navigation host managing initial auth state resolution, authentication flow, and main app routing.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }
    val appContext = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()

    // Initialize application-scoped repositories
    val database = remember { AppDatabase.getDatabase(appContext) }
    val localGameRepository = remember {
        LocalGameRepository(database.gameDao(), database.achievementDao())
    }
    val userProfileRepository = remember {
        UserProfileRepository(appContext)
    }

    // Auth state listener management
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var isCheckingAuth by remember { mutableStateOf(true) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            isCheckingAuth = false
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // Splash/Loading indicator during initial token resolution
    if (isCheckingAuth) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // Determine initial graph destination
    val startDestination = if (currentUser != null) MainAppRoute else AuthRoute

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Authentication flow
        composable<AuthRoute> {
            val authViewModel: AuthViewModel = viewModel(
                factory = AuthViewModel.Factory(userProfileRepository)
            )
            val uiState by authViewModel.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is AuthUiState.LoginSuccess || uiState is AuthUiState.RegisterSuccess) {
                    navController.navigate(MainAppRoute) {
                        popUpTo<AuthRoute> { inclusive = true }
                    }
                }
            }

            AuthScreen(
                uiState = uiState,
                onLoginClick = authViewModel::login,
                onRegisterClick = authViewModel::register,
                onTabSelected = authViewModel::resetState
            )
        }

        // Main authenticated app flow
        composable<MainAppRoute> {
            MainScreen(
                onLogout = {
                    scope.launch {
                        userProfileRepository.logoutAndClear(localGameRepository)
                        navController.navigate(AuthRoute) {
                            popUpTo<MainAppRoute> { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}