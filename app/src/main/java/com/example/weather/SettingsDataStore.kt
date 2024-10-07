package com.example.weather

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "forecast_settings")

val TEMPERATURE_UNIT_KEY = stringPreferencesKey("temperature_unit")
val UPDATE_FREQUENCY_KEY = intPreferencesKey("update_frequency")

class SettingsDataStore(private val context: Context) {
    val temperatureUnit: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TEMPERATURE_UNIT_KEY] ?: "°C"
    }
    val updateFrequency: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[UPDATE_FREQUENCY_KEY] ?: 1
    }

    suspend fun saveTemperatureUnit(temperatureUnit: String) {
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT_KEY] = temperatureUnit
        }
    }

    suspend fun saveUpdateFrequency(updateFrequency: Int) {
        Log.d(TAG, "saveUpdateFrequency(updateFrequency: Int); value: $updateFrequency")
        context.dataStore.edit { preferences ->
            preferences[UPDATE_FREQUENCY_KEY] = updateFrequency
        }
    }

    companion object{
        private const val TAG = "SettingsDataStore"
    }

}