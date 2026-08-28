package com.example.checkpoint.ui.sections.friends.components.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog prompt allowing users to enter a friend code and send a friend request.
 */
@Composable
fun AddFriendDialog(
    isOpen: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    if (!isOpen) return

    var friendCodeInput by remember { mutableStateOf("") }

    // Reset input field when dialog opens
    LaunchedEffect(isOpen) {
        friendCodeInput = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aggiungi Amico", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Inserisci il Codice Amico univoco (es. CKP-1234) per inviare la richiesta:",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = friendCodeInput,
                    onValueChange = { friendCodeInput = it.uppercase() },
                    placeholder = { Text("CKP-XXXX") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(friendCodeInput.trim()) },
                enabled = !isLoading && friendCodeInput.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Invia Richiesta")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Annulla")
            }
        }
    )
}