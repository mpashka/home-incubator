package com.receipt.scanner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.historyDataStore: DataStore<Preferences> by preferencesDataStore(name = "scan_history")

class ScanHistoryRepository(private val context: Context) {

    private val historyKey = stringPreferencesKey("history")
    private val gson = Gson()

    val history: Flow<List<ScanRecord>> = context.historyDataStore.data.map { preferences ->
        val json = preferences[historyKey] ?: "[]"
        val type = object : TypeToken<List<ScanRecord>>() {}.type
        gson.fromJson(json, type)
    }

    suspend fun addRecord(record: ScanRecord) {
        context.historyDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val type = object : TypeToken<MutableList<ScanRecord>>() {}.type
            val list: MutableList<ScanRecord> = gson.fromJson(currentJson, type)
            list.add(0, record)
            // Keep only last 100 records
            if (list.size > 100) {
                list.removeAt(list.size - 1)
            }
            preferences[historyKey] = gson.toJson(list)
        }
    }

    suspend fun updateRecord(id: Int, status: String, receiptId: Int?) {
        context.historyDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val type = object : TypeToken<MutableList<ScanRecord>>() {}.type
            val list: MutableList<ScanRecord> = gson.fromJson(currentJson, type)
            val index = list.indexOfFirst { it.id == id }
            if (index != -1) {
                list[index] = list[index].copy(status = status, receiptId = receiptId)
                preferences[historyKey] = gson.toJson(list)
            }
        }
    }

    suspend fun deleteRecord(id: Int) {
        context.historyDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val type = object : TypeToken<MutableList<ScanRecord>>() {}.type
            val list: MutableList<ScanRecord> = gson.fromJson(currentJson, type)
            list.removeAll { it.id == id }
            preferences[historyKey] = gson.toJson(list)
        }
    }
}
