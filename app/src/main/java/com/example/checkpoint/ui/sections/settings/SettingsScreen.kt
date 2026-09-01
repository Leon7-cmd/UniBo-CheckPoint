package com.example.checkpoint.ui.sections.settings

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.checkpoint.data.model.AppAccentColor
import com.example.checkpoint.data.model.AppThemeMode
import com.example.checkpoint.data.model.PrivacyLevel
import com.example.checkpoint.ui.sections.settings.components.GeneralSettingsCard
import com.example.checkpoint.ui.sections.settings.components.PrivacySettingsCard
import com.example.checkpoint.ui.sections.settings.components.SettingsFeedbackBanner
import kotlinx.coroutines.delay

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    uiState: SettingsUiState,
    onThemeChange: (AppThemeMode) -> Unit,
    onAccentColorChange: (AppAccentColor) -> Unit,
    onStatsPrivacyChange: (PrivacyLevel) -> Unit,
    onBadgesPrivacyChange: (PrivacyLevel) -> Unit,
    onLibraryPrivacyChange: (PrivacyLevel) -> Unit,
    onSaveClick: () -> Unit,
    onRevertSettings: () -> Unit,
    onDismissBanner: () -> Unit,
    onLogoutClick: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Auto-revert settings if screen is disposed with uncommitted changes
    DisposableEffect(Unit) {
        onDispose {
            onRevertSettings()
        }
    }

    // Intercept system back gestures when unpersisted changes exist
    BackHandler(enabled = uiState.hasUnsavedChanges && onNavigateBack != null) {
        showDiscardDialog = true
    }

    // Auto-dismiss the success banner after 3 seconds
    LaunchedEffect(uiState.showSuccessBanner) {
        if (uiState.showSuccessBanner) {
            delay(3000)
            onDismissBanner()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main scrollable settings content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Theme & Color settings
            GeneralSettingsCard(
                currentTheme = uiState.settings.themeMode,
                currentAccent = uiState.settings.accentColor,
                onThemeChange = onThemeChange,
                onAccentColorChange = onAccentColorChange
            )

            // Privacy settings
            PrivacySettingsCard(
                settings = uiState.settings,
                onStatsPrivacyChange = onStatsPrivacyChange,
                onBadgesPrivacyChange = onBadgesPrivacyChange,
                onLibraryPrivacyChange = onLibraryPrivacyChange
            )
        }

        // Bottom bar for feedback banner and action buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated success notification banner
            AnimatedVisibility(
                visible = uiState.showSuccessBanner,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                SettingsFeedbackBanner(onDismiss = onDismissBanner)
            }

            // Save and Logout actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onSaveClick,
                    enabled = !uiState.isSaving && uiState.hasUnsavedChanges,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SALVA")
                    }
                }

                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("LOGOUT")
                }
            }
        }
    }

    // Confirmation dialog on exit attempt with pending changes
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Modifiche non salvate") },
            text = { Text("Vuoi uscire senza salvare? Le impostazioni modificate verranno ripristinate.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRevertSettings()
                        onNavigateBack?.invoke()
                    }
                ) {
                    Text("Scarta e esci", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { }) {
                    Text("Continua a modificare")
                }
            }
        )
    }
}