package com.sarah.app.ui.screens.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.HumanLanguageHelper
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.DailyPlanRepository
import com.sarah.app.domain.repository.ReminderRepository
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
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TodayViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository,
    private val preferencesManager: SarahPreferences,
    private val feasibilityEngine: FeasibilityEngine,
    private val dailyPlanRepository: DailyPlanRepository,
    private val adaptivePlanner: AdaptivePlanner,
    private val nextActionEngine: NextActionEngine,
    private val reminderRepository: ReminderRepository,
    private val reminderScheduler: ReminderScheduler,
    private val deadlineReminderEngine: DeadlineReminderEngine
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
            scheduleRepository.getSchedule(),
            reminderRepository.getActiveUpcomingReminders()
        ) { tasks, subjects, schedule, reminders ->
            data class CoreQuad(
                val tasks: List<Task>,
                val subjects: List<com.sarah.app.domain.model.Subject>,
                val schedule: CollegeSchedule,
                val reminders: List<Reminder>
            )
            CoreQuad(tasks, subjects, schedule ?: CollegeSchedule(), reminders)
        }

        combine(
            coreDataFlow,
            userRepository.getUserProfile(),
            preferencesManager.energyLevelFlow,
            dailyPlanRepository.getInterruptions(todayEpochDay)
        ) { quad, profile, energy, interruptions ->
            val tasks = quad.tasks
            val subjects = quad.subjects
            val safeSchedule = quad.schedule
            val upcomingReminders = quad.reminders

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

            // Calculate daily summary
            val pendingTasksCount = tasks.count { it.status != TaskStatus.COMPLETED }
            val tomorrowDate = nowDate.plusDays(1)
            val tomorrowDeadlinesCount = tasks.count { task ->
                if (task.status == TaskStatus.COMPLETED) false
                else {
                    val taskDate = Instant.ofEpochMilli(task.deadlineEpochMs).atZone(ZoneId.systemDefault()).toLocalDate()
                    taskDate == tomorrowDate
                }
            }
            val dailySummary = HumanLanguageHelper.formatDailySummary(
                pendingTasksCount = pendingTasksCount,
                tomorrowDeadlinesCount = tomorrowDeadlinesCount,
                remindersCount = upcomingReminders.size
            )

            val humanHeadline = HumanLanguageHelper.formatFeasibilityHeadline(feasibilityReport)
            val humanSubtext = HumanLanguageHelper.formatFeasibilitySubtext(feasibilityReport)
            val humanCapacity = HumanLanguageHelper.formatCapacitySummary(feasibilityReport.realisticProductiveMinutes)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    greeting = greeting,
                    userProfile = profile,
                    schedule = safeSchedule,
                    energyLevel = energy,
                    tasks = tasks,
                    subjects = subjects,
                    upcomingReminders = upcomingReminders,
                    dailySummary = dailySummary,
                    humanFeasibilityHeadline = humanHeadline,
                    humanFeasibilitySubtext = humanSubtext,
                    humanCapacitySummary = humanCapacity,
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

            if (newStatus == TaskStatus.COMPLETED) {
                // Cancel active reminders for completed task
                reminderScheduler.cancelTaskReminders(task.id)
                reminderRepository.deleteRemindersByTaskId(task.id)
            } else if (preferencesManager.isDeadlineRemindersEnabled) {
                // Reschedule deadline reminders for restored task
                val reminders = deadlineReminderEngine.generateDeadlineReminders(task.copy(status = TaskStatus.PENDING))
                reminders.forEach { rem ->
                    val remId = reminderRepository.insertReminder(rem)
                    reminderScheduler.scheduleReminder(rem.copy(id = remId))
                }
            }
        }
    }

    fun completeTaskById(taskId: Long) {
        viewModelScope.launch {
            taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
            reminderScheduler.cancelTaskReminders(taskId)
            reminderRepository.deleteRemindersByTaskId(taskId)
        }
    }

    fun createCustomReminder(title: String, message: String, timeEpochMs: Long, subjectId: Long?) {
        viewModelScope.launch {
            val taskTitle = if (subjectId != null) {
                _uiState.value.subjects.find { it.id == subjectId }?.name
            } else null

            val reminder = Reminder(
                taskId = null,
                taskTitle = taskTitle,
                title = title,
                message = message,
                reminderTimeEpochMs = timeEpochMs,
                type = ReminderType.CUSTOM_REMINDER
            )
            val newId = reminderRepository.insertReminder(reminder)
            reminderScheduler.scheduleReminder(reminder.copy(id = newId))
        }
    }

    fun snoozeReminder(reminderId: Long, snoozeDurationMinutes: Int) {
        viewModelScope.launch {
            reminderRepository.snoozeReminder(reminderId, snoozeDurationMinutes)
            val updated = reminderRepository.getReminderById(reminderId)
            if (updated != null) {
                reminderScheduler.scheduleReminder(updated)
            }
        }
    }

    fun snoozeReminderUntil(reminderId: Long, newTimeEpochMs: Long) {
        viewModelScope.launch {
            reminderRepository.snoozeReminderUntil(reminderId, newTimeEpochMs)
            val updated = reminderRepository.getReminderById(reminderId)
            if (updated != null) {
                reminderScheduler.scheduleReminder(updated)
            }
        }
    }

    fun dismissReminder(reminder: Reminder) {
        viewModelScope.launch {
            if (reminder.taskId != null) {
                // If it's a task/deadline reminder, mark the task completed too!
                taskRepository.updateTaskStatus(reminder.taskId, TaskStatus.COMPLETED)
                reminderRepository.deleteRemindersByTaskId(reminder.taskId)
                reminderScheduler.cancelTaskReminders(reminder.taskId)
            } else {
                reminderRepository.dismissReminder(reminder.id)
                reminderScheduler.cancelReminder(reminder.id)
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderScheduler.cancelReminder(reminder.id)
            reminderRepository.deleteReminder(reminder)
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
        private val nextActionEngine: NextActionEngine,
        private val reminderRepository: ReminderRepository,
        private val reminderScheduler: ReminderScheduler,
        private val deadlineReminderEngine: DeadlineReminderEngine
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
                nextActionEngine,
                reminderRepository,
                reminderScheduler,
                deadlineReminderEngine
            ) as T
        }
    }
}
