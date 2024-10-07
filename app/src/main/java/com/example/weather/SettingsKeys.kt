package com.example.weather

import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val TEMPERATURE_UNIT_KEY = intPreferencesKey("temperature_unit")
    val UPDATE_FREQUENCY_KEY = stringPreferencesKey("update_frequency")
}