package com.example.modul_5.domain.usecase

import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.repository.TaskRepository

class UpdateTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        val updatedTask = if (task.isCompleted && task.completedAt == null) {
            task.copy(completedAt = System.currentTimeMillis())
        } else if (!task.isCompleted) {
            task.copy(completedAt = null)
        } else {
            task
        }
        repository.updateTask(updatedTask)
    }
}