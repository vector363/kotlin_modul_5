package com.example.modul_5.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.modul_5.domain.model.Task
import com.example.modul_5.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val completedTaskColor: Boolean = false,
    val errorMessage: String? = null
)

sealed class TaskEvent {
    data class OnTaskCheckedChange(val task: Task, val isChecked: Boolean) : TaskEvent()
    data class OnDeleteTask(val task: Task) : TaskEvent()
    data class OnAddTask(val title: String, val description: String) : TaskEvent()
    object OnToggleCompletedTaskColor : TaskEvent()
}

class TaskViewModel(
    private val getTasksUseCase: GetTasksUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val preferencesManager: com.example.modul_5.data.preferences.PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TaskEvent>()

    init {
        loadTasks()
        observePreferences()
        observeEvents()
    }

    private fun loadTasks() {
        viewModelScope.launch {
            getTasksUseCase()
                .onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Ошибка загрузки: ${e.message}"
                        )
                    }
                }
                .collectLatest { tasks ->
                    _uiState.update {
                        it.copy(
                            tasks = tasks,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesManager.completedTaskColor.collect { isGreen ->
                _uiState.update { it.copy(completedTaskColor = isGreen) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            _events.collect { event ->
                when (event) {
                    is TaskEvent.OnTaskCheckedChange -> {
                        updateTask(event.task.copy(isCompleted = event.isChecked))
                    }
                    is TaskEvent.OnDeleteTask -> {
                        deleteTask(event.task)
                    }
                    is TaskEvent.OnAddTask -> {
                        addTask(event.title, event.description)
                    }
                    is TaskEvent.OnToggleCompletedTaskColor -> {
                        toggleCompletedTaskColor()
                    }
                }
            }
        }
    }

    fun onEvent(event: TaskEvent) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }

    private suspend fun updateTask(task: Task) {
        try {
            updateTaskUseCase(task)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Ошибка обновления: ${e.message}")
            }
        }
    }

    private suspend fun deleteTask(task: Task) {
        try {
            deleteTaskUseCase(task)
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Ошибка удаления: ${e.message}")
            }
        }
    }

    private fun addTask(title: String, description: String) {
        viewModelScope.launch {
            if (title.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Заголовок не может быть пустым") }
                return@launch
            }

            try {
                addTaskUseCase(title, description)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Ошибка добавления: ${e.message}")
                }
            }
        }
    }

    private suspend fun toggleCompletedTaskColor() {
        preferencesManager.setCompletedTaskColor(!uiState.value.completedTaskColor)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}