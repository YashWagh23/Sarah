package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.FeasibilityReport
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FeasibilityEngine(
    private val agendaPlanner: AgendaPlanner = AgendaPlanner()
) {

    fun evaluateToday(
        tasks: List<Task>,
        schedule: CollegeSchedule,
        energyLevel: EnergyLevel,
        currentTime: LocalTime = LocalTime.now(),
        currentDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): FeasibilityReport {
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val sleepMinutes = schedule.sleepTimeMinutes
        val minutesUntilSleep = max(0, sleepMinutes - currentMinutes)

        // Calculate college & commute remaining overlap if student is currently in college or commuting
        val collegeEndWithCommute = schedule.collegeEndTimeMinutes + schedule.commuteMinutes
        val remainingCollegeAndCommute = if (currentMinutes < collegeEndWithCommute) {
            max(0, collegeEndWithCommute - currentMinutes)
        } else {
            0
        }

        // Buffer for dinner / rest
        val dinnerBuffer = if (currentMinutes < 20 * 60) schedule.dinnerBufferMinutes else 0

        // Raw study minutes available before sleep
        val rawAvailableMinutes = max(0, minutesUntilSleep - remainingCollegeAndCommute - dinnerBuffer)

        // Realistic productive capacity adjusted by energy focus multiplier
        val realisticProductiveMinutes = (rawAvailableMinutes * energyLevel.focusMultiplier).roundToInt()

        // Filter pending or in-progress tasks
        val activeTasks = tasks.filter { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }

        // Triage tasks into MUST_DO, SHOULD_DO, CAN_DEFER based on deadline, urgency, and priority
        val mustDoList = mutableListOf<Task>()
        val shouldDoList = mutableListOf<Task>()
        val canDeferList = mutableListOf<Task>()

        val todayMidnightEpochMs = currentDate.atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()
        val tomorrowMidnightEpochMs = currentDate.plusDays(1).atTime(23, 59, 59).atZone(zoneId).toInstant().toEpochMilli()

        for (task in activeTasks) {
            val isDueTodayOrTomorrowMorning = task.deadlineEpochMs <= tomorrowMidnightEpochMs
            val isCriticalOrHigh = task.priority == TaskPriority.CRITICAL || task.priority == TaskPriority.HIGH
            val isHighUrgencyType = task.type == TaskType.ASSIGNMENT || task.type == TaskType.PRACTICAL || task.type == TaskType.SUBMISSION

            if (isDueTodayOrTomorrowMorning && (isCriticalOrHigh || isHighUrgencyType)) {
                mustDoList.add(task)
            } else if (isDueTodayOrTomorrowMorning || isCriticalOrHigh || task.type == TaskType.REVISION || task.type == TaskType.EXAM_PREP) {
                shouldDoList.add(task)
            } else {
                canDeferList.add(task)
            }
        }

        // Sort each bucket intelligently: Critical priority first, then closer deadline, then shorter tasks
        val taskComparator = compareByDescending<Task> { it.priority.level }
            .thenBy { it.deadlineEpochMs }
            .thenBy { it.estimatedMinutes }

        mustDoList.sortWith(taskComparator)
        shouldDoList.sortWith(taskComparator)
        canDeferList.sortWith(taskComparator)

        // When exhausted or low energy, adjust triage to protect mental health
        if (energyLevel == EnergyLevel.EXHAUSTED) {
            // Demote non-critical high-energy tasks to CAN_DEFER
            val demoted = shouldDoList.filter { it.energyRequirement == EnergyRequirement.HIGH }
            shouldDoList.removeAll(demoted)
            canDeferList.addAll(demoted)
        }

        val mustDoMinutes = mustDoList.sumOf { it.estimatedMinutes }
        val shouldDoMinutes = shouldDoList.sumOf { it.estimatedMinutes }
        val totalRequiredMinutes = mustDoMinutes + shouldDoMinutes

        // Determine feasibility status
        val status = when {
            mustDoMinutes > realisticProductiveMinutes -> FeasibilityStatus.OVERLOADED
            totalRequiredMinutes > realisticProductiveMinutes -> FeasibilityStatus.TIGHT
            totalRequiredMinutes <= (realisticProductiveMinutes * 0.75f) -> FeasibilityStatus.OPTIMAL
            else -> FeasibilityStatus.MANAGEABLE
        }

        val guidanceMessage = when (status) {
            FeasibilityStatus.OVERLOADED -> {
                "Tonight is overloaded. You have ${formatMinutes(realisticProductiveMinutes)} productive time vs ${formatMinutes(mustDoMinutes)} urgent work. Focus only on high-risk tasks and defer the rest."
            }
            FeasibilityStatus.TIGHT -> {
                "Workload is tight tonight (${formatMinutes(totalRequiredMinutes)} needed vs ${formatMinutes(realisticProductiveMinutes)} productive time). Finish Must-Dos first before starting revision."
            }
            FeasibilityStatus.MANAGEABLE -> {
                "You have ${formatMinutes(realisticProductiveMinutes)} productive study time. You can finish your Must-Dos and make solid progress on revision."
            }
            FeasibilityStatus.OPTIMAL -> {
                "Great pace today! You have plenty of time (${formatMinutes(realisticProductiveMinutes)}) to finish your academic goals and unwind comfortably."
            }
        }

        // Generate dynamic suggested agenda
        val tasksToSchedule = mustDoList + shouldDoList
        val suggestedAgenda = agendaPlanner.generateAgenda(
            tasks = tasksToSchedule,
            currentMinutes = currentMinutes,
            sleepMinutes = sleepMinutes,
            energyLevel = energyLevel,
            schedule = schedule
        )

        return FeasibilityReport(
            currentTimeMinutes = currentMinutes,
            sleepTimeMinutes = sleepMinutes,
            minutesUntilSleep = minutesUntilSleep,
            rawAvailableMinutes = rawAvailableMinutes,
            realisticProductiveMinutes = realisticProductiveMinutes,
            totalRequiredMinutes = totalRequiredMinutes,
            mustDoMinutes = mustDoMinutes,
            status = status,
            mustDoTasks = mustDoList,
            shouldDoTasks = shouldDoList,
            canDeferTasks = canDeferList,
            suggestedAgenda = suggestedAgenda,
            guidanceMessage = guidanceMessage
        )
    }

    private fun formatMinutes(mins: Int): String {
        val hours = mins / 60
        val remainder = mins % 60
        return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
    }
}
