package com.example.modul_5

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.example.modul_5.data.local.database.TaskDatabase
import com.example.modul_5.data.preferences.PreferencesManager
import com.example.modul_5.data.repository.TaskRepositoryImpl
import com.example.modul_5.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore(
    name = "task_preferences"
)

class TodoApplication : Application() {

    lateinit var taskRepository: TaskRepository
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate() {
        super.onCreate()

        // Инициализация базы данных
        val database = TaskDatabase.getDatabase(this)

        // Инициализация репозитория
        taskRepository = TaskRepositoryImpl(this, database)

        // Инициализация PreferencesManager
        preferencesManager = PreferencesManager(this)

        // Импорт задач из JSON при первом запуске
        CoroutineScope(Dispatchers.IO).launch {
            if (taskRepository.isDatabaseEmpty()) {
                val jsonString = assets.open("tasks.json")
                    .bufferedReader().use { it.readText() }
                taskRepository.importTasksFromJson(jsonString)
            }
        }
    }
}