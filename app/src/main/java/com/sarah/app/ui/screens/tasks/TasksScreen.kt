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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.ui.components.TaskCard
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary

@Composable
fun TasksScreen(
    viewModel: TasksViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddTaskDialog() },
                containerColor = ElectricIndigo,
                contentColor = TextPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricIndigo)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        text = "ACADEMIC TASKS",
                        style = MaterialTheme.typography.labelMedium,
                        color = ElectricIndigo,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Assignments & Practicals",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Filter Tabs (Active / Completed / All)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskFilter.values().forEach { filter ->
                        val isSelected = filter == uiState.selectedFilter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricIndigo else DarkSurface)
                                .border(1.dp, if (isSelected) ElectricIndigo else DarkBorder, RoundedCornerShape(12.dp))
                                .clickable { viewModel.setFilter(filter) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Filter Chips
                if (uiState.subjects.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // All Chip
                        val isAllSelected = uiState.selectedSubjectId == null
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAllSelected) DarkSurfaceVariant else DarkSurface)
                                .border(1.dp, if (isAllSelected) ElectricIndigo else DarkBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSubjectFilter(null) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "All Subjects",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAllSelected) ElectricIndigo else TextSecondary,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        uiState.subjects.forEach { subject ->
                            val isSelected = uiState.selectedSubjectId == subject.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                                    .border(1.dp, if (isSelected) ElectricIndigo else DarkBorder, RoundedCornerShape(8.dp))
                                    .clickable { viewModel.setSubjectFilter(subject.id) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) ElectricIndigo else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tasks List
                if (uiState.filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Assignment,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tasks found in this view",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap + to add a new assignment or practical",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.filteredTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) },
                                onClick = { viewModel.openEditTaskDialog(task) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (uiState.isAddEditDialogOpen) {
            AddEditTaskDialog(
                task = uiState.editingTask,
                subjects = uiState.subjects,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave = { title, subjectId, type, desc, deadline, duration, priority, difficulty, energy ->
                    viewModel.saveTask(title, subjectId, type, desc, deadline, duration, priority, difficulty, energy)
                },
                onDelete = { viewModel.deleteTask(it) }
            )
        }
    }
}
