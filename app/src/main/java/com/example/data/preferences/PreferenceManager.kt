package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.settingsDataStore by preferencesDataStore(name = "weather_calendar_settings")

class PreferenceManager(private val context: Context) {

    companion object {
        private val KEY_PRIMARY_CITY = stringPreferencesKey("primary_city")
        private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        private val KEY_NOTIFF_ENABLED = booleanPreferencesKey("notiff_enabled")
        private val KEY_WIDGET_REFRESH_INTERVAL = stringPreferencesKey("widget_refresh")
        private val KEY_AI_INSIGHT_CACHE = stringPreferencesKey("ai_insight_cache")

        const val DEFAULT_CITY = "Lahore"
        const val UNIT_CELSIUS = "C"
        const val UNIT_FAHRENHEIT = "F"
    }

    val primaryCity: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_PRIMARY_CITY] ?: DEFAULT_CITY
    }

    val tempUnit: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_TEMP_UNIT] ?: UNIT_CELSIUS
    }

    val notificationsEnabled: Flow<Boolean> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_NOTIFF_ENABLED] ?: true
    }

    val widgetInterval: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_WIDGET_REFRESH_INTERVAL] ?: "30 Minutes"
    }

    val cachedAiInsight: Flow<String> = context.settingsDataStore.data.map { preferences ->
        preferences[KEY_AI_INSIGHT_CACHE] ?: "Click 'Scan with Gemini AI' to load predictive local insights."
    }

    suspend fun setPrimaryCity(city: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_PRIMARY_CITY] = city
        }
    }

    suspend fun setTempUnit(unit: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_TEMP_UNIT] = unit
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_NOTIFF_ENABLED] = enabled
        }
    }

    suspend fun setWidgetInterval(interval: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_WIDGET_REFRESH_INTERVAL] = interval
        }
    }

    suspend fun cacheAiInsight(insight: String) {
        context.settingsDataStore.edit { preferences ->
            preferences[KEY_AI_INSIGHT_CACHE] = insight
        }
    }
}
