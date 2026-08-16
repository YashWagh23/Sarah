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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PriorityHigh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahError
import com.sarah.app.ui.theme.SarahErrorContainer
import com.sarah.app.ui.theme.SarahOnErrorContainer
import com.sarah.app.ui.theme.SarahOnSurface
import com.sarah.app.ui.theme.SarahOnSurfaceVariant
import com.sarah.app.ui.theme.SarahOutlineVariant
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahPrimaryContainer
import com.sarah.app.ui.theme.SarahPrimaryFixed
import com.sarah.app.ui.theme.SarahPrimaryFixedDim
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainer
import com.sarah.app.ui.theme.SarahSurfaceContainerHigh
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahTertiary
import com.sarah.app.ui.theme.SarahTertiaryContainer
import com.sarah.app.ui.theme.SarahTertiaryFixedDim
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    quickCaptureViewModel: QuickCaptureViewModel,
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isQuickCaptureOpen by remember { mutableStateOf(false) }
    var isAddReminderOpen by remember { mutableStateOf(false) }
    var snoozingReminder by remember { mutableStateOf<Reminder?>(null) }
    var isAgendaExpanded by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick            = { isQuickCaptureOpen = true },
                containerColor     = SarahPrimaryContainer,
                contentColor       = SarahPrimary,
                shape              = RoundedCornerShape(14.dp),
                elevation          = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                Icon(
                    imageVector       = Icons.Outlined.Add,
                    contentDescription = "Quick Capture",
                    modifier          = Modifier.size(24.dp)
                )
            }
        },
        containerColor = SarahBackground
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier          = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment  = Alignment.Center
            ) {
                CircularProgressIndicator(color = SarahPrimary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier        = modifier.fillMaxSize().padding(innerPadding),
            contentPadding  = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── 1. Greeting ──────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 28.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val studentName = uiState.userProfile?.name?.takeIf { it.isNotBlank() } ?: "Student"
                    Text(
                        text  = "${uiState.greeting}, $studentName",
                        style = MaterialTheme.typography.headlineMedium,
                        color = SarahOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = "Let's figure out tonight.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SarahOnSurfaceVariant
                    )
                }
            }

            // ── 2. Sarah's Next Move – Glass Card ────────────────────────
            uiState.nextAction?.let { action ->
                item {
                    SarahNextActionCard(
                        nextAction          = action,
                        onPrimaryActionClick = { viewModel.handlePrimaryNextAction(it) },
                        onMarkCompletedClick = { viewModel.completeTaskById(it) },
                        modifier            = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
            }

            // ── 3. Daily Summary Row ──────────────────────────────────────
            item {
                val taskCount     = (uiState.feasibilityReport?.mustDoTasks?.size ?: 0) +
                                    (uiState.feasibilityReport?.shouldDoTasks?.size ?: 0)
                val reminderCount = uiState.upcomingReminders.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "DAILY SUMMARY",
                        style      = MaterialTheme.typography.labelSmall,
                        color      = SarahOnSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text  = buildString {
                            if (taskCount > 0) append("$taskCount tasks")
                            if (taskCount > 0 && reminderCount > 0) append(" • ")
                            if (reminderCount > 0) append("$reminderCount reminders")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = SarahSecondary
                    )
                }
            }

            // ── 4. Must Do tasks ─────────────────────────────────────────
            uiState.feasibilityReport?.mustDoTasks?.let { mustDos ->
                items(mustDos, key = { "must_${it.id}" }) { task ->
                    TodayTaskRow(
                        task           = task,
                        priorityLabel  = "Must do",
                        priorityColor  = SarahError,
                        iconBg         = SarahErrorContainer,
                        iconTint       = SarahOnErrorContainer,
                        icon           = Icons.Filled.Bolt,
                        onToggle       = { viewModel.toggleTaskStatus(task) },
                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
                }
            }

            // ── 5. Should Do tasks ───────────────────────────────────────
            uiState.feasibilityReport?.shouldDoTasks?.let { shouldDos ->
                items(shouldDos, key = { "should_${it.id}" }) { task ->
                    TodayTaskRow(
                        task           = task,
                        priorityLabel  = "Should do",
                        priorityColor  = SarahTertiary,
                        iconBg         = SarahTertiaryFixedDim.copy(alpha = 0.2f),
                        iconTint       = SarahTertiary,
                        icon           = Icons.Filled.MenuBook,
                        onToggle       = { viewModel.toggleTaskStatus(task) },
                        modifier       = Modifier.padding(horizontal = 20.dp, vertical = 5.dp)
                    )
                }
            }

            // ── 6. Deferred tasks ────────────────────────────────────────
            val deferred = uiState.feasibilityReport?.canDeferTasks ?: emptyList()
            items(deferred, key = { "defer_${it.id}" }) { task ->
                TodayTaskRow(
                    task           = task,
                    priorityLabel  = "Later",
                    priorityColor  = SarahSecondary,
                    iconBg         = SarahSurfaceContainerHigh,
                    iconTint       = SarahSecondary,
                    icon           = Icons.Outlined.Inventory2,
                    onToggle       = { viewModel.toggleTaskStatus(task) },
                    modifier       = Modifier
                        .padding(horizontal = 20.dp, vertical = 5.dp)
                        .then(Modifier /* 75% opacity for deferred */)
                )
            }

            // ── 7. Upcoming Reminders header + "Add" button ───────────────
            item {
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Upcoming Reminders",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = SarahOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SarahSurfaceContainerHigh)
                            .clickable { isAddReminderOpen = true }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector       = Icons.Outlined.Add,
                                contentDescription = "Add Reminder",
                                tint              = SarahPrimary,
                                modifier          = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text       = "Add",
                                style      = MaterialTheme.typography.labelSmall,
                                color      = SarahPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── 8. Upcoming Reminders – 2-col grid ───────────────────────
            if (uiState.upcomingReminders.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector       = Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint              = SarahSecondary,
                            modifier          = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text  = "No pending reminders. You're on track.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SarahSecondary
                        )
                    }
                }
            } else {
                // Pair reminders into rows of 2
                val reminders = uiState.upcomingReminders
                val rows      = reminders.chunked(2)
                items(rows, key = { row -> "rem_row_${row.first().id}" }) { row ->
                    Row(
                        modifier            = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { reminder ->
                            ReminderCard(
                                reminder      = reminder,
                                onSnoozeClick = { snoozingReminder = it },
                                onDismissClick = { viewModel.dismissReminder(it) },
                                onDeleteClick  = { viewModel.deleteReminder(it) },
                                modifier      = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // ── 9. Collapsible Detailed Agenda ───────────────────────────
            uiState.feasibilityReport?.suggestedAgenda?.let { agenda ->
                item {
                    Spacer(Modifier.height(20.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SarahSurfaceContainerLowest)
                            .border(1.dp, Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                            .clickable { isAgendaExpanded = !isAgendaExpanded }
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector       = Icons.Outlined.Schedule,
                                    contentDescription = null,
                                    tint              = SarahPrimary,
                                    modifier          = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text       = "Full Evening Agenda & Timeline",
                                    style      = MaterialTheme.typography.titleSmall,
                                    color      = SarahOnSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Icon(
                                imageVector       = if (isAgendaExpanded) Icons.Outlined.Alarm else Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint              = SarahSecondary,
                                modifier          = Modifier.size(18.dp)
                            )
                        }
                        AnimatedVisibility(
                            visible = isAgendaExpanded,
                            enter   = fadeIn() + expandVertically(),
                            exit    = fadeOut() + shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                AgendaTimeline(agendaItems = agenda)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }

        // ── Sheets & Dialogs ──────────────────────────────────────────────────
        if (isAddReminderOpen) {
            AddCustomReminderSheet(
                availableSubjects = uiState.subjects,
                onDismiss         = { isAddReminderOpen = false },
                onSaveReminder    = { title, message, timeEpochMs, subjectId ->
                    viewModel.createCustomReminder(title, message, timeEpochMs, subjectId)
                }
            )
        }

        snoozingReminder?.let { reminder ->
            SnoozeReminderDialog(
                reminder          = reminder,
                onDismiss         = { snoozingReminder = null },
                onSnoozeMinutes   = { mins -> viewModel.snoozeReminder(reminder.id, mins) },
                onSnoozeUntilEpochMs = { epochMs -> viewModel.snoozeReminderUntil(reminder.id, epochMs) }
            )
        }

        if (isQuickCaptureOpen) {
            QuickCaptureBottomSheet(
                viewModel         = quickCaptureViewModel,
                onDismiss         = { isQuickCaptureOpen = false }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TodayTaskRow — white card row matching the reference "Must do / Should do / Later" style
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun TodayTaskRow(
    task          : Task,
    priorityLabel : String,
    priorityColor : Color,
    iconBg        : Color,
    iconTint      : Color,
    icon          : ImageVector,
    onToggle      : () -> Unit,
    modifier      : Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.COMPLETED
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SarahSurfaceContainerLowest)
            .border(1.dp, Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon circle
        Box(
            modifier         = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector       = icon,
                contentDescription = null,
                tint              = iconTint,
                modifier          = Modifier.size(20.dp)
            )
        }

        // Text column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = priorityLabel.uppercase(),
                style      = MaterialTheme.typography.labelSmall,
                color      = priorityColor,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text       = task.title,
                style      = MaterialTheme.typography.titleMedium,
                color      = SarahOnSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Checkbox circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .then(
                    if (isDone) Modifier.background(SarahPrimary)
                    else Modifier
                        .background(Color.Transparent)
                        .border(2.dp, SarahOutlineVariant, CircleShape)
                )
                .clickable { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector       = Icons.Outlined.Check,
                    contentDescription = "Completed",
                    tint              = Color.White,
                    modifier          = Modifier.size(16.dp)
                )
            }
        }
    }
}
