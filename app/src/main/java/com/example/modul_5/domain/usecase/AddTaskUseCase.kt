package com.example.modul_5.domain.usecase

import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.repository.TaskRepository

class AddTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        isCompleted: Boolean = false
    ) {
        val task = Task(
            title = title,
            description = description,
            isCompleted = isCompleted,
            createdAt = System.currentTimeMillis(),
            completedAt = if (isCompleted) System.currentTimeMillis() else null
        )
        repository.addTask(task)
    }
}