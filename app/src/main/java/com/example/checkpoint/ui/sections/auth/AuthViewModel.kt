package com.example.checkpoint.ui.sections.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Compila tutti i campi")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.signInWithEmailAndPassword(email.trim(), pass).await()
                val uid = result.user?.uid ?: ""
                _uiState.value = AuthUiState.LoginSuccess(userId = uid)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseError(e))
            }
        }
    }

    fun register(username: String, email: String, pass: String, confirmPass: String) {
        if (username.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Tutti i campi sono obbligatori")
            return
        }
        if (pass != confirmPass) {
            _uiState.value = AuthUiState.Error("Le password non coincidono")
            return
        }
        if (pass.length < 6) {
            _uiState.value = AuthUiState.Error("La password deve avere almeno 6 caratteri")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), pass).await()
                val uid = result.user?.uid ?: ""

                // TODO: Salvare lo username e il Friend Code su Firestore
                _uiState.value = AuthUiState.RegisterSuccess("Registrazione avvenuta con successo")
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(mapFirebaseError(e))
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    // Function to translate Firebase exceptions
    private fun mapFirebaseError(e: Exception): String {
        return when (e) {
            is FirebaseAuthUserCollisionException ->
                "Questa email è già registrata. Prova ad accedere."
            is FirebaseAuthInvalidCredentialsException ->
                "Email o password errate. Riprova."
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_INVALID_EMAIL" -> "Formato email non valido."
                    "ERROR_USER_NOT_FOUND" -> "Nessun account trovato con questa email."
                    "ERROR_WRONG_PASSWORD" -> "Password errata."
                    "ERROR_WEAK_PASSWORD" -> "La password è troppo debole."
                    "ERROR_EMAIL_ALREADY_IN_USE" -> "Email già registrata."
                    "ERROR_USER_DISABLED" -> "Questo account è stato disabilitato."
                    "ERROR_TOO_MANY_REQUESTS" -> "Troppi tentativi falliti. Riprova più tardi."
                    "ERROR_NETWORK_REQUEST_FAILED" -> "Connessione internet assente o instabile."
                    else -> "Si è verificato un errore. Riprova."
                }
            }
            else -> "Impossibile completare l'operazione. Controlla la connessione."
        }
    }
}