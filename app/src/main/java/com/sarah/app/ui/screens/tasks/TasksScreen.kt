@file:JvmName("TasksScreenAndroidKt")
package com.sarah.app.ui.screens.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    TasksScreenContent(
        uiState = uiState,
        onOpenAddTask = { viewModel.openAddTaskDialog() },
        onFilterChange = { viewModel.setFilter(it) },
        onSubjectFilterChange = { viewModel.setSubjectFilter(it) },
        onToggleTaskStatus = { viewModel.toggleTaskStatus(it) },
        onEditTask = { viewModel.openEditTaskDialog(it) },
        onCloseAddEditDialog = { viewModel.closeAddEditDialog() },
        onSaveTask = { title, subjectId, type, desc, deadline, duration, priority, difficulty, energy ->
            viewModel.saveTask(title, subjectId, type, desc, deadline, duration, priority, difficulty, energy)
        },
        onDeleteTask = { viewModel.deleteTask(it) },
        modifier = modifier
    )
}
