package com.example.checkpoint.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

enum class AuthTab { LOGIN, REGISTER }

@Composable
fun AuthScreen(
    uiState: AuthUiState,
    onLoginClick: (email: String, pass: String) -> Unit,
    onRegisterClick: (username: String, email: String, pass: String, confirmPass: String) -> Unit,
    onTabSelected: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Fields Form Login
    var loginEmail by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }

    // Fields Form Register
    var regUsername by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.RegisterSuccess) {
            regUsername = ""
            regEmail = ""
            regPassword = ""
            regConfirmPassword = ""
            selectedTab = AuthTab.LOGIN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CHECK POINT",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ElevatedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    Tab(
                        selected = selectedTab == AuthTab.LOGIN,
                        onClick = {
                            if (selectedTab != AuthTab.LOGIN) {
                                selectedTab = AuthTab.LOGIN
                                onTabSelected()
                            }
                        },
                        text = { Text("LOGIN") }
                    )
                    Tab(
                        selected = selectedTab == AuthTab.REGISTER,
                        onClick = {
                            if (selectedTab != AuthTab.REGISTER) {
                                selectedTab = AuthTab.REGISTER
                                onTabSelected()
                            }
                        },
                        text = { Text("REGISTRAZIONE") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Switch Tab Ui Elements
                when (selectedTab) {
                    AuthTab.LOGIN -> {
                        OutlinedTextField(
                            value = loginEmail,
                            onValueChange = { loginEmail = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = { onLoginClick(loginEmail, loginPassword) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Accedi")
                            }
                        }
                    }

                    AuthTab.REGISTER -> {
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regConfirmPassword,
                            onValueChange = { regConfirmPassword = it },
                            label = { Text("Conferma Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (uiState is AuthUiState.Loading) {
                            CircularProgressIndicator()
                        } else {
                            Button(
                                onClick = {
                                    onRegisterClick(regUsername, regEmail, regPassword, regConfirmPassword)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Registrati")
                            }
                        }
                    }
                }

                // Error Message
                if (uiState is AuthUiState.Error) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Success Message
                if (uiState is AuthUiState.RegisterSuccess) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}