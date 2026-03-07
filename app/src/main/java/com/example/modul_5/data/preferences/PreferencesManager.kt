package com.example.modul_5.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "task_preferences"
)

class PreferencesManager(private val context: Context) {

    companion object {
        private val COMPLETED_TASK_COLOR = booleanPreferencesKey("completed_task_color")
    }


    val completedTaskColor: Flow<Boolean> = context.dataStore.data //для отслеживания цвета
        .map { preferences ->
            preferences[COMPLETED_TASK_COLOR] ?: false
        }

    suspend fun setCompletedTaskColor(isGreen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[COMPLETED_TASK_COLOR] = isGreen
        }
    }
}