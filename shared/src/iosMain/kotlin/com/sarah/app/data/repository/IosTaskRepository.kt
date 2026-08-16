package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosTaskRepository(
    private val database: IosSarahDatabase
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return database.tasksFlow
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return database.tasksFlow.map { list ->
            list.filter { it.status != TaskStatus.COMPLETED }
        }
    }

    override fun getTasksBySubject(subjectId: Long): Flow<List<Task>> {
        return database.tasksFlow.map { list ->
            list.filter { it.subjectId == subjectId }
        }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return database.tasksFlow.value.find { it.id == id }
    }

    override suspend fun insertTask(task: Task): Long {
        val currentList = database.tasksFlow.value.toMutableList()
        val newId = if (task.id > 0) task.id else (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = task.copy(id = newId)
        val existingIndex = currentList.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = toInsert
        } else {
            currentList.add(toInsert)
        }
        database.tasksFlow.value = currentList
        database.saveTasks()
        return newId
    }

    override suspend fun updateTask(task: Task) {
        val currentList = database.tasksFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            currentList[index] = task
            database.tasksFlow.value = currentList
            database.saveTasks()
        }
    }

    override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
        val currentList = database.tasksFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(status = status)
            database.tasksFlow.value = currentList
            database.saveTasks()
        }
    }

    override suspend fun deleteTask(task: Task) {
        deleteTaskById(task.id)
    }

    override suspend fun deleteTaskById(id: Long) {
        val currentList = database.tasksFlow.value.toMutableList()
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            database.tasksFlow.value = currentList
            database.saveTasks()
        }
    }
}
