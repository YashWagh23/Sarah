package com.sarah.app.ui.screens.notes

import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.model.Subject

data class NotesUiState(
    val notes: List<AcademicNote> = emptyList(),
    val filteredNotes: List<AcademicNote> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val selectedSubjectId: Long? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isAddSheetOpen: Boolean = false,
    val editingNote: AcademicNote? = null,
    val reminderConversionNote: AcademicNote? = null,
    val userMessage: String? = null
) {
    val pinnedNotes: List<AcademicNote>
        get() = filteredNotes.filter { it.isPinned }

    val unpinnedNotes: List<AcademicNote>
        get() = filteredNotes.filter { !it.isPinned }
}
