package com.sarah.app.ui.screens.subjects

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sarah.app.ui.components.SubjectCard
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryContainer
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer

@Composable
fun SubjectsScreen(
    viewModel: SubjectsViewModel,
    modifier : Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { viewModel.openAddSubjectDialog() },
                containerColor = SarahPrimaryContainer,
                contentColor   = SarahPrimary,
                shape          = RoundedCornerShape(14.dp),
                elevation      = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector        = Icons.Outlined.Add,
                    contentDescription = "Add Subject",
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
            LazyVerticalGrid(
                columns         = GridCells.Fixed(2),
                modifier        = modifier.fillMaxSize().padding(innerPadding),
                contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement   = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Full-span header ─────────────────────────────────────
                item(span = { GridItemSpan(2) }) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val subjectCount  = uiState.subjectsWithCount.size
                        val avgAttendance = if (subjectCount > 0) {
                            uiState.subjectsWithCount.map {
                                it.subject.currentAttendancePercentage
                            }.average().toInt()
                        } else 0

                        Text(
                            text       = "Semester Subjects",
                            style      = MaterialTheme.typography.headlineMedium,
                            color      = SarahOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        if (subjectCount > 0) {
                            Text(
                                text  = "$subjectCount Active Courses · $avgAttendance% Avg. Attendance",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SarahOnSurfaceVariant
                            )
                        }
                    }
                }

                // ── Scrollable filter chips ──────────────────────────────
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubjectFilterChip(label = "All Subjects", isSelected = true, onClick = {})
                        SubjectFilterChip(label = "Core",         isSelected = false, onClick = {})
                        SubjectFilterChip(label = "Elective",     isSelected = false, onClick = {})
                    }
                    Spacer(Modifier.height(4.dp))
                }

                if (uiState.subjectsWithCount.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier         = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector        = Icons.Outlined.School,
                                    contentDescription = null,
                                    tint               = SarahOutlineVariant,
                                    modifier           = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text  = "No subjects added yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SarahSecondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text  = "Tap + to add your course subjects",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SarahOnSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.subjectsWithCount, key = { it.subject.id }) { item ->
                        SubjectCard(
                            subject           = item.subject,
                            pendingTasksCount = item.pendingTasksCount,
                            notesCount        = 0, // notes count not available at this level
                            onClick           = { viewModel.openEditSubjectDialog(item.subject) }
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(Modifier.height(40.dp))
                }
            }
        }

        // Add/Edit Dialog
        if (uiState.isAddEditDialogOpen) {
            AddEditSubjectDialog(
                subject   = uiState.editingSubject,
                onDismiss = { viewModel.closeAddEditDialog() },
                onSave    = { name, code, prof, color, hours, targetAtt, currAtt ->
                    viewModel.saveSubject(name, code, prof, color, hours, targetAtt, currAtt)
                },
                onDelete  = { viewModel.deleteSubject(it) }
            )
        }
    }
}

@Composable
private fun SubjectFilterChip(
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
