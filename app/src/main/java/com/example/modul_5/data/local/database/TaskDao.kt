package com.example.modul_5.data.local.database

import androidx.room.*
import com.example.modul_5.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY CASE WHEN isCompleted = 0 THEN 0 ELSE 1 END, createdAt DESC")
        fun getAllTasks(): Flow<List<TaskEntity>>

        @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY createdAt DESC")
        fun getActiveTasks(): Flow<List<TaskEntity>>

        @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
        fun getCompletedTasks(): Flow<List<TaskEntity>>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertTask(task: TaskEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAllTasks(tasks: List<TaskEntity>)

        @Update
        suspend fun updateTask(task: TaskEntity)

        @Delete
        suspend fun deleteTask(task: TaskEntity)

        @Query("DELETE FROM tasks WHERE id = :taskId")
        suspend fun deleteTaskById(taskId: Int)

        @Query("DELETE FROM tasks")
        suspend fun deleteAllTasks()

        @Query("SELECT COUNT(*) FROM tasks")
        suspend fun getTaskCount(): Int
}