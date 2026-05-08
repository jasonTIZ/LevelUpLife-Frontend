package com.example.leveluplife.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.debugApiDataStore: DataStore<Preferences> by preferencesDataStore(name = "debug_api_prefs")

class DebugApiPreferences(private val context: Context) {

    private val keyHost = stringPreferencesKey("debug_api_host")
    private val keyScheme = stringPreferencesKey("debug_api_scheme")

    val host: Flow<String?> = context.debugApiDataStore.data.map { it[keyHost] }
    val scheme: Flow<String?> = context.debugApiDataStore.data.map { it[keyScheme] }

    suspend fun setHost(value: String?) {
        context.debugApiDataStore.edit { prefs ->
            if (value.isNullOrBlank()) prefs.remove(keyHost) else prefs[keyHost] = value
        }
    }

    suspend fun setScheme(value: String?) {
        context.debugApiDataStore.edit { prefs ->
            if (value.isNullOrBlank()) prefs.remove(keyScheme) else prefs[keyScheme] = value
        }
    }
}
