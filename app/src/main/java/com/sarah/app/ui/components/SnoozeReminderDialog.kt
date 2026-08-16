package com.sarah.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sarah.app.domain.model.Reminder
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeReminderDialog(
    reminder: Reminder,
    onDismiss: () -> Unit,
    onSnoozeMinutes: (Int) -> Unit,
    onSnoozeUntilEpochMs: (Long) -> Unit
) {
    var showCustomDateTimePicker by remember { mutableStateOf(false) }

    if (showCustomDateTimePicker) {
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
                onDismissRequest = { showCustomDateTimePicker = false },
                confirmButton = {
                    TextButton(onClick = { isPickingTime = true }) {
                        Text("Next: Set Time", color = CyanAccent)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCustomDateTimePicker = false }) {
                        Text("Cancel", color = TextMuted)
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        } else {
            AlertDialog(
                onDismissRequest = { showCustomDateTimePicker = false },
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
                            onSnoozeUntilEpochMs(targetEpochMs)
                            showCustomDateTimePicker = false
                            onDismiss()
                        }
                    ) {
                        Text("Set Reminder", color = CyanAccent, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { isPickingTime = false }) {
                        Text("Back", color = TextMuted)
                    }
                },
                title = { Text("Choose Reminder Time", color = TextPrimary) },
                text = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        TimePicker(state = timePickerState)
                    }
                },
                containerColor = DarkSurface
            )
        }
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = DarkSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Snooze,
                        contentDescription = null,
                        tint = WarmAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snooze Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "\"${reminder.title}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    SnoozeOptionItem(
                        icon = Icons.Rounded.AccessTime,
                        title = "10 minutes",
                        subtitle = "Quick temporary pause",
                        onClick = {
                            onSnoozeMinutes(10)
                            onDismiss()
                        }
                    )

                    SnoozeOptionItem(
                        icon = Icons.Rounded.AccessTime,
                        title = "30 minutes",
                        subtitle = "Short study break",
                        onClick = {
                            onSnoozeMinutes(30)
                            onDismiss()
                        }
                    )

                    SnoozeOptionItem(
                        icon = Icons.Rounded.Bedtime,
                        title = "Tonight at 8:00 PM",
                        subtitle = "Evening study session",
                        onClick = {
                            val target = LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 0))
                            val epochMs = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            val safeEpochMs = if (epochMs <= System.currentTimeMillis()) System.currentTimeMillis() + (60 * 60 * 1000L) else epochMs
                            onSnoozeUntilEpochMs(safeEpochMs)
                            onDismiss()
                        }
                    )

                    SnoozeOptionItem(
                        icon = Icons.Rounded.WbSunny,
                        title = "Tomorrow at 8:00 AM",
                        subtitle = "Before college begins",
                        onClick = {
                            val target = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(8, 0))
                            val epochMs = target.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                            onSnoozeUntilEpochMs(epochMs)
                            onDismiss()
                        }
                    )

                    SnoozeOptionItem(
                        icon = Icons.Rounded.Today,
                        title = "Choose specific time...",
                        subtitle = "Pick date & exact time",
                        onClick = { showCustomDateTimePicker = true }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SnoozeOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 11.sp
            )
        }
    }
}
