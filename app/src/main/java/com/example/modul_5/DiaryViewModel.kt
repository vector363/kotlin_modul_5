package com.example.modul_5

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.lifecycle.AndroidViewModel
import android.app.Application

data class DiaryEntry(
    val fileName: String,
    val content: String,
    val timestamp: Long
)

class DiaryViewModel(application: Application) : AndroidViewModel(application) {

    private val fileRepository = FileRepository(application.applicationContext)

    private val _entries = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            val entries = fileRepository.loadAllEntries()
            _entries.value = entries
        }
    }

    fun addEntry(content: String) {
        viewModelScope.launch {
            val fileName = "${System.currentTimeMillis()}.txt"

            // Сохраняем в файл
            val success = fileRepository.saveEntry(fileName, content)

            if (success) {
                val newEntry = DiaryEntry(
                    fileName = fileName,
                    content = content,
                    timestamp = System.currentTimeMillis()
                )

                // Добавляем в список
                _entries.value = listOf(newEntry) + _entries.value
                println("✅ Запись сохранена: $fileName")
            } else {
                println("❌ Ошибка при сохранении: $fileName")
            }
        }
    }

    fun deleteEntry(fileName: String) {
        viewModelScope.launch {
            // Удаляем файл
            val success = fileRepository.deleteEntry(fileName)

            if (success) {
                // Удаляем из списка
                _entries.value = _entries.value.filter { it.fileName != fileName }
                println("✅ Запись удалена: $fileName")
            } else {
                println("❌ Ошибка при удалении: $fileName")
            }
        }
    }

    fun updateEntry(fileName: String, newContent: String) {
        viewModelScope.launch {
            // Обновляем файл
            val success = fileRepository.saveEntry(fileName, newContent)

            if (success) {
                // Обновляем в списке
                _entries.value = _entries.value.map { entry ->
                    if (entry.fileName == fileName) {
                        entry.copy(content = newContent)
                    } else {
                        entry
                    }
                }
                println("✅ Запись обновлена: $fileName")
            } else {
                println("❌ Ошибка при обновлении: $fileName")
            }
        }
    }
}