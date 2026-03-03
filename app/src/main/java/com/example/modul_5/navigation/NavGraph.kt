package com.example.modul_5.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.modul_5.TodoApplication
import com.example.modul_5.domain.usecase.*
import com.example.modul_5.presentation.ui.screen.AddEditTaskScreen
import com.example.modul_5.presentation.ui.screen.TaskListScreen
import com.example.modul_5.presentation.viewmodel.TaskEvent
import com.example.modul_5.presentation.viewmodel.TaskViewModel
import com.example.modul_5.presentation.viewmodel.TaskViewModelFactory

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as TodoApplication

    val getTasksUseCase = GetTasksUseCase(application.taskRepository)
    val addTaskUseCase = AddTaskUseCase(application.taskRepository)
    val updateTaskUseCase = UpdateTaskUseCase(application.taskRepository)
    val deleteTaskUseCase = DeleteTaskUseCase(application.taskRepository)

    val viewModel: TaskViewModel = viewModel(
        factory = TaskViewModelFactory(
            getTasksUseCase,
            addTaskUseCase,
            updateTaskUseCase,
            deleteTaskUseCase,
            application.preferencesManager
        )
    )

    NavHost(
        navController = navController,
        startDestination = "task_list"
    ) {
        composable("task_list") {
            TaskListScreen(
                viewModel = viewModel,
                onNavigateToAddTask = {
                    navController.navigate("add_task")
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate("edit_task/$taskId")
                }
            )
        }

        composable("add_task") {
            AddEditTaskScreen(
                task = null,
                onSave = { title, description ->
                    viewModel.onEvent(
                        TaskEvent.OnAddTask(title, description)
                    )
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "edit_task/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId") ?: 0
            val task = viewModel.uiState.value.tasks.find { it.id == taskId }

            if (task != null) {
                AddEditTaskScreen(
                    task = task,
                    onSave = { title, description ->
                        val updatedTask = task.copy(
                            title = title,
                            description = description
                        )
                        viewModel.onEvent(
                            com.example.modul_5.presentation.viewmodel.TaskEvent.OnTaskCheckedChange(
                                updatedTask, updatedTask.isCompleted
                            )
                        )
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
    }
}