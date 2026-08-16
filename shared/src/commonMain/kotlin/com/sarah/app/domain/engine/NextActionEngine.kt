package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.PlanItem
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.SchedulePaceStatus
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NextActionEngine {

    /**
     * Deterministically derives the single best NextAction from the current DailyPlan,
     * student energy, schedule constraints, and clock time.
     */
    fun computeNextAction(
        plan: DailyPlan,
        tasks: List<Task>,
        schedule: CollegeSchedule,
        currentMinutesInput: Int? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): NextAction {
        val currentMinutes = currentMinutesInput ?: run {
            val localTime = Clock.System.now().toLocalDateTime(timeZone)
            localTime.hour * 60 + localTime.minute
        }
        val sleepMinutes = schedule.sleepTimeMinutes
        val activeTasks = tasks.filter { it.status != TaskStatus.COMPLETED && it.remainingMinutes > 0 }

        // 1. Bedtime / Past Sleep Boundary Protection
        if (currentMinutes >= sleepMinutes) {
            return NextAction(
                title = "Time for Sleep & Rest",
                subtitle = "Bedtime reached (${schedule.formattedSleepTime})",
                actionType = NextActionType.STOP_FOR_TONIGHT,
                taskId = null,
                durationMinutes = 480,
                reason = "Bedtime boundary reached. Rest now to protect your focus and energy for tomorrow.",
                urgencyBadge = "REST"
            )
        }

        // 2. All Tasks Completed / No Remaining Work
        if (activeTasks.isEmpty()) {
            val minutesToSleep = maxOf(0, sleepMinutes - currentMinutes)
            return NextAction(
                title = "All Done for Tonight!",
                subtitle = "All planned tasks completed",
                actionType = NextActionType.STOP_FOR_TONIGHT,
                taskId = null,
                durationMinutes = minutesToSleep,
                reason = "You have cleared all academic goals for today. Relax and enjoy your evening.",
                urgencyBadge = "COMPLETE"
            )
        }

        // 3. Significant Schedule Delay / Behind Schedule Recovery
        val paceStatus = plan.scheduleProgress.paceStatus
        val minutesBehind = plan.scheduleProgress.minutesBehindSchedule
        if (paceStatus == SchedulePaceStatus.SIGNIFICANTLY_BEHIND || minutesBehind >= 30) {
            return NextAction(
                title = "Readjust Schedule Pace",
                subtitle = "${minutesBehind}m behind schedule",
                actionType = NextActionType.RECOVER_FROM_DELAY,
                taskId = null,
                durationMinutes = 5,
                reason = "You are currently running behind schedule by ${minutesBehind}m. Let's adapt your timeline.",
                urgencyBadge = "ADAPT"
            )
        }

        // 4. Exhausted Energy Boundary
        if (plan.currentEnergy == EnergyLevel.EXHAUSTED) {
            val minutesToSleep = sleepMinutes - currentMinutes
            if (minutesToSleep <= 30 && activeTasks.all { it.priority != TaskPriority.CRITICAL }) {
                return NextAction(
                    title = "Call It a Night",
                    subtitle = "Energy depleted (${minutesToSleep}m to sleep)",
                    actionType = NextActionType.STOP_FOR_TONIGHT,
                    taskId = null,
                    durationMinutes = minutesToSleep,
                    reason = "Energy is exhausted. Pushing through now causes cognitive fatigue with diminishing returns.",
                    urgencyBadge = "RECHARGE"
                )
            }
        }

        // 5. Active Plan Item Check (currently ongoing slot)
        val activeItem = plan.items.firstOrNull { it.startTimeMinutes <= currentMinutes && currentMinutes < it.endTimeMinutes }
        if (activeItem != null) {
            return resolveActionFromItem(activeItem, tasks, currentMinutes, sleepMinutes)
        }

        // 6. Next Upcoming Plan Item (between slots or upcoming)
        val nextItem = plan.items.firstOrNull { it.endTimeMinutes > currentMinutes }
        if (nextItem != null) {
            return resolveActionFromItem(nextItem, tasks, currentMinutes, sleepMinutes)
        }

        // 7. Fallback: If no item matches but tasks remain before bedtime
        val topTask = activeTasks.first()
        val sessionDuration = minOf(topTask.remainingMinutes, minOf(45, maxOf(15, sleepMinutes - currentMinutes)))
        return if (topTask.completedMinutes > 0) {
            NextAction(
                title = "Continue: ${topTask.title}",
                subtitle = "${topTask.subjectName ?: topTask.type.displayName} • ${topTask.remainingMinutes}m remaining",
                actionType = NextActionType.CONTINUE_TASK,
                taskId = topTask.id,
                durationMinutes = sessionDuration,
                reason = "Resume progress on ${topTask.title}.",
                urgencyBadge = if (topTask.priority == TaskPriority.CRITICAL) "CRITICAL" else "FOCUS"
            )
        } else {
            NextAction(
                title = "Start: ${topTask.title}",
                subtitle = "${topTask.subjectName ?: topTask.type.displayName} • ${sessionDuration}m focus block",
                actionType = NextActionType.START_TASK,
                taskId = topTask.id,
                durationMinutes = sessionDuration,
                reason = "Top priority task ready to begin.",
                urgencyBadge = if (topTask.priority == TaskPriority.CRITICAL) "CRITICAL" else "NEXT UP"
            )
        }
    }

    private fun resolveActionFromItem(
        item: PlanItem,
        tasks: List<Task>,
        currentMinutes: Int,
        sleepMinutes: Int
    ): NextAction {
        val remainingSlotMinutes = maxOf(1, item.endTimeMinutes - maxOf(currentMinutes, item.startTimeMinutes))

        return when (item.type) {
            PlanItemType.MEAL -> {
                NextAction(
                    title = item.taskTitle,
                    subtitle = item.subjectName ?: "Dinner & Rest",
                    actionType = NextActionType.MEAL,
                    taskId = null,
                    durationMinutes = remainingSlotMinutes,
                    reason = "Scheduled dinner/meal buffer to restore your physical energy.",
                    urgencyBadge = "MEAL"
                )
            }
            PlanItemType.BREAK -> {
                NextAction(
                    title = item.taskTitle,
                    subtitle = item.subjectName ?: "Pacing break",
                    actionType = NextActionType.TAKE_BREAK,
                    taskId = null,
                    durationMinutes = remainingSlotMinutes,
                    reason = item.reason.ifBlank { "Rest your eyes and stretch before the next focus block." },
                    urgencyBadge = "BREAK"
                )
            }
            PlanItemType.REST, PlanItemType.UNAVAILABLE, PlanItemType.COLLEGE, PlanItemType.COMMUTE -> {
                NextAction(
                    title = item.taskTitle,
                    subtitle = item.subjectName ?: "Scheduled buffer",
                    actionType = NextActionType.REST,
                    taskId = null,
                    durationMinutes = remainingSlotMinutes,
                    reason = item.reason.ifBlank { "Scheduled non-study commitment." },
                    urgencyBadge = "REST"
                )
            }
            PlanItemType.WIND_DOWN -> {
                NextAction(
                    title = "Wind Down for Bed",
                    subtitle = "Relaxation buffer",
                    actionType = NextActionType.REST,
                    taskId = null,
                    durationMinutes = maxOf(10, sleepMinutes - currentMinutes),
                    reason = "Academic tasks are completed for tonight. Wind down before sleep.",
                    urgencyBadge = "WIND DOWN"
                )
            }
            PlanItemType.TASK -> {
                val task = tasks.firstOrNull { it.id == item.taskId }
                val taskTitle = task?.title ?: item.taskTitle
                val taskSubject = task?.subjectName ?: item.subjectName ?: "Study"
                val taskRemaining = task?.remainingMinutes ?: item.durationMinutes
                val effectiveDuration = minOf(item.durationMinutes, taskRemaining)

                val isContinuing = (task?.completedMinutes ?: 0) > 0 || item.status == PlanItemStatus.IN_PROGRESS

                if (isContinuing) {
                    NextAction(
                        title = "Continue: $taskTitle",
                        subtitle = "$taskSubject • ${taskRemaining}m remaining",
                        actionType = NextActionType.CONTINUE_TASK,
                        taskId = task?.id ?: item.taskId,
                        durationMinutes = effectiveDuration,
                        reason = item.reason.ifBlank { "Resume progress on $taskTitle." },
                        urgencyBadge = if (task?.priority == TaskPriority.CRITICAL) "CRITICAL" else "FOCUS"
                    )
                } else {
                    NextAction(
                        title = "Start: $taskTitle",
                        subtitle = "$taskSubject • ${effectiveDuration}m focus session",
                        actionType = NextActionType.START_TASK,
                        taskId = task?.id ?: item.taskId,
                        durationMinutes = effectiveDuration,
                        reason = item.reason.ifBlank { "Top scheduled priority for tonight." },
                        urgencyBadge = if (task?.priority == TaskPriority.CRITICAL) "CRITICAL" else "NEXT UP"
                    )
                }
            }
        }
    }
}
