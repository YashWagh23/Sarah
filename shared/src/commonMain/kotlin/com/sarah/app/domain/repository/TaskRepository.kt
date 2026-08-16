package com.sarah.app.domain.repository

import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    fun getTasksBySubject(subjectId: Long): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun insertTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun updateTaskStatus(id: Long, status: TaskStatus)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTaskById(id: Long)
}
