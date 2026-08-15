package com.sarah.app.ui.screens.subjects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubjectWithTaskCount(
    val subject: Subject,
    val pendingTasksCount: Int
)

data class SubjectsUiState(
    val isLoading: Boolean = true,
    val subjectsWithCount: List<SubjectWithTaskCount> = emptyList(),
    val isAddEditDialogOpen: Boolean = false,
    val editingSubject: Subject? = null
)

class SubjectsViewModel(
    private val subjectRepository: SubjectRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubjectsUiState())
    val uiState: StateFlow<SubjectsUiState> = _uiState.asStateFlow()

    init {
        combine(
            subjectRepository.getAllSubjects(),
            taskRepository.getActiveTasks()
        ) { subjects, activeTasks ->
            val list = subjects.map { subj ->
                val count = activeTasks.count { it.subjectId == subj.id && it.status != TaskStatus.COMPLETED }
                SubjectWithTaskCount(subject = subj, pendingTasksCount = count)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    subjectsWithCount = list
                )
            }
        }.launchIn(viewModelScope)
    }

    fun openAddSubjectDialog() {
        _uiState.update { it.copy(isAddEditDialogOpen = true, editingSubject = null) }
    }

    fun openEditSubjectDialog(subject: Subject) {
        _uiState.update { it.copy(isAddEditDialogOpen = true, editingSubject = subject) }
    }

    fun closeAddEditDialog() {
        _uiState.update { it.copy(isAddEditDialogOpen = false, editingSubject = null) }
    }

    fun saveSubject(
        name: String,
        code: String,
        professorName: String,
        colorHex: String,
        weeklyHours: Int,
        targetAttendancePercentage: Int,
        currentAttendancePercentage: Int
    ) {
        viewModelScope.launch {
            val existing = _uiState.value.editingSubject
            if (existing != null) {
                subjectRepository.updateSubject(
                    existing.copy(
                        name = name,
                        code = code,
                        professorName = professorName,
                        colorHex = colorHex,
                        weeklyHours = weeklyHours,
                        targetAttendancePercentage = targetAttendancePercentage,
                        currentAttendancePercentage = currentAttendancePercentage
                    )
                )
            } else {
                subjectRepository.insertSubject(
                    Subject(
                        name = name,
                        code = code,
                        professorName = professorName,
                        colorHex = colorHex,
                        weeklyHours = weeklyHours,
                        targetAttendancePercentage = targetAttendancePercentage,
                        currentAttendancePercentage = currentAttendancePercentage
                    )
                )
            }
            closeAddEditDialog()
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch {
            subjectRepository.deleteSubject(subject)
            closeAddEditDialog()
        }
    }

    class Factory(
        private val subjectRepository: SubjectRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SubjectsViewModel(subjectRepository, taskRepository) as T
        }
    }
}
