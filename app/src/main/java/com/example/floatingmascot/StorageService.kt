package com.example.floatingmascot

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class StorageService() {


    companion object{
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    }

    val COLD_TEMPERATURE = intPreferencesKey("cold_temperature")
    val HOT_TEMPERATURE = intPreferencesKey("hot_temperature")
    val HIGH_LX = intPreferencesKey("high_lx")
    val LOW_LX = intPreferencesKey("low_lx")
    val START_LUNCH_TIME = intPreferencesKey("start_lunch_time")
    val END_LUNCH_TIME = intPreferencesKey("end_lunch_time")
    val START_SLEEP_TIME = intPreferencesKey("start_sleep_time")
    val END_SLEEP_TIME = intPreferencesKey("end_sleep_time")

    suspend fun getStorageValue(context:Context, key:Preferences.Key<Int>):Int{
        val value: Flow<Int> = context.dataStore.data.map { preferences ->
            preferences[key] ?: 0
        }
        return value.first()
    }

    suspend fun storageValue(context: Context, key:Preferences.Key<Int>, value:Int) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }
}