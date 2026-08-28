package com.example.checkpoint.ui.sections.settings

import androidx.compose.runtime.Immutable
import com.example.checkpoint.data.model.AppSettings

/**
 * UI State representation for the Settings screen.
 */
@Immutable
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isSaving: Boolean = false,
    val showSuccessBanner: Boolean = false,
    val hasUnsavedChanges: Boolean = false
)