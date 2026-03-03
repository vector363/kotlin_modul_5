package com.example.modul_5.domain.usecase

import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.repository.TaskRepository

class DeleteTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        repository.deleteTask(task)
    }
}