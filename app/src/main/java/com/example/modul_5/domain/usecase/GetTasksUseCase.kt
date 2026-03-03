package com.example.modul_5.domain.usecase

import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return repository.getAllTasks()
    }
}