package com.example.modul_5

import android.content.Context
import java.io.File
import java.io.IOException

class FileRepository(private val context: Context) {

    private val diaryDir: File get() = context.filesDir

    fun saveEntry(fileName: String, content: String): Boolean {
        return try {
            val file = File(diaryDir, fileName)
            file.writeText(content)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Прочитать запись из файла
     */
    fun readEntry(fileName: String): String? {
        return try {
            val file = File(diaryDir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }


    fun deleteEntry(fileName: String): Boolean {
        return try {
            val file = File(diaryDir, fileName)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Получить список всех файлов записей
     */
    fun getAllEntryFiles(): List<File> {
        return diaryDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".txt") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
    }


    fun loadAllEntries(): List<DiaryEntry> {
        return getAllEntryFiles().mapNotNull { file ->
            val content = file.readText()
            val fileName = file.name
            val timestamp = try { fileName.removeSuffix(".txt").toLong()
            } catch (e: NumberFormatException) {
                file.lastModified()
            }

            DiaryEntry(
                fileName = fileName,
                content = content,
                timestamp = timestamp
            )
        }.sortedByDescending { it.timestamp }
    }
}