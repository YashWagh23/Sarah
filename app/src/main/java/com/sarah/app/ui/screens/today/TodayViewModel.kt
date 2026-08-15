package com.sarah.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.DailyPlanRepository
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: SarahPreferences,
    private val feasibilityEngine: FeasibilityEngine,
    private val dailyPlanRepository: DailyPlanRepository,
    private val adaptivePlanner: AdaptivePlanner,
    private val nextActionEngine: NextActionEngine
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodayUiState())
    val uiState: StateFlow<TodayUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        val todayEpochDay = LocalDate.now().toEpochDay()
        val coreDataFlow = combine(
            taskRepository.getAllTasks(),
            subjectRepository.getActiveSubjects(),
            scheduleRepository.getSchedule()
        ) { tasks, subjects, schedule ->
            Triple(tasks, subjects, schedule ?: CollegeSchedule())
        }

        combine(
            coreDataFlow,
            userRepository.getUserProfile(),
            preferencesManager.energyLevelFlow,
            dailyPlanRepository.getInterruptions(todayEpochDay)
        ) { (tasks, subjects, safeSchedule), profile, energy, interruptions ->
            val nowTime = LocalTime.now()
            val nowDate = LocalDate.now()
            val hour = nowTime.hour
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

            val rawPlan = adaptivePlanner.generatePlan(
                tasks = tasks,
                schedule = safeSchedule,
                energyLevel = energy,
                interruptions = interruptions,
                currentTime = nowTime,
                currentDate = nowDate
            )

            val nextAction = nextActionEngine.computeNextAction(
                plan = rawPlan,
                tasks = tasks,
                schedule = safeSchedule,
                currentTime = nowTime,
                currentDate = nowDate
            )

            val fullPlan = rawPlan.copy(nextAction = nextAction)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    greeting = greeting,
                    userProfile = profile,
                    schedule = safeSchedule,
                    energyLevel = energy,
                    tasks = tasks,
                    subjects = subjects,
                    feasibilityReport = feasibilityReport,
                    dailyPlan = fullPlan,
                    nextAction = nextAction
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setEnergyLevel(energyLevel: EnergyLevel) {
        preferencesManager.currentEnergyLevel = energyLevel
    }

    fun toggleTaskStatus(task: Task) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
            taskRepository.updateTaskStatus(task.id, newStatus)
        }
    }

    fun completeTaskById(taskId: Long) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
        }
    }

    fun handlePrimaryNextAction(nextAction: NextAction) {
        when (nextAction.actionType) {
            NextActionType.START_TASK, NextActionType.CONTINUE_TASK -> {
                // Focus session active or starting
            }
            NextActionType.STOP_FOR_TONIGHT -> {
                // Sleep / wrap-up acknowledgement
            }
            NextActionType.TAKE_BREAK, NextActionType.MEAL, NextActionType.REST -> {
                // Break / Meal buffer acknowledgement
            }
            NextActionType.RECOVER_FROM_DELAY -> {
                // Recalculates dynamically
            }
        }
    }

    class Factory(
        private val taskRepository: TaskRepository,
        private val subjectRepository: SubjectRepository,
        private val scheduleRepository: ScheduleRepository,
        private val userRepository: UserRepository,
        private val preferencesManager: SarahPreferences,
        private val feasibilityEngine: FeasibilityEngine,
        private val dailyPlanRepository: DailyPlanRepository,
        private val adaptivePlanner: AdaptivePlanner,
        private val nextActionEngine: NextActionEngine
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TodayViewModel(
                taskRepository,
                subjectRepository,
                scheduleRepository,
                userRepository,
                preferencesManager,
                feasibilityEngine,
                dailyPlanRepository,
                adaptivePlanner,
                nextActionEngine
            ) as T
        }
    }
}
