package com.sarah.app.ui.screens.subjects

import com.sarah.app.domain.model.Subject

data class SubjectWithTaskCount(
    val subject: Subject,
    val pendingTasksCount: Int
)

data class SubjectsUiState(
    val isLoading: Boolean = true,
    val subjectsWithCount: List<SubjectWithTaskCount> = emptyList(),
    val isAddEditDialogOpen: Boolean = false,
    val editingSubject: Subject? = null
)
