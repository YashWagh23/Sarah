package com.sarah.app.ui.screens.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahTertiary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskDialog(
    task: Task?,
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        subjectId: Long?,
        type: TaskType,
        description: String,
        deadlineEpochMs: Long,
        estimatedMinutes: Int,
        priority: TaskPriority,
        difficulty: Difficulty,
        energyRequirement: EnergyRequirement
    ) -> Unit,
    onDelete: (Task) -> Unit = {}
) {
    var title by remember { mutableStateOf(task?.title ?: "") }
    var selectedSubjectId by remember { mutableStateOf(task?.subjectId ?: subjects.firstOrNull()?.id) }
    var selectedType by remember { mutableStateOf(task?.type ?: TaskType.ASSIGNMENT) }
    var description by remember { mutableStateOf(task?.description ?: "") }
    var estimatedMinutes by remember { mutableFloatStateOf((task?.estimatedMinutes ?: 45).toFloat()) }
    var selectedPriority by remember { mutableStateOf(task?.priority ?: TaskPriority.MEDIUM) }
    var selectedDifficulty by remember { mutableStateOf(task?.difficulty ?: Difficulty.MEDIUM) }
    var selectedEnergy by remember { mutableStateOf(task?.energyRequirement ?: EnergyRequirement.MEDIUM) }
    var deadlineEpochMs by remember {
        mutableLongStateOf(task?.deadlineEpochMs ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000))
    }

    var isSubjectDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, SarahOutlineVariant, RoundedCornerShape(24.dp)),
            color = SarahSurfaceContainerLowest
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (task == null) "New Academic Task" else "Edit Task",
                        style = MaterialTheme.typography.titleLarge,
                        color = SarahOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = SarahSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("e.g., Java Practical Programs 1-5") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SarahPrimary,
                        unfocusedBorderColor = SarahOutlineVariant,
                        focusedTextColor = SarahOnSurface,
                        unfocusedTextColor = SarahOnSurface,
                        focusedLabelColor = SarahPrimary,
                        unfocusedLabelColor = SarahSecondary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Dropdown
                ExposedDropdownMenuBox(
                    expanded = isSubjectDropdownExpanded,
                    onExpandedChange = { isSubjectDropdownExpanded = it }
                ) {
                    val currentSubjectName = subjects.find { it.id == selectedSubjectId }?.name ?: "No Subject"
                    OutlinedTextField(
                        value = currentSubjectName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubjectDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SarahPrimary,
                            unfocusedBorderColor = SarahOutlineVariant,
                            focusedTextColor = SarahOnSurface,
                            unfocusedTextColor = SarahOnSurface,
                            focusedLabelColor = SarahPrimary,
                            unfocusedLabelColor = SarahSecondary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = isSubjectDropdownExpanded,
                        onDismissRequest = { isSubjectDropdownExpanded = false },
                        modifier = Modifier.background(SarahSurfaceContainer)
                    ) {
                        subjects.forEach { subject ->
                            DropdownMenuItem(
                                text = { Text(subject.name, color = SarahOnSurface) },
                                onClick = {
                                    selectedSubjectId = subject.id
                                    isSubjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Task Type Selector
                Text(
                    text = "TASK TYPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskType.values().forEach { type ->
                        val isSelected = type == selectedType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SarahPrimary else SarahSurfaceContainer)
                                .clickable { selectedType = type }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Estimated Duration Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ESTIMATED DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = SarahSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${estimatedMinutes.roundToInt()} minutes",
                        style = MaterialTheme.typography.labelMedium,
                        color = SarahPrimaryFixedDim,
                        fontWeight = FontWeight.Bold
                    )
                }
                Slider(
                    value = estimatedMinutes,
                    onValueChange = { estimatedMinutes = it },
                    valueRange = 15f..180f,
                    steps = 10,
                    colors = SliderDefaults.colors(
                        thumbColor = SarahPrimary,
                        activeTrackColor = SarahPrimary,
                        inactiveTrackColor = SarahSurfaceContainer
                    )
                )

                // Quick preset duration buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(20, 30, 45, 60, 90).forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (estimatedMinutes.roundToInt() == preset) SarahPrimary.copy(alpha = 0.3f) else SarahSurfaceContainer)
                                .border(1.dp, if (estimatedMinutes.roundToInt() == preset) SarahPrimary else SarahOutlineVariant, RoundedCornerShape(8.dp))
                                .clickable { estimatedMinutes = preset.toFloat() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${preset}m",
                                style = MaterialTheme.typography.labelSmall,
                                color = SarahOnSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Priority
                Text(
                    text = "PRIORITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TaskPriority.values().forEach { priority ->
                        val isSelected = priority == selectedPriority
                        val color = when (priority) {
                            TaskPriority.CRITICAL -> SarahError
                            TaskPriority.HIGH -> SarahTertiary
                            TaskPriority.MEDIUM -> SarahPrimaryFixedDim
                            TaskPriority.LOW -> SarahSecondary
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.25f) else SarahSurfaceContainer)
                                .border(1.dp, if (isSelected) color else SarahOutlineVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedPriority = priority }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = priority.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Energy Requirement
                Text(
                    text = "ENERGY REQUIREMENT",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahSecondary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EnergyRequirement.values().forEach { req ->
                        val isSelected = req == selectedEnergy
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SarahPrimary.copy(alpha = 0.2f) else SarahSurfaceContainer)
                                .border(1.dp, if (isSelected) SarahPrimary else SarahOutlineVariant, RoundedCornerShape(10.dp))
                                .clickable { selectedEnergy = req }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = req.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Notes / Instructions") },
                    placeholder = { Text("e.g. Chapter 3 exercise questions 1 to 5") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SarahPrimary,
                        unfocusedBorderColor = SarahOutlineVariant,
                        focusedTextColor = SarahOnSurface,
                        unfocusedTextColor = SarahOnSurface,
                        focusedLabelColor = SarahPrimary,
                        unfocusedLabelColor = SarahSecondary
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task != null) {
                        IconButton(onClick = { onDelete(task) }) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = "Delete Task",
                                tint = SarahError
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = SarahOnSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSave(
                                        title.trim(),
                                        selectedSubjectId,
                                        selectedType,
                                        description.trim(),
                                        deadlineEpochMs,
                                        estimatedMinutes.roundToInt(),
                                        selectedPriority,
                                        selectedDifficulty,
                                        selectedEnergy
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SarahPrimary,
                                contentColor = SarahOnSurface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = title.isNotBlank()
                        ) {
                            Text(if (task == null) "Create Task" else "Save Changes")
                        }
                    }
                }
            }
        }
    }
}
