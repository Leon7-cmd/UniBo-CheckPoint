package com.example.checkpoint.ui.sections.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

/**
 * Utility mapper translating Firebase Authentication exceptions into user-friendly localized messages.
 */
object FirebaseAuthErrors {

    /**
     * Maps an authentication exception to a readable error string.
     */
    fun parse(e: Exception): String {
        return when (e) {
            is FirebaseAuthUserCollisionException ->
                "Questa email è già registrata. Prova ad accedere."

            is FirebaseAuthWeakPasswordException ->
                "La password è troppo debole. Usa almeno 6 caratteri."

            is FirebaseAuthInvalidUserException ->
                "Nessun account associato a questa email o account disabilitato."

            is FirebaseAuthInvalidCredentialsException ->
                "Email o password errate. Riprova."

            is FirebaseNetworkException ->
                "Connessione internet assente o instabile."

            is FirebaseAuthException -> when (e.errorCode) {
                "ERROR_INVALID_EMAIL", "ERROR_INVALID_CREDENTIAL" -> "Credenziali o formato email non valido."
                "ERROR_USER_NOT_FOUND" -> "Nessun account trovato con questa email."
                "ERROR_WRONG_PASSWORD" -> "Password errata."
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email già registrata."
                "ERROR_USER_DISABLED" -> "Questo account è stato disabilitato."
                "ERROR_TOO_MANY_REQUESTS" -> "Troppi tentativi falliti. Riprova tra qualche minuto."
                "ERROR_NETWORK_REQUEST_FAILED" -> "Errore di rete. Controlla la connessione."
                else -> e.localizedMessage ?: "Si è verificato un errore durante l'autenticazione."
            }

            else -> e.localizedMessage ?: "Impossibile completare l'operazione. Riprova."
        }
    }
}