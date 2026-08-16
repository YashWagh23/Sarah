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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Subject
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    onDismiss: () -> Unit,
    onSaveReminder: (title: String, message: String, timeEpochMs: Long, subjectId: Long?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf(ReminderTimePreset.TODAY_EVENING) }
    var customTimeEpochMs by remember { mutableStateOf<Long?>(null) }
    var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
    var showCustomPicker by remember { mutableStateOf(false) }

    fun calculateEpochMs(preset: ReminderTimePreset): Long {
        val now = LocalDateTime.now()
        val zone = ZoneId.systemDefault()
        return when (preset) {
            ReminderTimePreset.TODAY_EVENING -> {
                val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(18, 30))
                if (target.isAfter(now)) target.atZone(zone).toInstant().toEpochMilli()
                else now.plusHours(2).atZone(zone).toInstant().toEpochMilli()
            }
            ReminderTimePreset.TONIGHT -> {
                val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(21, 0))
                if (target.isAfter(now)) target.atZone(zone).toInstant().toEpochMilli()
                else now.plusHours(1).atZone(zone).toInstant().toEpochMilli()
            }
            ReminderTimePreset.TOMORROW_MORNING -> {
                val target = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 0))
                target.atZone(zone).toInstant().toEpochMilli()
            }
            ReminderTimePreset.TOMORROW_EVENING -> {
                val target = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(18, 0))
                target.atZone(zone).toInstant().toEpochMilli()
            }
            ReminderTimePreset.CUSTOM -> customTimeEpochMs ?: (System.currentTimeMillis() + 60 * 60 * 1000L)
        }
    }

    if (showCustomPicker) {
        val now = LocalDateTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = (now.hour + 1) % 24,
            initialMinute = 0
        )
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis()
        )
        var isPickingTime by remember { mutableStateOf(false) }

        if (!isPickingTime) {
            DatePickerDialog(
                onDismissRequest = { showCustomPicker = false },
                confirmButton = {
                    TextButton(onClick = { isPickingTime = true }) {
                        Text("Next: Time", color = CyanAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomPicker = false }) {
                        Text("Cancel", color = TextMuted)
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
                            val selectedDateEpochMs = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                            val localDate = LocalDate.ofEpochDay(selectedDateEpochMs / (24 * 60 * 60 * 1000L))
                            val chosenDateTime = LocalDateTime.of(
                                localDate,
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            )
                            val targetEpochMs = chosenDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            customTimeEpochMs = targetEpochMs
                            selectedPreset = ReminderTimePreset.CUSTOM
                            showCustomPicker = false
                        }
                    ) {
                        Text("Confirm", color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isPickingTime = false }) {
                        Text("Back", color = TextMuted)
                    }
                },
                title = { Text("Select Reminder Time", color = TextPrimary) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface,
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
                            .background(ElectricIndigo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "New Quick Reminder",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sarah remembers so you don't have to",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextMuted
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
                    focusedBorderColor = ElectricIndigo,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = ElectricIndigo,
                    unfocusedLabelColor = TextMuted
                )
            )

            // Reminder Time Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "WHEN SHOULD IT RING?",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanAccent,
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
                            val dt = Instant.ofEpochMilli(customTimeEpochMs!!).atZone(ZoneId.systemDefault())
                            dt.format(DateTimeFormatter.ofPattern("MMM d, h:mm a"))
                        } else {
                            preset.label
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricIndigo else DarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CyanAccent else DarkBorder,
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
                                color = if (isSelected) TextPrimary else TextSecondary,
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
                        color = TextMuted,
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
                                    .background(if (isSelected) ElectricIndigo.copy(alpha = 0.3f) else DarkSurfaceVariant)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) ElectricIndigo else DarkBorder,
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
                                            tint = CyanAccent,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = subject.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) TextPrimary else TextSecondary
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
                    containerColor = ElectricIndigo,
                    contentColor = TextPrimary,
                    disabledContainerColor = DarkSurfaceVariant,
                    disabledContentColor = TextMuted
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
