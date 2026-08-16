package com.sarah.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.ReminderRepository
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
    private val subjectRepository: SubjectRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val deadlineReminderEngine: DeadlineReminderEngine,
    private val preferencesManager: SarahPreferences
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

            if (newStatus == TaskStatus.COMPLETED) {
                // Cancel and remove reminders for completed task
                reminderScheduler.cancelTaskReminders(task.id)
                reminderRepository.deleteRemindersByTaskId(task.id)
            } else if (preferencesManager.isDeadlineRemindersEnabled) {
                // Task restored to pending -> reschedule deadline reminders
                val reminders = deadlineReminderEngine.generateDeadlineReminders(task.copy(status = TaskStatus.PENDING))
                reminders.forEach { rem ->
                    val remId = reminderRepository.insertReminder(rem)
                    reminderScheduler.scheduleReminder(rem.copy(id = remId))
                }
            }
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
                val updatedTask = existing.copy(
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
                taskRepository.updateTask(updatedTask)

                // Cancel old reminders and reschedule new ones
                reminderScheduler.cancelTaskReminders(updatedTask.id)
                reminderRepository.deleteRemindersByTaskId(updatedTask.id)

                if (updatedTask.status != TaskStatus.COMPLETED && preferencesManager.isDeadlineRemindersEnabled) {
                    val reminders = deadlineReminderEngine.generateDeadlineReminders(updatedTask)
                    reminders.forEach { rem ->
                        val remId = reminderRepository.insertReminder(rem)
                        reminderScheduler.scheduleReminder(rem.copy(id = remId))
                    }
                }
            } else {
                val newTask = Task(
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
                val newId = taskRepository.insertTask(newTask)

                if (preferencesManager.isDeadlineRemindersEnabled) {
                    val reminders = deadlineReminderEngine.generateDeadlineReminders(newTask.copy(id = newId))
                    reminders.forEach { rem ->
                        val remId = reminderRepository.insertReminder(rem)
                        reminderScheduler.scheduleReminder(rem.copy(id = remId))
                    }
                }
            }
            closeAddEditDialog()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            reminderScheduler.cancelTaskReminders(task.id)
            reminderRepository.deleteRemindersByTaskId(task.id)
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
        private val subjectRepository: SubjectRepository,
        private val reminderRepository: ReminderRepository,
        private val reminderScheduler: ReminderScheduler,
        private val deadlineReminderEngine: DeadlineReminderEngine,
        private val preferencesManager: SarahPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TasksViewModel(
                taskRepository,
                subjectRepository,
                reminderRepository,
                reminderScheduler,
                deadlineReminderEngine,
                preferencesManager
            ) as T
        }
    }
}
