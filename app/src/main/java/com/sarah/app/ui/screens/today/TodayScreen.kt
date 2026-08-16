@file:JvmName("TodayScreenAndroidKt")
package com.sarah.app.ui.screens.today

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sarah.app.ui.components.QuickCaptureBottomSheet
import com.sarah.app.ui.screens.quickcapture.QuickCaptureViewModel

@Composable
fun TodayScreen(
    viewModel: TodayViewModel,
    quickCaptureViewModel: QuickCaptureViewModel,
    onNavigateToNotes: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isQuickCaptureOpen by remember { mutableStateOf(false) }

    TodayScreenContent(
        uiState = uiState,
        onPrimaryNextActionClick = { viewModel.handlePrimaryNextAction(it) },
        onMarkNextActionCompletedClick = { viewModel.completeTaskById(it) },
        onToggleTask = { viewModel.toggleTaskStatus(it) },
        onAddReminder = { title, message, timeEpochMs, subjectId ->
            viewModel.createCustomReminder(title, message, timeEpochMs, subjectId)
        },
        onSnoozeReminderMinutes = { id, mins -> viewModel.snoozeReminder(id, mins) },
        onSnoozeReminderUntil = { id, epochMs -> viewModel.snoozeReminderUntil(id, epochMs) },
        onDismissReminder = { viewModel.dismissReminder(it) },
        onDeleteReminder = { viewModel.deleteReminder(it) },
        onOpenQuickCapture = { isQuickCaptureOpen = true },
        onNavigateToNotes = onNavigateToNotes,
        modifier = modifier
    )

    if (isQuickCaptureOpen) {
        QuickCaptureBottomSheet(
            viewModel = quickCaptureViewModel,
            onDismiss = { isQuickCaptureOpen = false }
        )
    }
}
