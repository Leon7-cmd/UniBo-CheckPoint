package com.example.checkpoint.ui.sections.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.checkpoint.data.model.AppAccentColor
import com.example.checkpoint.data.model.AppSettings
import com.example.checkpoint.data.model.AppThemeMode
import com.example.checkpoint.data.model.PrivacyLevel
import com.example.checkpoint.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing application settings state, preview alterations, and revert workflows.
 */
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private var persistedSettings: AppSettings = settingsRepository.loadCachedSettings()

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            settings = persistedSettings,
            hasUnsavedChanges = false
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Sync user settings from cloud on initialization
        viewModelScope.launch(Dispatchers.IO) {
            val synced = settingsRepository.syncSettingsFromCloud()
            persistedSettings = synced
            _uiState.update {
                it.copy(
                    settings = synced,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    // Update theme mode and trigger instant preview
    fun updateTheme(themeMode: AppThemeMode) {
        updateDraftSettings { it.copy(themeMode = themeMode) }
        settingsRepository.applyThemePreview(themeMode)
    }

    // Update accent color and trigger instant preview
    fun updateAccentColor(accentColor: AppAccentColor) {
        updateDraftSettings { it.copy(accentColor = accentColor) }
        settingsRepository.applyAccentPreview(accentColor)
    }

    fun updateStatsPrivacy(level: PrivacyLevel) {
        updateDraftSettings { it.copy(showStatsPrivacy = level) }
    }

    fun updateBadgesPrivacy(level: PrivacyLevel) {
        updateDraftSettings { it.copy(showBadgesPrivacy = level) }
    }

    fun updateLibraryPrivacy(level: PrivacyLevel) {
        updateDraftSettings { it.copy(showLibraryPrivacy = level) }
    }

    private fun updateDraftSettings(transform: (AppSettings) -> AppSettings) {
        _uiState.update { state ->
            val updated = transform(state.settings)
            state.copy(
                settings = updated,
                hasUnsavedChanges = (updated != persistedSettings)
            )
        }
    }

    // Rollback unpersisted changes to the last confirmed state
    fun revertSettings() {
        if (!_uiState.value.hasUnsavedChanges) return

        _uiState.update {
            it.copy(
                settings = persistedSettings,
                hasUnsavedChanges = false
            )
        }
        // Restore theme and accent previews to saved state
        settingsRepository.applyThemePreview(persistedSettings.themeMode)
        settingsRepository.applyAccentPreview(persistedSettings.accentColor)
    }

    // Persist all modified settings locally and to cloud
    fun saveAllSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isSaving = true) }
            val currentSettings = _uiState.value.settings
            settingsRepository.saveSettings(currentSettings)
            persistedSettings = currentSettings
            _uiState.update {
                it.copy(
                    isSaving = false,
                    showSuccessBanner = true,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    fun dismissSuccessBanner() {
        _uiState.update { it.copy(showSuccessBanner = false) }
    }

    override fun onCleared() {
        super.onCleared()
        revertSettings()
    }
}