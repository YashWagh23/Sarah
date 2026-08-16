package com.sarah.app.ui.components

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.util.currentTimeEpochMs
import com.sarah.app.domain.util.formatDateTime
import com.sarah.app.domain.util.getStartOfTodayEpochMs
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest

enum class ReminderTimePreset(val label: String) {
    TODAY_EVENING("Today 6:30 PM"),
    TONIGHT("Tonight 9:00 PM"),
    TOMORROW_MORNING("Tomorrow 8:00 AM"),
    TOMORROW_EVENING("Tomorrow 6:00 PM"),
    CUSTOM("Custom Time...")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomReminderSheet(
    availableSubjects: List<Subject>,
    initialTitle: String = "",
    initialSubjectId: Long? = null,
    onDismiss: () -> Unit,
    onSaveReminder: (title: String, message: String, timeEpochMs: Long, subjectId: Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }
    var selectedPreset by remember { mutableStateOf(ReminderTimePreset.TODAY_EVENING) }
    var customTimeEpochMs by remember { mutableStateOf<Long?>(null) }
    var selectedSubjectId by remember(initialSubjectId) { mutableStateOf(initialSubjectId) }
    var showCustomPicker by remember { mutableStateOf(false) }

    fun calculateEpochMs(preset: ReminderTimePreset): Long {
        val startOfToday = getStartOfTodayEpochMs()
        val now = currentTimeEpochMs()
        return when (preset) {
            ReminderTimePreset.TODAY_EVENING -> {
                val target = startOfToday + (18 * 3600 + 30 * 60) * 1000L
                if (target > now) target else now + (2 * 3600 * 1000L)
            }
            ReminderTimePreset.TONIGHT -> {
                val target = startOfToday + (21 * 3600) * 1000L
                if (target > now) target else now + (1 * 3600 * 1000L)
            }
            ReminderTimePreset.TOMORROW_MORNING -> {
                startOfToday + (32 * 3600) * 1000L
            }
            ReminderTimePreset.TOMORROW_EVENING -> {
                startOfToday + (42 * 3600) * 1000L
            }
            ReminderTimePreset.CUSTOM -> customTimeEpochMs ?: (now + 60 * 60 * 1000L)
        }
    }

    if (showCustomPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 19,
            initialMinute = 0
        )
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = currentTimeEpochMs()
        )
        var isPickingTime by remember { mutableStateOf(false) }

        if (!isPickingTime) {
            DatePickerDialog(
                onDismissRequest = { showCustomPicker = false },
                confirmButton = {
                    TextButton(onClick = { isPickingTime = true }) {
                        Text("Next: Time", color = SarahPrimaryFixedDim)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomPicker = false }) {
                        Text("Cancel", color = SarahSecondary)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        } else {
            AlertDialog(
                onDismissRequest = { showCustomPicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDateEpochMs = datePickerState.selectedDateMillis ?: currentTimeEpochMs()
                            val startOfDay = (selectedDateEpochMs / (24 * 60 * 60 * 1000L)) * (24 * 60 * 60 * 1000L)
                            val targetEpochMs = startOfDay + (timePickerState.hour * 3600_000L) + (timePickerState.minute * 60_000L)
                            customTimeEpochMs = targetEpochMs
                            selectedPreset = ReminderTimePreset.CUSTOM
                            showCustomPicker = false
                        }
                    ) {
                        Text("Confirm", color = SarahPrimaryFixedDim, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isPickingTime = false }) {
                        Text("Back", color = SarahSecondary)
                    }
                },
                title = { Text("Select Reminder Time", color = SarahOnSurface) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                containerColor = SarahSurfaceContainerLowest
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SarahSurfaceContainerLowest,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SarahPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = SarahPrimaryFixedDim,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "New Quick Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SarahOnSurface
                        )
                        Text(
                            text = "Sarah remembers so you don't have to",
                            style = MaterialTheme.typography.labelSmall,
                            color = SarahSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = SarahSecondary
                    )
                }
            }

            // Input field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("What should Sarah remind you?") },
                placeholder = { Text("e.g. Bring DBMS record, Print lab sheet, Call mentor") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SarahPrimary,
                    unfocusedBorderColor = SarahOutlineVariant,
                    focusedTextColor = SarahOnSurface,
                    unfocusedTextColor = SarahOnSurface,
                    focusedLabelColor = SarahPrimary,
                    unfocusedLabelColor = SarahSecondary
                )
            )

            // Reminder Time Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WHEN SHOULD IT RING?",
                    style = MaterialTheme.typography.labelSmall,
                    color = SarahPrimaryFixedDim,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReminderTimePreset.values().forEach { preset ->
                        val isSelected = selectedPreset == preset
                        val labelText = if (preset == ReminderTimePreset.CUSTOM && customTimeEpochMs != null) {
                            formatDateTime(customTimeEpochMs!!)
                        } else {
                            preset.label
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) SarahPrimary else SarahSurfaceContainer)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) SarahPrimaryFixedDim else SarahOutlineVariant,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    if (preset == ReminderTimePreset.CUSTOM) {
                                        showCustomPicker = true
                                    } else {
                                        selectedPreset = preset
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = labelText,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Optional Subject Tagging
            if (availableSubjects.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "RELATED SUBJECT (OPTIONAL)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SarahSecondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableSubjects.forEach { subject ->
                            val isSelected = selectedSubjectId == subject.id
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) SarahPrimary.copy(alpha = 0.3f) else SarahSurfaceContainer)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) SarahPrimary else SarahOutlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        selectedSubjectId = if (isSelected) null else subject.id
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = SarahPrimaryFixedDim,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) SarahOnSurface else SarahOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Save button
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val epochMs = calculateEpochMs(selectedPreset)
                        val subjectName = availableSubjects.find { it.id == selectedSubjectId }?.name
                        val message = if (subjectName != null) {
                            "Reminder for $subjectName: $title"
                        } else {
                            title.trim()
                        }
                        onSaveReminder(title.trim(), message, epochMs, selectedSubjectId)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SarahPrimary,
                    contentColor = SarahOnSurface,
                    disabledContainerColor = SarahSurfaceContainer,
                    disabledContentColor = SarahSecondary
                )
            ) {
                Text(
                    text = "Save Reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
