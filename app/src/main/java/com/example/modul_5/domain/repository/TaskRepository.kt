package com.example.modul_5.domain.repository

import com.example.modul_5.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun getAllTasks(): Flow<List<Task>>

    suspend fun getTaskById(taskId: Int): Task?

    suspend fun addTask(task: Task)

    suspend fun updateTask(task: Task)

    suspend fun deleteTask(task: Task)

    suspend fun deleteTaskById(taskId: Int)

    suspend fun importTasksFromJson(jsonString: String)

    suspend fun isDatabaseEmpty(): Boolean
}