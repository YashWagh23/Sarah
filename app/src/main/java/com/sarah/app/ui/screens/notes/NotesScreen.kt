@file:JvmName("NotesScreenAndroidKt")
package com.sarah.app.ui.screens.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    NotesScreenContent(
        uiState = uiState,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onSelectSubject = { viewModel.setSelectedSubject(it) },
        onOpenAddSheet = { viewModel.openAddSheet(it) },
        onCloseAddSheet = { viewModel.closeAddSheet() },
        onSaveNote = { title, content, subjectId, isPinned ->
            viewModel.saveNote(title, content, subjectId, isPinned)
        },
        onTogglePin = { note, isPinned -> viewModel.togglePin(note, isPinned) },
        onConvertToTask = { viewModel.convertNoteToTask(it) },
        onOpenReminderConversion = { viewModel.openReminderConversion(it) },
        onCloseReminderConversion = { viewModel.closeReminderConversion() },
        onSaveReminderFromNote = { note, title, message, timeEpochMs ->
            viewModel.createReminderFromNote(note.copy(title = title, content = message), timeEpochMs)
        },
        onDeleteNote = { viewModel.deleteNote(it) },
        onClearUserMessage = { viewModel.clearUserMessage() },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}
