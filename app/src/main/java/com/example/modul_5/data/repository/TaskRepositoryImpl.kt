package com.example.modul_5.data.repository

import android.content.Context
import com.example.modul_5.data.local.database.TaskDatabase
import com.example.modul_5.data.local.entity.TaskEntity
import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.repository.TaskRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.IOException

class TaskRepositoryImpl(
    private val context: Context,
    private val database: TaskDatabase
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return database.taskDao().getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTaskById(taskId: Int): Task? {
        // Для простоты можно добавить метод в DAO позже
        return null
    }

    override suspend fun addTask(task: Task) {
        database.taskDao().insertTask(task.toEntity())
    }

    override suspend fun updateTask(task: Task) {
        database.taskDao().updateTask(task.toEntity())
    }

    override suspend fun deleteTask(task: Task) {
        database.taskDao().deleteTask(task.toEntity())
    }

    override suspend fun deleteTaskById(taskId: Int) {
        database.taskDao().deleteTaskById(taskId)
    }

    override suspend fun importTasksFromJson(jsonString: String) {
        try {
            val itemType = object : TypeToken<List<TaskEntity>>() {}.type
            val tasks: List<TaskEntity> = Gson().fromJson(jsonString, itemType)

            // Проверяем, есть ли уже задачи в БД
            if (database.taskDao().getTaskCount() == 0) {
                database.taskDao().insertAllTasks(tasks)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override suspend fun isDatabaseEmpty(): Boolean {
        return database.taskDao().getTaskCount() == 0
    }

    // Extension functions для преобразования между Entity и Domain
    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = this.id,
            title = this.title,
            description = this.description,
            isCompleted = this.isCompleted,
            createdAt = this.createdAt,
            completedAt = this.completedAt
        )
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = this.id,
            title = this.title,
            description = this.description,
            isCompleted = this.isCompleted,
            createdAt = this.createdAt,
            completedAt = this.completedAt
        )
    }
}