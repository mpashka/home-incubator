package com.receipt.scanner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val backendUrlKey = stringPreferencesKey("backend_url")
    private val userIdKey = intPreferencesKey("user_id")

    val backendUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[backendUrlKey] ?: "http://pwn.gp23.pasha-home.ru:8080/"
    }

    val userId: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[userIdKey] ?: 1
    }

    suspend fun getBackendUrl(): String {
        return backendUrl.first()
        // return context.dataStore.data.first()[backendUrlKey] ?: ""
    }

    suspend fun getUserId(): Int {
        return userId.first()
        // return context.dataStore.data.first()[userIdKey] ?: 1
    }

    suspend fun setBackendUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[backendUrlKey] = url
        }
    }

    suspend fun setUserId(id: Int) {
        context.dataStore.edit { preferences ->
            preferences[userIdKey] = id
        }
    }
}
