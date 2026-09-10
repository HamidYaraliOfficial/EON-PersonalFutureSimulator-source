package com.eon.futuresimulator.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eon.futuresimulator.core.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "eon_preferences")

data class AppLanguage(val tag: String, val displayName: String)

object SupportedLanguages {
    val ENGLISH = AppLanguage("en", "English")
    val PERSIAN = AppLanguage("fa", "فارسی")
    val CHINESE = AppLanguage("zh-CN", "中文")
    val ALL = listOf(ENGLISH, PERSIAN, CHINESE)
}

/**
 * Every knob that changes *how EON behaves* rather than *what the user's life data is*
 * lives here: theme, language, and every Privacy Center consent switch (Calendar
 * access, AI access, Cloud sync, retention policy). All Local-First — DataStore only,
 * nothing ever leaves the device unless [cloudAiConsent] or [cloudSyncConsent] is true.
 */
@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color_allowed")
        val LANGUAGE_TAG = stringPreferencesKey("language_tag")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")

        // Privacy Center
        val CALENDAR_ACCESS_CONSENT = booleanPreferencesKey("consent_calendar_access")
        val ACTIVITY_DATA_CONSENT = booleanPreferencesKey("consent_activity_data")
        val HABIT_DATA_CONSENT = booleanPreferencesKey("consent_habit_data")
        val USAGE_DATA_CONSENT = booleanPreferencesKey("consent_usage_data")
        val CLOUD_AI_CONSENT = booleanPreferencesKey("consent_cloud_ai")
        val CLOUD_SYNC_CONSENT = booleanPreferencesKey("consent_cloud_sync")
        val BIOMETRIC_LOCK_ENABLED = booleanPreferencesKey("biometric_lock_enabled")
        val RETENTION_DAYS = intPreferencesKey("retention_days")

        // Auto-Adapt vs Manual Control (Adaptive Plan Engine)
        val AUTO_ADAPT_ENABLED = booleanPreferencesKey("auto_adapt_enabled")

        // Device Capability override (0 = auto)
        val MONTE_CARLO_ITERATION_CAP = intPreferencesKey("monte_carlo_iteration_cap")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.fromStorageKey(it[Keys.THEME_MODE])
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val dynamicColorAllowed: Flow<Boolean> = context.dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: false }
    suspend fun setDynamicColorAllowed(allowed: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = allowed }
    }

    val languageTag: Flow<String> = context.dataStore.data.map { it[Keys.LANGUAGE_TAG] ?: "en" }
    suspend fun setLanguageTag(tag: String) {
        context.dataStore.edit { it[Keys.LANGUAGE_TAG] = tag }
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }

    val calendarAccessConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.CALENDAR_ACCESS_CONSENT] ?: false }
    val activityDataConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.ACTIVITY_DATA_CONSENT] ?: true }
    val habitDataConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.HABIT_DATA_CONSENT] ?: true }
    val usageDataConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.USAGE_DATA_CONSENT] ?: true }
    val cloudAiConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.CLOUD_AI_CONSENT] ?: false }
    val cloudSyncConsent: Flow<Boolean> = context.dataStore.data.map { it[Keys.CLOUD_SYNC_CONSENT] ?: false }
    val biometricLockEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.BIOMETRIC_LOCK_ENABLED] ?: false }
    val retentionDays: Flow<Int> = context.dataStore.data.map { it[Keys.RETENTION_DAYS] ?: 365 }
    val autoAdaptEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.AUTO_ADAPT_ENABLED] ?: true }
    val monteCarloIterationCap: Flow<Int> = context.dataStore.data.map { it[Keys.MONTE_CARLO_ITERATION_CAP] ?: 0 }

    suspend fun setCalendarAccessConsent(v: Boolean) = context.dataStore.edit { it[Keys.CALENDAR_ACCESS_CONSENT] = v }
    suspend fun setActivityDataConsent(v: Boolean) = context.dataStore.edit { it[Keys.ACTIVITY_DATA_CONSENT] = v }
    suspend fun setHabitDataConsent(v: Boolean) = context.dataStore.edit { it[Keys.HABIT_DATA_CONSENT] = v }
    suspend fun setUsageDataConsent(v: Boolean) = context.dataStore.edit { it[Keys.USAGE_DATA_CONSENT] = v }
    suspend fun setCloudAiConsent(v: Boolean) = context.dataStore.edit { it[Keys.CLOUD_AI_CONSENT] = v }
    suspend fun setCloudSyncConsent(v: Boolean) = context.dataStore.edit { it[Keys.CLOUD_SYNC_CONSENT] = v }
    suspend fun setBiometricLockEnabled(v: Boolean) = context.dataStore.edit { it[Keys.BIOMETRIC_LOCK_ENABLED] = v }
    suspend fun setRetentionDays(days: Int) = context.dataStore.edit { it[Keys.RETENTION_DAYS] = days }
    suspend fun setAutoAdaptEnabled(v: Boolean) = context.dataStore.edit { it[Keys.AUTO_ADAPT_ENABLED] = v }
    suspend fun setMonteCarloIterationCap(cap: Int) = context.dataStore.edit { it[Keys.MONTE_CARLO_ITERATION_CAP] = cap }
}
