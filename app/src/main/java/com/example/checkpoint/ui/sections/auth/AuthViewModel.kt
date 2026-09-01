package com.example.checkpoint.ui.sections.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.repository.UserProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // Sign in with email and password
    fun login(email: String, pass: String) {
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank() || pass.isBlank()) {
            _uiState.update { AuthUiState.Error("Compila tutti i campi.") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { AuthUiState.Error("Inserisci un indirizzo email valido.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            try {
                val result = auth.signInWithEmailAndPassword(cleanEmail, pass).await()
                val uid = result.user?.uid.orEmpty()
                _uiState.update { AuthUiState.LoginSuccess(userId = uid) }
            } catch (e: Exception) {
                _uiState.update { AuthUiState.Error(FirebaseAuthErrors.parse(e)) }
            }
        }
    }

    // Register a new user account with profile initialization
    fun register(username: String, email: String, pass: String, confirmPass: String) {
        val cleanUsername = username.trim()
        val cleanEmail = email.trim()

        if (cleanUsername.isBlank() || cleanEmail.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _uiState.update { AuthUiState.Error("Tutti i campi sono obbligatori.") }
            return
        }
        if (cleanUsername.length < 3) {
            _uiState.update { AuthUiState.Error("Il nome utente deve contenere almeno 3 caratteri.") }
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            _uiState.update { AuthUiState.Error("Inserisci un formato email valido.") }
            return
        }
        if (pass.length < 6) {
            _uiState.update { AuthUiState.Error("La password deve avere almeno 6 caratteri.") }
            return
        }
        if (pass != confirmPass) {
            _uiState.update { AuthUiState.Error("Le due password non coincidono.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            try {
                // 1. Create Firebase Auth user
                val result = auth.createUserWithEmailAndPassword(cleanEmail, pass).await()
                val user = result.user ?: throw IllegalStateException("Creazione utente fallita.")

                // 2. Initialize Firestore profile and local cache
                userProfileRepository.createInitialUserProfile(
                    uid = user.uid,
                    email = cleanEmail,
                    username = cleanUsername
                )

                // 3. Set display name in Firebase Auth user profile
                val profileUpdates = userProfileChangeRequest {
                    displayName = cleanUsername
                }
                user.updateProfile(profileUpdates).await()

                _uiState.update { AuthUiState.RegisterSuccess("Registrazione completata con successo!") }
            } catch (e: Exception) {
                _uiState.update { AuthUiState.Error(FirebaseAuthErrors.parse(e)) }
            }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState.Idle }
    }
}