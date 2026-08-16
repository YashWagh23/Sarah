package com.sarah.app.ui.screens.today

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.ui.components.AddCustomReminderSheet
import com.sarah.app.ui.components.AgendaTimeline
import com.sarah.app.ui.components.EnergyLevelPicker
import com.sarah.app.ui.components.QuickCaptureBottomSheet
import com.sarah.app.ui.components.ReminderCard
import com.sarah.app.ui.components.SarahNextActionCard
import com.sarah.app.ui.components.SnoozeReminderDialog
import com.sarah.app.ui.components.TaskCard
import com.sarah.app.ui.screens.quickcapture.QuickCaptureViewModel
import com.sarah.app.ui.theme.CoralRed
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.DarkSurfaceVariant
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.MintEmerald
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary
import com.sarah.app.ui.theme.WarmAmber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    quickCaptureViewModel: QuickCaptureViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isQuickCaptureOpen by remember { mutableStateOf(false) }
    var isAddReminderOpen by remember { mutableStateOf(false) }
    var snoozingReminder by remember { mutableStateOf<Reminder?>(null) }
    var isAgendaExpanded by remember { mutableStateOf(false) }
    var isFabMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(
                    visible = isFabMenuExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Quick Reminder FAB
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    isFabMenuExpanded = false
                                    isAddReminderOpen = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Add Reminder",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabMenuExpanded = false
                                    isAddReminderOpen = true
                                },
                                containerColor = CyanAccent,
                                contentColor = DarkBackground,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Alarm,
                                    contentDescription = "Add Reminder",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Quick Task FAB
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                                .clickable {
                                    isFabMenuExpanded = false
                                    isQuickCaptureOpen = true
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Capture Task",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SmallFloatingActionButton(
                                onClick = {
                                    isFabMenuExpanded = false
                                    isQuickCaptureOpen = true
                                },
                                containerColor = ElectricIndigo,
                                contentColor = TextPrimary,
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Assignment,
                                    contentDescription = "Capture Task",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                FloatingActionButton(
                    onClick = { isFabMenuExpanded = !isFabMenuExpanded },
                    containerColor = ElectricIndigo,
                    contentColor = TextPrimary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = if (isFabMenuExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.Add,
                        contentDescription = "Quick Actions",
                        modifier = Modifier.size(24.dp)
                    )
                }
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
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Header Greeting & Daily Summary
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val studentName = uiState.userProfile?.name?.takeIf { it.isNotBlank() } ?: "Student"
                        Text(
                            text = "${uiState.greeting.uppercase()}, $studentName",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "What do I need to remember today?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        // Compact Daily Summary Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface)
                                .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.dailySummary.ifBlank { "You're all caught up 🎉" },
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // 2. Sarah's Next Move (Live Guidance Card)
                uiState.nextAction?.let { action ->
                    item {
                        SarahNextActionCard(
                            nextAction = action,
                            onPrimaryActionClick = { viewModel.handlePrimaryNextAction(it) },
                            onMarkCompletedClick = { viewModel.completeTaskById(it) }
                        )
                    }
                }

                // 3. Humanized Feasibility & Energy Card
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(DarkSurface)
                            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "TONIGHT'S OUTLOOK",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = uiState.humanCapacitySummary,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = uiState.humanFeasibilityHeadline,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = uiState.humanFeasibilitySubtext,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        // Energy Level Picker
                        EnergyLevelPicker(
                            currentEnergy = uiState.energyLevel,
                            onEnergySelected = { viewModel.setEnergyLevel(it) }
                        )
                    }
                }

                // 4. Upcoming Reminders Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TodaySectionHeader(
                            title = "UPCOMING REMINDERS",
                            count = uiState.upcomingReminders.size,
                            accentColor = WarmAmber
                        )

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .clickable { isAddReminderOpen = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add Reminder",
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Add",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (uiState.upcomingReminders.isEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(DarkSurface.copy(alpha = 0.6f))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsNone,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "No pending reminders. You're on track.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    items(uiState.upcomingReminders, key = { "rem_${it.id}" }) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onSnoozeClick = { snoozingReminder = it },
                            onDismissClick = { viewModel.dismissReminder(it) },
                            onDeleteClick = { viewModel.deleteReminder(it) }
                        )
                    }
                }

                // 5. Section: Must Do Tonight
                uiState.feasibilityReport?.mustDoTasks?.let { mustDos ->
                    if (mustDos.isNotEmpty()) {
                        item {
                            TodaySectionHeader(
                                title = "MUST DO TONIGHT",
                                count = mustDos.size,
                                accentColor = CoralRed
                            )
                        }
                        items(mustDos, key = { "must_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                    }
                }

                // 6. Section: Should Do Tonight
                uiState.feasibilityReport?.shouldDoTasks?.let { shouldDos ->
                    if (shouldDos.isNotEmpty()) {
                        item {
                            TodaySectionHeader(
                                title = "SHOULD DO TONIGHT",
                                count = shouldDos.size,
                                accentColor = WarmAmber
                            )
                        }
                        items(shouldDos, key = { "should_${it.id}" }) { task ->
                            TaskCard(
                                task = task,
                                onStatusToggle = { viewModel.toggleTaskStatus(it) }
                            )
                        }
                    }
                }

                // 7. Section: Can Defer / Tomorrow Preview
                val deferredTasks = uiState.feasibilityReport?.canDeferTasks ?: emptyList()
                if (deferredTasks.isNotEmpty()) {
                    item {
                        TodaySectionHeader(
                            title = "CAN DEFER (TOMORROW)",
                            count = deferredTasks.size,
                            accentColor = TextMuted
                        )
                    }
                    items(deferredTasks, key = { "defer_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            onStatusToggle = { viewModel.toggleTaskStatus(it) }
                        )
                    }
                }

                // 8. Collapsible Detailed Agenda & Timeline
                uiState.feasibilityReport?.suggestedAgenda?.let { agenda ->
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
                                .clickable { isAgendaExpanded = !isAgendaExpanded }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Full Evening Agenda & Timeline",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (isAgendaExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                    contentDescription = null,
                                    tint = TextSecondary
                                )
                            }

                            AnimatedVisibility(
                                visible = isAgendaExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    AgendaTimeline(agendaItems = agenda)
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        // Add Quick Reminder Sheet
        if (isAddReminderOpen) {
            AddCustomReminderSheet(
                availableSubjects = uiState.subjects,
                onDismiss = { isAddReminderOpen = false },
                onSaveReminder = { title, message, timeEpochMs, subjectId ->
                    viewModel.createCustomReminder(title, message, timeEpochMs, subjectId)
                }
            )
        }

        // Snooze Dialog
        snoozingReminder?.let { reminder ->
            SnoozeReminderDialog(
                reminder = reminder,
                onDismiss = { snoozingReminder = null },
                onSnoozeMinutes = { mins ->
                    viewModel.snoozeReminder(reminder.id, mins)
                },
                onSnoozeUntilEpochMs = { epochMs ->
                    viewModel.snoozeReminderUntil(reminder.id, epochMs)
                }
            )
        }

        // Quick Capture Bottom Sheet
        if (isQuickCaptureOpen) {
            QuickCaptureBottomSheet(
                viewModel = quickCaptureViewModel,
                onDismiss = { isQuickCaptureOpen = false }
            )
        }
    }
}

@Composable
fun TodaySectionHeader(
    title: String,
    count: Int,
    accentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = accentColor,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        if (count > 0) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}
