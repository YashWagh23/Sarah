package com.sarah.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferencesManager
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: SarahPreferencesManager,
    private val feasibilityEngine: FeasibilityEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        combine(
            taskRepository.getAllTasks(),
            subjectRepository.getActiveSubjects(),
            scheduleRepository.getSchedule(),
            userRepository.getUserProfile(),
            preferencesManager.energyLevelFlow
        ) { tasks, subjects, schedule, profile, energy ->
            val safeSchedule = schedule ?: CollegeSchedule()
            val hour = LocalTime.now().hour
            val greeting = when {
                hour < 12 -> "Good Morning"
                hour < 17 -> "Good Afternoon"
                hour < 21 -> "Good Evening"
                else -> "Good Night"
            }

            val feasibilityReport = feasibilityEngine.evaluateToday(
                tasks = tasks,
                schedule = safeSchedule,
                energyLevel = energy
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    greeting = greeting,
                    userProfile = profile,
                    schedule = safeSchedule,
                    energyLevel = energy,
                    tasks = tasks,
                    subjects = subjects,
                    feasibilityReport = feasibilityReport
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setEnergyLevel(energyLevel: EnergyLevel) {
        preferencesManager.currentEnergyLevel = energyLevel
        // The reactive energyLevelFlow will automatically trigger an evaluation update
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.updateTaskStatus(task.id, newStatus)
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val subjectRepository: SubjectRepository,
        private val scheduleRepository: ScheduleRepository,
        private val userRepository: UserRepository,
        private val preferencesManager: SarahPreferencesManager,
        private val feasibilityEngine: FeasibilityEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(
                taskRepository,
                subjectRepository,
                scheduleRepository,
                userRepository,
                preferencesManager,
                feasibilityEngine
            ) as T
        }
    }
}
