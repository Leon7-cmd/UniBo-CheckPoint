package com.example.checkpoint.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.checkpoint.ui.auth.AuthScreen
import com.example.checkpoint.ui.auth.AuthUiState
import com.example.checkpoint.ui.auth.AuthViewModel
import kotlinx.serialization.Serializable

@Serializable object AuthRoute
@Serializable object MainAppRoute

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthRoute
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
            Text("Benvenuto in CHECK POINT!")
        }
    }
}