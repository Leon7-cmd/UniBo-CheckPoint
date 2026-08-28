package com.example.checkpoint.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.checkpoint.data.model.AppAccentColor
import com.example.checkpoint.data.model.AppSettings
import com.example.checkpoint.data.model.AppThemeMode
import com.example.checkpoint.data.model.PrivacyLevel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Repository managing application theme preferences, dynamic accent colors, privacy toggles, and Firestore syncing.
 */
class SettingsRepository(
    context: Context,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val currentUid: String?
        get() = auth.currentUser?.uid

    init {
        loadCachedSettings()
    }

    // Synchronous local load from SharedPreferences
    fun loadCachedSettings(): AppSettings {
        val uid = currentUid
        val loaded = readSettingsFromPrefs(prefs, uid)
        _settings.update { loaded }
        return loaded
    }

    // Immediate local preview for app theme mode
    fun applyThemePreview(themeMode: AppThemeMode) {
        val uid = currentUid ?: return
        prefs.edit { putString(keyTheme(uid), themeMode.name) }
        _settings.update { it.copy(themeMode = themeMode) }
    }

    // Immediate local preview for accent color
    fun applyAccentPreview(accentColor: AppAccentColor) {
        val uid = currentUid ?: return
        prefs.edit { putString(keyAccent(uid), accentColor.name) }
        _settings.update { it.copy(accentColor = accentColor) }
    }

    // Fetch and apply settings stored in Firestore
    suspend fun syncSettingsFromCloud(): AppSettings = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext loadCachedSettings()

        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val cached = readSettingsFromPrefs(prefs, uid)

                val themeStr = doc.getString("themeMode") ?: cached.themeMode.name
                val accentStr = doc.getString("accentColor") ?: cached.accentColor.name
                val statsPrivacyStr = doc.getString("statsPrivacy") ?: cached.showStatsPrivacy.name
                val badgesPrivacyStr = doc.getString("badgesPrivacy") ?: cached.showBadgesPrivacy.name
                val libraryPrivacyStr = doc.getString("libraryPrivacy") ?: cached.showLibraryPrivacy.name

                val synced = AppSettings(
                    themeMode = parseEnum(themeStr, AppThemeMode.SYSTEM),
                    accentColor = parseEnum(accentStr, AppAccentColor.PURPLE),
                    showStatsPrivacy = parseEnum(statsPrivacyStr, PrivacyLevel.PUBLIC),
                    showBadgesPrivacy = parseEnum(badgesPrivacyStr, PrivacyLevel.PUBLIC),
                    showLibraryPrivacy = parseEnum(libraryPrivacyStr, PrivacyLevel.PUBLIC)
                )

                writeSettingsToPrefs(prefs, uid, synced)
                _settings.update { synced }
                synced
            } else {
                loadCachedSettings()
            }
        } catch (_: Exception) {
            loadCachedSettings()
        }
    }

    // Persist full settings payload locally and to Firestore
    suspend fun saveSettings(newSettings: AppSettings) = withContext(Dispatchers.IO) {
        val uid = currentUid ?: return@withContext

        writeSettingsToPrefs(prefs, uid, newSettings)
        _settings.update { newSettings }

        val cloudData = mapOf(
            "themeMode" to newSettings.themeMode.name,
            "accentColor" to newSettings.accentColor.name,
            "statsPrivacy" to newSettings.showStatsPrivacy.name,
            "badgesPrivacy" to newSettings.showBadgesPrivacy.name,
            "libraryPrivacy" to newSettings.showLibraryPrivacy.name,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            firestore.collection("users")
                .document(uid)
                .set(cloudData, SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Suppress error or log in production
        }
    }

    fun resetOnLogout() {
        _settings.update { AppSettings() }
    }

    companion object {
        private const val PREFS_NAME = "checkpoint_settings_prefs"

        private fun keyTheme(uid: String?) = "setting_theme_${uid.orEmpty()}"
        private fun keyAccent(uid: String?) = "setting_accent_${uid.orEmpty()}"
        private fun keyStatsPrivacy(uid: String?) = "setting_stats_privacy_${uid.orEmpty()}"
        private fun keyBadgesPrivacy(uid: String?) = "setting_badges_privacy_${uid.orEmpty()}"
        private fun keyLibraryPrivacy(uid: String?) = "setting_library_privacy_${uid.orEmpty()}"

        private val _settings = MutableStateFlow(AppSettings())
        val settings: StateFlow<AppSettings> = _settings.asStateFlow()

        // Synchronous early initialization before setContent in MainActivity
        fun init(context: Context) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            _settings.update { readSettingsFromPrefs(prefs, uid) }
        }

        private fun readSettingsFromPrefs(prefs: SharedPreferences, uid: String?): AppSettings {
            val themeStr = prefs.getString(keyTheme(uid), AppThemeMode.SYSTEM.name)
            val accentStr = prefs.getString(keyAccent(uid), AppAccentColor.PURPLE.name)
            val statsPrivacyStr = prefs.getString(keyStatsPrivacy(uid), PrivacyLevel.PUBLIC.name)
            val badgesPrivacyStr = prefs.getString(keyBadgesPrivacy(uid), PrivacyLevel.PUBLIC.name)
            val libraryPrivacyStr = prefs.getString(keyLibraryPrivacy(uid), PrivacyLevel.PUBLIC.name)

            return AppSettings(
                themeMode = parseEnum(themeStr, AppThemeMode.SYSTEM),
                accentColor = parseEnum(accentStr, AppAccentColor.PURPLE),
                showStatsPrivacy = parseEnum(statsPrivacyStr, PrivacyLevel.PUBLIC),
                showBadgesPrivacy = parseEnum(badgesPrivacyStr, PrivacyLevel.PUBLIC),
                showLibraryPrivacy = parseEnum(libraryPrivacyStr, PrivacyLevel.PUBLIC)
            )
        }

        private fun writeSettingsToPrefs(prefs: SharedPreferences, uid: String, settings: AppSettings) {
            prefs.edit {
                putString(keyTheme(uid), settings.themeMode.name)
                putString(keyAccent(uid), settings.accentColor.name)
                putString(keyStatsPrivacy(uid), settings.showStatsPrivacy.name)
                putString(keyBadgesPrivacy(uid), settings.showBadgesPrivacy.name)
                putString(keyLibraryPrivacy(uid), settings.showLibraryPrivacy.name)
            }
        }

        private inline fun <reified T : Enum<T>> parseEnum(value: String?, default: T): T {
            if (value == null) return default
            return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
        }
    }
}