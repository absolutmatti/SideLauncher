package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "launcher_preferences")

data class LauncherSettings(
    val startPage: String = "HOME", // "HOME", "APPS", "WATCHLIST"
    val timeFormat: String = "SYSTEM", // "SYSTEM", "12H", "24H"
    val accentColorHex: String = "#6C8CFF",
    val backgroundType: String = "SOLID", // "SOLID", "GRADIENT", "DYNAMIC"
    val autoCollapseSidebar: Boolean = true,
    val appColumns: Int = 4,
    val appSortMode: String = "AZ", // "AZ", "LAST_USED", "CUSTOM"
    val tmdbLanguage: String = "de-DE",
    val providerRegion: String = "DE"
)

class PreferencesRepository(private val context: Context) {
    private val dataStore = context.dataStore

    private object Keys {
        val START_PAGE = stringPreferencesKey("start_page")
        val TIME_FORMAT = stringPreferencesKey("time_format")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val BACKGROUND_TYPE = stringPreferencesKey("background_type")
        val AUTO_COLLAPSE_SIDEBAR = booleanPreferencesKey("auto_collapse_sidebar")
        val APP_COLUMNS = intPreferencesKey("app_columns")
        val APP_SORT_MODE = stringPreferencesKey("app_sort_mode")
        val TMDB_LANGUAGE = stringPreferencesKey("tmdb_language")
        val PROVIDER_REGION = stringPreferencesKey("provider_region")
    }

    val settingsFlow: Flow<LauncherSettings> = dataStore.data.map { prefs ->
        LauncherSettings(
            startPage = prefs[Keys.START_PAGE] ?: "HOME",
            timeFormat = prefs[Keys.TIME_FORMAT] ?: "SYSTEM",
            accentColorHex = prefs[Keys.ACCENT_COLOR_HEX] ?: "#6C8CFF",
            backgroundType = prefs[Keys.BACKGROUND_TYPE] ?: "SOLID",
            autoCollapseSidebar = prefs[Keys.AUTO_COLLAPSE_SIDEBAR] ?: true,
            appColumns = prefs[Keys.APP_COLUMNS] ?: 4,
            appSortMode = prefs[Keys.APP_SORT_MODE] ?: "AZ",
            tmdbLanguage = prefs[Keys.TMDB_LANGUAGE] ?: "de-DE",
            providerRegion = prefs[Keys.PROVIDER_REGION] ?: "DE"
        )
    }

    suspend fun updateStartPage(page: String) = dataStore.edit { it[Keys.START_PAGE] = page }
    suspend fun updateTimeFormat(format: String) = dataStore.edit { it[Keys.TIME_FORMAT] = format }
    suspend fun updateAccentColor(hex: String) = dataStore.edit { it[Keys.ACCENT_COLOR_HEX] = hex }
    suspend fun updateBackgroundType(type: String) = dataStore.edit { it[Keys.BACKGROUND_TYPE] = type }
    suspend fun updateAutoCollapseSidebar(enabled: Boolean) = dataStore.edit { it[Keys.AUTO_COLLAPSE_SIDEBAR] = enabled }
    suspend fun updateAppColumns(cols: Int) = dataStore.edit { it[Keys.APP_COLUMNS] = cols }
    suspend fun updateAppSortMode(mode: String) = dataStore.edit { it[Keys.APP_SORT_MODE] = mode }
    suspend fun updateTmdbLanguage(lang: String) = dataStore.edit { it[Keys.TMDB_LANGUAGE] = lang }
    suspend fun updateProviderRegion(region: String) = dataStore.edit { it[Keys.PROVIDER_REGION] = region }
}
