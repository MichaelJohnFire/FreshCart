package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "freshcart_user_settings")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val code: String, val titleRes: Int) {
    SYSTEM("system", R.string.language_system),
    ENGLISH("en", R.string.language_en),
    SPANISH("es", R.string.language_es),
    POLISH("pl", R.string.language_pl),
    GERMAN("de", R.string.language_de),
    FRENCH("fr", R.string.language_fr);

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: SYSTEM
        }
    }
}

class PreferencesManager(private val context: Context) {

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeName = preferences[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeName)
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        }

    val languageCodeFlow: Flow<String> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_LANGUAGE_CODE] ?: AppLanguage.SYSTEM.code
        }

    suspend fun saveSettings(themeMode: ThemeMode, languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = themeMode.name
            preferences[KEY_LANGUAGE_CODE] = languageCode
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setLanguage(code: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LANGUAGE_CODE] = code
        }
    }

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("key_theme_mode")
        val KEY_LANGUAGE_CODE = stringPreferencesKey("key_language_code")

        @Volatile
        private var INSTANCE: PreferencesManager? = null

        fun getInstance(context: Context): PreferencesManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
