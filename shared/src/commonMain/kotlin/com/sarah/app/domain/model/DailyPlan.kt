package com.sarah.app.domain.model

import com.sarah.app.domain.util.currentTimeEpochMs

enum class PlanItemType(val displayName: String) {
    TASK("Task Session"),
    BREAK("Restorative Break"),
    MEAL("Meal / Dinner"),
    COLLEGE("College Hours"),
    COMMUTE("Commute"),
    UNAVAILABLE("Unavailable"),
    REST("Rest"),
    WIND_DOWN("Wind Down")
}

enum class PlanItemStatus(val displayName: String) {
    PLANNED("Planned"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    SKIPPED("Skipped"),
    RESCHEDULED("Rescheduled")
}

enum class SchedulePaceStatus(val displayName: String) {
    ON_TRACK("On Track"),
    SLIGHTLY_BEHIND("Slightly Behind"),
    SIGNIFICANTLY_BEHIND("Significantly Behind")
}

enum class NextActionType {
    START_TASK,
    CONTINUE_TASK,
    TAKE_BREAK,
    MEAL,
    REST,
    STOP_FOR_TONIGHT,
    RECOVER_FROM_DELAY
}

enum class ForecastHorizon(val displayName: String) {
    TODAY("Today"),
    TOMORROW("Tomorrow"),
    NEXT_3_DAYS("Next 3 Days"),
    NEXT_7_DAYS("Next 7 Days")
}

data class PlanItem(
    val id: Long = 0,
    val dailyPlanId: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String,
    val subjectName: String? = null,
    val type: PlanItemType = PlanItemType.TASK,
    val status: PlanItemStatus = PlanItemStatus.PLANNED,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val durationMinutes: Int,
    val orderIndex: Int = 0,
    val reason: String = "",
    val isBreak: Boolean = false
)

data class ScheduleProgress(
    val completedMinutes: Int = 0,
    val totalPlannedMinutes: Int = 0,
    val completedTaskCount: Int = 0,
    val totalTaskCount: Int = 0,
    val minutesBehindSchedule: Int = 0,
    val paceStatus: SchedulePaceStatus = SchedulePaceStatus.ON_TRACK
)

data class NextAction(
    val title: String,
    val subtitle: String,
    val actionType: NextActionType = NextActionType.START_TASK,
    val taskId: Long? = null,
    val durationMinutes: Int = 0,
    val reason: String = "",
    val urgencyBadge: String = ""
)

data class WorkloadForecast(
    val horizon: ForecastHorizon,
    val requiredMinutes: Int,
    val capacityMinutes: Int,
    val overloadRatio: Float,
    val riskStatus: FeasibilityStatus,
    val taskCount: Int,
    val urgentDeadlineCount: Int
)

data class TemporaryInterruption(
    val id: Long = 0,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val dateEpochDay: Long = currentTimeEpochMs() / 86_400_000L
)

data class DailyPlan(
    val id: Long = 0,
    val dateEpochDay: Long = currentTimeEpochMs() / 86_400_000L,
    val generatedAtEpochMs: Long = currentTimeEpochMs(),
    val updatedAtEpochMs: Long = currentTimeEpochMs(),
    val availableMinutes: Int = 0,
    val realisticCapacityMinutes: Int = 0,
    val requiredMinutes: Int = 0,
    val feasibilityStatus: FeasibilityStatus = FeasibilityStatus.MANAGEABLE,
    val currentEnergy: EnergyLevel = EnergyLevel.NORMAL,
    val items: List<PlanItem> = emptyList(),
    val deferredTasks: List<Task> = emptyList(),
    val scheduleProgress: ScheduleProgress = ScheduleProgress(),
    val nextAction: NextAction? = null
)
