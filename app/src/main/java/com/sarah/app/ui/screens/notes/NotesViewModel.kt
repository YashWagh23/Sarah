package com.sarah.app.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.AcademicNoteRepository
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotesViewModel(
    private val academicNoteRepository: AcademicNoteRepository,
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val deadlineReminderEngine: DeadlineReminderEngine,
    private val preferencesManager: SarahPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                academicNoteRepository.getAllNotes(),
                subjectRepository.getActiveSubjects()
            ) { allNotes, subjects ->
                Pair(allNotes, subjects)
            }.collect { (allNotes, subjects) ->
                _uiState.update { current ->
                    val filtered = applyFilterAndSearch(
                        notes = allNotes,
                        selectedSubjectId = current.selectedSubjectId,
                        query = current.searchQuery
                    )
                    current.copy(
                        notes = allNotes,
                        filteredNotes = filtered,
                        subjects = subjects,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { current ->
            val filtered = applyFilterAndSearch(
                notes = current.notes,
                selectedSubjectId = current.selectedSubjectId,
                query = query
            )
            current.copy(searchQuery = query, filteredNotes = filtered)
        }
    }

    fun setSelectedSubject(subjectId: Long?) {
        _uiState.update { current ->
            val filtered = applyFilterAndSearch(
                notes = current.notes,
                selectedSubjectId = subjectId,
                query = current.searchQuery
            )
            current.copy(selectedSubjectId = subjectId, filteredNotes = filtered)
        }
    }

    fun openAddSheet(noteToEdit: AcademicNote? = null) {
        _uiState.update { it.copy(isAddSheetOpen = true, editingNote = noteToEdit) }
    }

    fun closeAddSheet() {
        _uiState.update { it.copy(isAddSheetOpen = false, editingNote = null) }
    }

    fun saveNote(title: String, content: String, subjectId: Long?, isPinned: Boolean) {
        viewModelScope.launch {
            val currentEditing = _uiState.value.editingNote
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title,
                    content = content,
                    subjectId = subjectId,
                    isPinned = isPinned,
                    updatedEpochMs = System.currentTimeMillis()
                )
                academicNoteRepository.updateNote(updated)
                _uiState.update { it.copy(isAddSheetOpen = false, editingNote = null, userMessage = "Note updated") }
            } else {
                val newNote = AcademicNote(
                    title = title,
                    content = content,
                    subjectId = subjectId,
                    isPinned = isPinned,
                    createdEpochMs = System.currentTimeMillis(),
                    updatedEpochMs = System.currentTimeMillis()
                )
                academicNoteRepository.insertNote(newNote)
                _uiState.update { it.copy(isAddSheetOpen = false, editingNote = null, userMessage = "Note saved") }
            }
        }
    }

    fun togglePin(note: AcademicNote, isPinned: Boolean) {
        viewModelScope.launch {
            academicNoteRepository.togglePin(note.id, isPinned)
        }
    }

    fun deleteNote(note: AcademicNote) {
        viewModelScope.launch {
            academicNoteRepository.deleteNote(note)
            _uiState.update { it.copy(userMessage = "Note deleted") }
        }
    }

    fun convertNoteToTask(note: AcademicNote) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val tomorrowMs = now + (24 * 60 * 60 * 1000)
            val newTask = Task(
                id = 0,
                title = note.title,
                subjectId = note.subjectId,
                subjectName = note.subjectName,
                type = TaskType.ASSIGNMENT,
                description = note.content,
                deadlineEpochMs = tomorrowMs,
                estimatedMinutes = 45,
                priority = TaskPriority.MEDIUM,
                difficulty = Difficulty.MEDIUM,
                energyRequirement = EnergyRequirement.MEDIUM,
                status = TaskStatus.PENDING,
                completionPercentage = 0,
                createdAtEpochMs = now,
                completedAtEpochMs = null
            )
            val newTaskId = taskRepository.insertTask(newTask)

            // If deadline reminders enabled, generate & schedule
            if (preferencesManager.isDeadlineRemindersEnabled) {
                val reminders = deadlineReminderEngine.generateDeadlineReminders(newTask.copy(id = newTaskId))
                reminders.forEach { rem ->
                    val remId = reminderRepository.insertReminder(rem)
                    reminderScheduler.scheduleReminder(rem.copy(id = remId))
                }
            }

            _uiState.update { it.copy(userMessage = "Converted to Task: \"${note.title}\"") }
        }
    }

    fun openReminderConversion(note: AcademicNote) {
        _uiState.update { it.copy(reminderConversionNote = note) }
    }

    fun closeReminderConversion() {
        _uiState.update { it.copy(reminderConversionNote = null) }
    }

    fun createReminderFromNote(note: AcademicNote, reminderTimeEpochMs: Long) {
        viewModelScope.launch {
            val reminder = Reminder(
                taskId = null,
                taskTitle = note.subjectName,
                title = note.title,
                message = if (note.content.isNotBlank()) note.content else "Classroom note memo",
                reminderTimeEpochMs = reminderTimeEpochMs,
                type = ReminderType.CUSTOM_REMINDER
            )
            val newId = reminderRepository.insertReminder(reminder)
            reminderScheduler.scheduleReminder(reminder.copy(id = newId))
            _uiState.update {
                it.copy(
                    reminderConversionNote = null,
                    userMessage = "Reminder scheduled for note"
                )
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun applyFilterAndSearch(
        notes: List<AcademicNote>,
        selectedSubjectId: Long?,
        query: String
    ): List<AcademicNote> {
        val subjectFiltered = if (selectedSubjectId == null) {
            notes
        } else {
            notes.filter { it.subjectId == selectedSubjectId }
        }

        if (query.isBlank()) return subjectFiltered

        val cleanQuery = query.trim().lowercase()
        return subjectFiltered.filter { note ->
            val subName = note.subjectName
            note.title.lowercase().contains(cleanQuery) ||
                note.content.lowercase().contains(cleanQuery) ||
                (subName != null && subName.lowercase().contains(cleanQuery))
        }
    }

    class Factory(
        private val academicNoteRepository: AcademicNoteRepository,
        private val subjectRepository: SubjectRepository,
        private val taskRepository: TaskRepository,
        private val reminderRepository: ReminderRepository,
        private val reminderScheduler: ReminderScheduler,
        private val deadlineReminderEngine: DeadlineReminderEngine,
        private val preferencesManager: SarahPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(
                academicNoteRepository,
                subjectRepository,
                taskRepository,
                reminderRepository,
                reminderScheduler,
                deadlineReminderEngine,
                preferencesManager
            ) as T
        }
    }
}
