package com.example.checkpoint.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkpoint.ui.main.MainScreen
import com.example.checkpoint.ui.sections.auth.AuthScreen
import com.example.checkpoint.ui.sections.auth.AuthUiState
import com.example.checkpoint.ui.sections.auth.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable

@Serializable object AuthRoute
@Serializable object MainAppRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val auth = remember { FirebaseAuth.getInstance() }

    // 1. Check if user is logged in
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var isLoading by remember { mutableStateOf(true) }

    // Listener to update currentUser and isLoading
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
            isLoading = false
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    // 2. Shows a loading screen while the user is being checked
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 3. Set the start destination based on the current user
    val startDestination = if (currentUser != null) MainAppRoute else AuthRoute

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<AuthRoute> {
            val viewModel: AuthViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState) {
                if (uiState is AuthUiState.LoginSuccess) {
                    navController.navigate(MainAppRoute) {
                        popUpTo<AuthRoute> { inclusive = true }
                    }
                }
            }

            AuthScreen(
                uiState = uiState,
                onLoginClick = { email, pass ->
                    viewModel.login(email, pass)
                },
                onRegisterClick = { username, email, pass, confirmPass ->
                    viewModel.register(username, email, pass, confirmPass)
                },
                onTabSelected = { viewModel.resetState() }
            )
        }

        composable<MainAppRoute> {
            MainScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate(AuthRoute) {
                        popUpTo<MainAppRoute> { inclusive = true }
                    }
                }
            )
        }
    }
}