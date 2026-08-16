package com.sarah.app.ui.screens.tasks

import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task

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
