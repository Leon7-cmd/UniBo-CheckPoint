package com.example.checkpoint.ui.sections.auth

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class LoginSuccess(val userId: String) : AuthUiState
    data class RegisterSuccess(val message: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}