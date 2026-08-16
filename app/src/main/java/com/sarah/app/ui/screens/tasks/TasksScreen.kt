package com.sarah.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sarah.app.ui.components.TaskCard
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryContainer
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    modifier : Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.openAddTaskDialog() },
                containerColor = SarahPrimaryContainer,
                contentColor   = SarahPrimary,
                shape          = RoundedCornerShape(14.dp),
                elevation      = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = "Add Task",
                    modifier           = Modifier.size(24.dp)
                )
            }
        },
        containerColor = SarahBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier         = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SarahPrimary)
            }
        } else {
            LazyColumn(
                modifier       = modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Header ──────────────────────────────────────────────────
                item {
                    Text(
                        text       = "Active",
                        style      = MaterialTheme.typography.headlineMedium,
                        color      = SarahOnSurface,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                    )
                }

                // ── Scrollable subject filter chips ──────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // "All Tasks" chip
                        val isAllSelected = uiState.selectedSubjectId == null &&
                                uiState.selectedFilter == TaskFilter.ALL
                        FilterChip(
                            label      = "All Tasks",
                            isSelected = isAllSelected,
                            onClick    = {
                                viewModel.setSubjectFilter(null)
                                viewModel.setFilter(TaskFilter.ALL)
                            }
                        )
                        uiState.subjects.forEach { subject ->
                            val isSelected = uiState.selectedSubjectId == subject.id
                            FilterChip(
                                label      = subject.name,
                                isSelected = isSelected,
                                onClick    = { viewModel.setSubjectFilter(subject.id) }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Active tasks group ────────────────────────────────────────
                val activeTasks = uiState.filteredTasks.filter {
                    it.status != com.sarah.app.domain.model.TaskStatus.COMPLETED
                }
                if (activeTasks.isNotEmpty()) {
                    item {
                        // Bordered card container
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SarahSurfaceContainerLowest)
                                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        ) {
                            activeTasks.forEachIndexed { index, task ->
                                TaskCard(
                                    task           = task,
                                    onStatusToggle = { viewModel.toggleTaskStatus(it) },
                                    onClick        = { viewModel.openEditTaskDialog(task) }
                                )
                                // Divider between rows
                                if (index < activeTasks.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.Black.copy(alpha = 0.04f))
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Completed tasks group ────────────────────────────────────
                val completedTasks = uiState.filteredTasks.filter {
                    it.status == com.sarah.app.domain.model.TaskStatus.COMPLETED
                }
                if (completedTasks.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(24.dp))
                        // Section header with count badge
                        Row(
                            modifier              = Modifier.padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Text(
                                text       = "Completed",
                                style      = MaterialTheme.typography.headlineSmall,
                                color      = SarahOnSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SarahSurfaceContainerHigh)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text  = completedTasks.size.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SarahOnSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SarahSurfaceContainerLowest.copy(alpha = 0.75f))
                                .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        ) {
                            completedTasks.forEachIndexed { index, task ->
                                TaskCard(
                                    task           = task,
                                    onStatusToggle = { viewModel.toggleTaskStatus(it) },
                                    onClick        = { viewModel.openEditTaskDialog(task) }
                                )
                                if (index < completedTasks.lastIndex) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(Color.Black.copy(alpha = 0.04f))
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Empty state ──────────────────────────────────────────────
                if (uiState.filteredTasks.isEmpty()) {
                    item {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Outlined.Assignment,
                                    contentDescription = null,
                                    tint               = SarahOutlineVariant,
                                    modifier           = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text  = "No tasks found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SarahSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = "Tap + to add a new assignment",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }

        // Add / Edit Dialog
        if (uiState.isAddEditDialogOpen) {
            AddEditTaskDialog(
                task      = uiState.editingTask,
                subjects  = uiState.subjects,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave    = { title, subjectId, type, desc, deadline, duration, priority, difficulty, energy ->
                    viewModel.saveTask(title, subjectId, type, desc, deadline, duration, priority, difficulty, energy)
                },
                onDelete  = { viewModel.deleteTask(it) }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Reusable filter chip component (rounded-full, matching reference)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun FilterChip(
    label     : String,
    isSelected: Boolean,
    onClick   : () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) SarahPrimaryContainer else SarahSurfaceContainer)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text       = label,
            style      = MaterialTheme.typography.labelSmall,
            color      = if (isSelected) SarahOnSurface else SarahSecondary,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
