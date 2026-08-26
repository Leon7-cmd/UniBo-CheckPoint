package com.example.checkpoint.ui.sections.auth

import androidx.compose.runtime.Immutable

/**
 * UI State representation for authentication flows (login, registration, loading, and error handling).
 */
@Immutable
sealed interface AuthUiState {
    @Immutable
    data object Idle : AuthUiState

    @Immutable
    data object Loading : AuthUiState

    @Immutable
    data class LoginSuccess(val userId: String) : AuthUiState

    @Immutable
    data class RegisterSuccess(val message: String) : AuthUiState

    @Immutable
    data class Error(val message: String) : AuthUiState
}