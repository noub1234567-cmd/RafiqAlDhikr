package com.rafiqaldhikr.app.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rafiqaldhikr.app.core.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rafiq_preferences")

@Serializable
data class DashboardConfig(
    val showProgressSummary: Boolean = true,
    val showTodayActivities: Boolean = true,
    val showPendingMissed: Boolean = true,
    val showUpcomingReminders: Boolean = true,
    val showDailySuggestion: Boolean = true,
    val showWeeklyProgress: Boolean = true,
    val showQuickAccess: Boolean = true
)

@Serializable
data class AppSettingsSnapshot(
    val activeProfileId: Long = 1L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hijriOffsetDays: Int = 0,
    val tasbeehHapticEnabled: Boolean = true
)

class UserPreferencesDataStore(private val context: Context) {

    private object Keys {
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HIJRI_OFFSET_DAYS = intPreferencesKey("hijri_offset_days")
        val TASBEEH_HAPTIC = booleanPreferencesKey("tasbeeh_haptic")
    }

    private fun dashKey(profileId: Long, card: String) =
        booleanPreferencesKey("dash_${profileId}_$card")

    val settingsFlow: Flow<AppSettingsSnapshot> = context.dataStore.data.map { prefs ->
        val themeStr = prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val themeMode = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)
        AppSettingsSnapshot(
            activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: -1L,
            themeMode = themeMode,
            hijriOffsetDays = (prefs[Keys.HIJRI_OFFSET_DAYS] ?: 0).coerceIn(-2, 2),
            tasbeehHapticEnabled = prefs[Keys.TASBEEH_HAPTIC] ?: true
        )
    }

    fun observeDashboardConfig(profileId: Long): Flow<DashboardConfig> =
        context.dataStore.data.map { prefs ->
            DashboardConfig(
                showProgressSummary = prefs[dashKey(profileId, "progress")] ?: true,
                showTodayActivities = prefs[dashKey(profileId, "today")] ?: true,
                showPendingMissed = prefs[dashKey(profileId, "pending")] ?: true,
                showUpcomingReminders = prefs[dashKey(profileId, "reminders")] ?: true,
                showDailySuggestion = prefs[dashKey(profileId, "suggestion")] ?: true,
                showWeeklyProgress = prefs[dashKey(profileId, "weekly")] ?: true,
                showQuickAccess = prefs[dashKey(profileId, "quick")] ?: true
            )
        }

    suspend fun getSnapshot(): AppSettingsSnapshot = settingsFlow.first()

    suspend fun setActiveProfileId(profileId: Long) {
        context.dataStore.edit { it[Keys.ACTIVE_PROFILE_ID] = profileId }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setHijriOffsetDays(offsetDays: Int) {
        context.dataStore.edit { it[Keys.HIJRI_OFFSET_DAYS] = offsetDays.coerceIn(-2, 2) }
    }

    suspend fun setTasbeehHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TASBEEH_HAPTIC] = enabled }
    }

    suspend fun updateDashboardConfig(profileId: Long, config: DashboardConfig) {
        context.dataStore.edit { prefs ->
            prefs[dashKey(profileId, "progress")] = config.showProgressSummary
            prefs[dashKey(profileId, "today")] = config.showTodayActivities
            prefs[dashKey(profileId, "pending")] = config.showPendingMissed
            prefs[dashKey(profileId, "reminders")] = config.showUpcomingReminders
            prefs[dashKey(profileId, "suggestion")] = config.showDailySuggestion
            prefs[dashKey(profileId, "weekly")] = config.showWeeklyProgress
            prefs[dashKey(profileId, "quick")] = config.showQuickAccess
        }
    }
}
