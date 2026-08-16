package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.PlanItem
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.SchedulePaceStatus
import com.sarah.app.domain.model.ScheduleProgress
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskBucket
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TemporaryInterruption
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class BlockedTimeInterval(
    val startMinutes: Int,
    val endMinutes: Int,
    val type: PlanItemType,
    val title: String,
    val subtitle: String = ""
)

class AdaptivePlanner(
    private val priorityScorer: TaskPriorityScorer = TaskPriorityScorer()
) {

    fun generatePlan(
        tasks: List<Task>,
        schedule: CollegeSchedule,
        energyLevel: EnergyLevel,
        interruptions: List<TemporaryInterruption> = emptyList(),
        currentMinutesInput: Int? = null,
        currentDateInput: LocalDate? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): DailyPlan {
        val now = Clock.System.now()
        val localDateTime = now.toLocalDateTime(timeZone)
        val currentMinutes = currentMinutesInput ?: (localDateTime.hour * 60 + localDateTime.minute)
        val currentDate = currentDateInput ?: localDateTime.date
        val sleepMinutes = schedule.sleepTimeMinutes
        val todayEpochDay = currentDate.toEpochDays().toLong()
        val currentEpochMs = now.toEpochMilliseconds()

        // 1. If currently past bedtime, return minimal wind down/sleep state
        if (currentMinutes >= sleepMinutes) {
            return DailyPlan(
                dateEpochDay = todayEpochDay,
                generatedAtEpochMs = currentEpochMs,
                updatedAtEpochMs = currentEpochMs,
                availableMinutes = 0,
                realisticCapacityMinutes = 0,
                requiredMinutes = tasks.filter { it.status != TaskStatus.COMPLETED }.sumOf { it.remainingMinutes },
                feasibilityStatus = FeasibilityStatus.TIGHT,
                currentEnergy = energyLevel,
                items = listOf(
                    PlanItem(
                        taskTitle = "Bedtime & Sleep",
                        subjectName = "Restorative Rest",
                        type = PlanItemType.REST,
                        status = PlanItemStatus.PLANNED,
                        startTimeMinutes = sleepMinutes,
                        endTimeMinutes = min(sleepMinutes + 480, 24 * 60),
                        durationMinutes = 480,
                        orderIndex = 0,
                        reason = "Scheduled sleep boundary",
                        isBreak = true
                    )
                ),
                deferredTasks = tasks.filter { it.status != TaskStatus.COMPLETED },
                scheduleProgress = ScheduleProgress(
                    completedMinutes = tasks.filter { it.status == TaskStatus.COMPLETED }.sumOf { it.estimatedMinutes },
                    totalPlannedMinutes = 0,
                    completedTaskCount = tasks.count { it.status == TaskStatus.COMPLETED },
                    totalTaskCount = tasks.size,
                    paceStatus = SchedulePaceStatus.ON_TRACK
                )
            )
        }

        // 2. Identify all blocked non-study intervals between currentMinutes and sleepMinutes
        val blockedIntervals = mutableListOf<BlockedTimeInterval>()

        // College & Commute overlap if student is currently at college or commuting
        val collegeEndWithCommute = schedule.collegeEndTimeMinutes + schedule.commuteMinutes
        if (currentMinutes < collegeEndWithCommute) {
            val blockEnd = min(collegeEndWithCommute, sleepMinutes)
            blockedIntervals.add(
                BlockedTimeInterval(
                    startMinutes = currentMinutes,
                    endMinutes = blockEnd,
                    type = if (currentMinutes < schedule.collegeEndTimeMinutes) PlanItemType.COLLEGE else PlanItemType.COMMUTE,
                    title = if (currentMinutes < schedule.collegeEndTimeMinutes) "College Hours" else "Commute Home",
                    subtitle = "Dedicated academic / travel buffer"
                )
            )
        }

        // Dinner / Meal Buffer (e.g. 19:00 - 19:45 if currently before 19:00)
        val dinnerStart = 19 * 60 // 7:00 PM
        val dinnerEnd = dinnerStart + schedule.dinnerBufferMinutes
        if (currentMinutes <= dinnerStart && dinnerEnd < sleepMinutes) {
            blockedIntervals.add(
                BlockedTimeInterval(
                    startMinutes = dinnerStart,
                    endMinutes = dinnerEnd,
                    type = PlanItemType.MEAL,
                    title = "Dinner & Rest Buffer",
                    subtitle = "Nutrition and decompression"
                )
            )
        }

        // Temporary Interruptions for today
        for (interruption in interruptions.filter { it.dateEpochDay == todayEpochDay }) {
            if (interruption.endMinutes > currentMinutes && interruption.startMinutes < sleepMinutes) {
                val clampedStart = max(currentMinutes, interruption.startMinutes)
                val clampedEnd = min(sleepMinutes, interruption.endMinutes)
                if (clampedEnd > clampedStart) {
                    blockedIntervals.add(
                        BlockedTimeInterval(
                            startMinutes = clampedStart,
                            endMinutes = clampedEnd,
                            type = PlanItemType.UNAVAILABLE,
                            title = interruption.title.ifBlank { "Personal Unavailable Time" },
                            subtitle = "Temporary schedule block"
                        )
                    )
                }
            }
        }

        // Sort blocked intervals chronologically and merge overlapping blocks
        blockedIntervals.sortBy { it.startMinutes }
        val mergedBlocked = mergeBlockedIntervals(blockedIntervals)

        // 3. Compute raw available study minutes & realistic productive capacity
        var rawAvailableMinutes = 0
        var windowScan = currentMinutes
        for (block in mergedBlocked) {
            if (block.startMinutes > windowScan) {
                rawAvailableMinutes += (block.startMinutes - windowScan)
            }
            windowScan = max(windowScan, block.endMinutes)
        }
        if (windowScan < sleepMinutes) {
            rawAvailableMinutes += (sleepMinutes - windowScan)
        }

        val realisticCapacityMinutes = (rawAvailableMinutes * energyLevel.focusMultiplier).roundToInt()

        // 4. Score and triage active tasks
        val activeTasks = tasks.filter { it.status != TaskStatus.COMPLETED && it.remainingMinutes > 0 }
        val scoredTasks = priorityScorer.prioritizeTasks(
            tasks = activeTasks,
            currentEnergy = energyLevel,
            currentEpochMs = currentEpochMs
        )

        val mustDoMinutes = scoredTasks.filter { it.bucket == TaskBucket.MUST_DO }.sumOf { it.task.remainingMinutes }
        val totalRequiredMinutes = scoredTasks.sumOf { it.task.remainingMinutes }

        val feasibilityStatus = when {
            mustDoMinutes > realisticCapacityMinutes -> FeasibilityStatus.OVERLOADED
            totalRequiredMinutes > realisticCapacityMinutes -> FeasibilityStatus.TIGHT
            totalRequiredMinutes <= (realisticCapacityMinutes * 0.75f) -> FeasibilityStatus.OPTIMAL
            else -> FeasibilityStatus.MANAGEABLE
        }

        // 5. Generate Focus-Sessions, Breaks, and Timeline
        val maxChunk = when (energyLevel) {
            EnergyLevel.EXHAUSTED -> 25
            EnergyLevel.LOW -> 30
            EnergyLevel.NORMAL -> 45
            EnergyLevel.HIGH -> 60
        }
        val breakDuration = when (energyLevel) {
            EnergyLevel.EXHAUSTED -> 20
            EnergyLevel.LOW -> 15
            EnergyLevel.NORMAL -> 15
            EnergyLevel.HIGH -> 10
        }

        val planItems = mutableListOf<PlanItem>()
        val deferredTasksList = mutableListOf<Task>()
        var clock = currentMinutes
        var orderIndex = 0

        // Remaining minutes tracker per task id
        val taskRemainingMap = scoredTasks.associate { it.task.id to it.task.remainingMinutes }.toMutableMap()
        val taskQueue = scoredTasks.toMutableList()

        while (clock < sleepMinutes) {
            // Check if clock is inside or matches a blocked interval
            val currentBlock = mergedBlocked.firstOrNull { it.startMinutes <= clock && clock < it.endMinutes }
            if (currentBlock != null) {
                val blockDuration = currentBlock.endMinutes - clock
                planItems.add(
                    PlanItem(
                        id = 0,
                        taskTitle = currentBlock.title,
                        subjectName = currentBlock.subtitle,
                        type = currentBlock.type,
                        status = PlanItemStatus.PLANNED,
                        startTimeMinutes = clock,
                        endTimeMinutes = currentBlock.endMinutes,
                        durationMinutes = blockDuration,
                        orderIndex = orderIndex++,
                        reason = "Preserved routine / interruption window",
                        isBreak = currentBlock.type == PlanItemType.MEAL || currentBlock.type == PlanItemType.REST
                    )
                )
                clock = currentBlock.endMinutes
                continue
            }

            // Find next available free window before the next blocked interval or sleep
            val nextBlock = mergedBlocked.firstOrNull { it.startMinutes > clock }
            val nextLimit = min(nextBlock?.startMinutes ?: sleepMinutes, sleepMinutes)
            val windowMinutes = nextLimit - clock

            if (windowMinutes < 10) {
                // Too small for a study chunk; absorb as rest / buffer
                if (windowMinutes > 0) {
                    planItems.add(
                        PlanItem(
                            id = 0,
                            taskTitle = "Buffer & Rest",
                            subjectName = "Transition time",
                            type = PlanItemType.REST,
                            status = PlanItemStatus.PLANNED,
                            startTimeMinutes = clock,
                            endTimeMinutes = nextLimit,
                            durationMinutes = windowMinutes,
                            orderIndex = orderIndex++,
                            reason = "Short transition buffer",
                            isBreak = true
                        )
                    )
                    clock = nextLimit
                }
                continue
            }

            // Pop next task from queue
            val currentScored = taskQueue.firstOrNull { (taskRemainingMap[it.task.id] ?: 0) > 0 }
            if (currentScored == null) {
                // No more study tasks needed; fill remaining time until bedtime with relaxation/wind down
                if (clock < sleepMinutes) {
                    planItems.add(
                        PlanItem(
                            id = 0,
                            taskTitle = "Relaxation & Free Time",
                            subjectName = "Goals cleared for today",
                            type = PlanItemType.WIND_DOWN,
                            status = PlanItemStatus.PLANNED,
                            startTimeMinutes = clock,
                            endTimeMinutes = sleepMinutes,
                            durationMinutes = sleepMinutes - clock,
                            orderIndex = orderIndex++,
                            reason = "All active academic goals are scheduled or complete",
                            isBreak = true
                        )
                    )
                    clock = sleepMinutes
                }
                break
            }

            val task = currentScored.task
            val remainingForTask = taskRemainingMap[task.id] ?: task.remainingMinutes

            // Determine chunk size
            val plannedChunk = min(remainingForTask, min(maxChunk, windowMinutes))
            val sessionEnd = clock + plannedChunk

            planItems.add(
                PlanItem(
                    id = 0,
                    taskId = task.id,
                    taskTitle = task.title,
                    subjectName = task.subjectName ?: task.type.displayName,
                    type = PlanItemType.TASK,
                    status = PlanItemStatus.PLANNED,
                    startTimeMinutes = clock,
                    endTimeMinutes = sessionEnd,
                    durationMinutes = plannedChunk,
                    orderIndex = orderIndex++,
                    reason = currentScored.reason,
                    isBreak = false
                )
            )

            val newRemaining = remainingForTask - plannedChunk
            taskRemainingMap[task.id] = newRemaining
            clock = sessionEnd

            // Check if we should insert a short break after this focus block
            val hasMoreWork = taskQueue.any { (taskRemainingMap[it.task.id] ?: 0) > 0 }
            val spaceForBreak = (nextLimit - clock) >= (breakDuration + 10)
            if (hasMoreWork && spaceForBreak && clock < sleepMinutes) {
                val breakEnd = min(clock + breakDuration, nextLimit)
                val actualBreakDuration = breakEnd - clock
                planItems.add(
                    PlanItem(
                        id = 0,
                        taskTitle = "Pacing Break",
                        subjectName = "Reset focus",
                        type = PlanItemType.BREAK,
                        status = PlanItemStatus.PLANNED,
                        startTimeMinutes = clock,
                        endTimeMinutes = breakEnd,
                        durationMinutes = actualBreakDuration,
                        orderIndex = orderIndex++,
                        reason = "Scheduled mental recovery between focus blocks",
                        isBreak = true
                    )
                )
                clock = breakEnd
            }
        }

        // Collect any tasks that could not be fitted into the evening schedule
        for (scored in scoredTasks) {
            val rem = taskRemainingMap[scored.task.id] ?: 0
            if (rem > 0) {
                deferredTasksList.add(scored.task)
            }
        }

        val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }
        val completedMinutes = completedTasks.sumOf { it.estimatedMinutes } + tasks.sumOf { it.completedMinutes }
        val totalPlannedMinutes = planItems.filter { it.type == PlanItemType.TASK }.sumOf { it.durationMinutes }

        return DailyPlan(
            dateEpochDay = todayEpochDay,
            generatedAtEpochMs = currentEpochMs,
            updatedAtEpochMs = currentEpochMs,
            availableMinutes = rawAvailableMinutes,
            realisticCapacityMinutes = realisticCapacityMinutes,
            requiredMinutes = totalRequiredMinutes,
            feasibilityStatus = feasibilityStatus,
            currentEnergy = energyLevel,
            items = planItems,
            deferredTasks = deferredTasksList,
            scheduleProgress = ScheduleProgress(
                completedMinutes = completedMinutes,
                totalPlannedMinutes = totalPlannedMinutes,
                completedTaskCount = completedTasks.size,
                totalTaskCount = tasks.size,
                paceStatus = SchedulePaceStatus.ON_TRACK
            )
        )
    }

    private fun mergeBlockedIntervals(intervals: List<BlockedTimeInterval>): List<BlockedTimeInterval> {
        if (intervals.isEmpty()) return emptyList()
        val sorted = intervals.sortedBy { it.startMinutes }
        val merged = mutableListOf<BlockedTimeInterval>()
        var current = sorted[0]

        for (i in 1 until sorted.size) {
            val next = sorted[i]
            if (next.startMinutes <= current.endMinutes) {
                current = current.copy(
                    endMinutes = max(current.endMinutes, next.endMinutes),
                    title = "${current.title} / ${next.title}"
                )
            } else {
                merged.add(current)
                current = next
            }
        }
        merged.add(current)
        return merged
    }
}
