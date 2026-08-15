package com.sarah.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TaskFilter(val displayName: String) {
    ALL("All"),
    ACTIVE("Active"),
    COMPLETED("Completed")
}

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.ACTIVE,
    val selectedSubjectId: Long? = null,
    val isAddEditDialogOpen: Boolean = false,
    val editingTask: Task? = null
)

class TasksViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        combine(
            taskRepository.getAllTasks(),
            subjectRepository.getActiveSubjects()
        ) { tasks, subjects ->
            _uiState.update { current ->
                val filtered = applyFilter(tasks, current.selectedFilter, current.selectedSubjectId)
                current.copy(
                    isLoading = false,
                    tasks = tasks,
                    filteredTasks = filtered,
                    subjects = subjects
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setFilter(filter: TaskFilter) {
        _uiState.update { current ->
            current.copy(
                selectedFilter = filter,
                filteredTasks = applyFilter(current.tasks, filter, current.selectedSubjectId)
            )
        }
    }

    fun setSubjectFilter(subjectId: Long?) {
        _uiState.update { current ->
            val newSubjectId = if (current.selectedSubjectId == subjectId) null else subjectId
            current.copy(
                selectedSubjectId = newSubjectId,
                filteredTasks = applyFilter(current.tasks, current.selectedFilter, newSubjectId)
            )
        }
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.updateTaskStatus(task.id, newStatus)
        }
    }

    fun openAddTaskDialog() {
        _uiState.update { it.copy(isAddEditDialogOpen = true, editingTask = null) }
    }

    fun openEditTaskDialog(task: Task) {
        _uiState.update { it.copy(isAddEditDialogOpen = true, editingTask = task) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditDialogOpen = false, editingTask = null) }
    }

    fun saveTask(
        title: String,
        subjectId: Long?,
        type: TaskType,
        description: String,
        deadlineEpochMs: Long,
        estimatedMinutes: Int,
        priority: TaskPriority,
        difficulty: Difficulty,
        energyRequirement: EnergyRequirement
    ) {
        viewModelScope.launch {
            val subjectName = _uiState.value.subjects.find { it.id == subjectId }?.name
            val existing = _uiState.value.editingTask

            if (existing != null) {
                taskRepository.updateTask(
                    existing.copy(
                        title = title,
                        subjectId = subjectId,
                        subjectName = subjectName,
                        type = type,
                        description = description,
                        deadlineEpochMs = deadlineEpochMs,
                        estimatedMinutes = estimatedMinutes,
                        priority = priority,
                        difficulty = difficulty,
                        energyRequirement = energyRequirement
                    )
                )
            } else {
                taskRepository.insertTask(
                    Task(
                        title = title,
                        subjectId = subjectId,
                        subjectName = subjectName,
                        type = type,
                        description = description,
                        deadlineEpochMs = deadlineEpochMs,
                        estimatedMinutes = estimatedMinutes,
                        priority = priority,
                        difficulty = difficulty,
                        energyRequirement = energyRequirement
                    )
                )
            }
            closeAddEditDialog()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
            closeAddEditDialog()
        }
    }

    private fun applyFilter(tasks: List<Task>, filter: TaskFilter, subjectId: Long?): List<Task> {
        return tasks.filter { task ->
            val statusMatch = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.ACTIVE -> task.status != TaskStatus.COMPLETED
                TaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
            }
            val subjectMatch = subjectId == null || task.subjectId == subjectId
            statusMatch && subjectMatch
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val subjectRepository: SubjectRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(taskRepository, subjectRepository) as T
        }
    }
}
