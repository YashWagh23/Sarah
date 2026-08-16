package com.sarah.app.ui.screens.quickcapture

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.DocumentTextExtractor
import com.sarah.app.domain.engine.NaturalLanguageTaskParser
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.CaptureSourceType
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.ExtractedTaskDraft
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuickCaptureViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val parser: NaturalLanguageTaskParser,
    private val textExtractor: DocumentTextExtractor,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val deadlineReminderEngine: DeadlineReminderEngine,
    private val preferencesManager: SarahPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuickCaptureUiState())
    val uiState: StateFlow<QuickCaptureUiState> = _uiState.asStateFlow()

    init {
        subjectRepository.getActiveSubjects()
            .onEach { subjects ->
                _uiState.update { it.copy(availableSubjects = subjects) }
            }.launchIn(viewModelScope)
    }

    fun selectTab(tab: QuickCaptureTab) {
        _uiState.update { it.copy(selectedTab = tab, errorMessage = null) }
    }

    fun updateNaturalLanguageInput(input: String) {
        _uiState.update { it.copy(naturalLanguageInput = input) }
        if (input.trim().length >= 3) {
            val parsedDraft = parser.parse(
                rawText = input,
                availableSubjects = _uiState.value.availableSubjects,
                sourceType = CaptureSourceType.NATURAL_LANGUAGE
            )
            _uiState.update { it.copy(draft = parsedDraft) }
        } else {
            _uiState.update { it.copy(draft = null) }
        }
    }

    fun processImageUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Reading notice image with on-device OCR...",
                    errorMessage = null
                )
            }
            try {
                val extractedText = textExtractor.extractFromImage(context, uri)
                if (extractedText.isNotBlank()) {
                    val parsedDraft = parser.parse(
                        rawText = extractedText,
                        availableSubjects = _uiState.value.availableSubjects,
                        sourceType = CaptureSourceType.IMAGE_GALLERY
                    )
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            draft = parsedDraft,
                            naturalLanguageInput = extractedText.take(150)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "No readable text detected in the selected image."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Image processing failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun processPdfUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isProcessing = true,
                    processingMessage = "Extracting academic content from PDF document...",
                    errorMessage = null
                )
            }
            try {
                val extractedText = textExtractor.extractFromPdf(context, uri)
                if (extractedText.isNotBlank() && !extractedText.startsWith("PDF extraction error")) {
                    val parsedDraft = parser.parse(
                        rawText = extractedText,
                        availableSubjects = _uiState.value.availableSubjects,
                        sourceType = CaptureSourceType.PDF_DOCUMENT
                    )
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            draft = parsedDraft,
                            naturalLanguageInput = extractedText.take(150)
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Unable to extract text from the selected PDF document."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "PDF import failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun updateDraftFields(
        title: String? = null,
        subjectId: Long? = null,
        type: TaskType? = null,
        description: String? = null,
        deadlineEpochMs: Long? = null,
        estimatedMinutes: Int? = null,
        priority: TaskPriority? = null,
        difficulty: Difficulty? = null,
        energyRequirement: EnergyRequirement? = null
    ) {
        val current = _uiState.value.draft ?: return
        val subjectName = if (subjectId != null) {
            _uiState.value.availableSubjects.find { it.id == subjectId }?.name
        } else current.subjectName

        val updated = current.copy(
            title = title ?: current.title,
            subjectId = subjectId ?: current.subjectId,
            subjectName = subjectName,
            type = type ?: current.type,
            description = description ?: current.description,
            deadlineEpochMs = deadlineEpochMs ?: current.deadlineEpochMs,
            estimatedMinutes = estimatedMinutes ?: current.estimatedMinutes,
            priority = priority ?: current.priority,
            difficulty = difficulty ?: current.difficulty,
            energyRequirement = energyRequirement ?: current.energyRequirement
        )
        _uiState.update { it.copy(draft = updated) }
    }

    fun saveDraftToPlan(onSuccess: () -> Unit) {
        val draft = _uiState.value.draft ?: return
        viewModelScope.launch {
            val task = draft.toTask()
            val newId = taskRepository.insertTask(task)

            if (preferencesManager.isDeadlineRemindersEnabled) {
                val reminders = deadlineReminderEngine.generateDeadlineReminders(task.copy(id = newId))
                reminders.forEach { rem ->
                    val remId = reminderRepository.insertReminder(rem)
                    reminderScheduler.scheduleReminder(rem.copy(id = remId))
                }
            }

            _uiState.update {
                it.copy(
                    isSavedSuccess = true,
                    draft = null,
                    naturalLanguageInput = ""
                )
            }
            onSuccess()
        }
    }

    fun reset() {
        _uiState.update {
            QuickCaptureUiState(availableSubjects = it.availableSubjects)
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val subjectRepository: SubjectRepository,
        private val parser: NaturalLanguageTaskParser,
        private val textExtractor: DocumentTextExtractor,
        private val reminderRepository: ReminderRepository,
        private val reminderScheduler: ReminderScheduler,
        private val deadlineReminderEngine: DeadlineReminderEngine,
        private val preferencesManager: SarahPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuickCaptureViewModel(
                taskRepository,
                subjectRepository,
                parser,
                textExtractor,
                reminderRepository,
                reminderScheduler,
                deadlineReminderEngine,
                preferencesManager
            ) as T
        }
    }
}
