package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.SchedulePaceStatus
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class NextActionMatrixTest {

    private lateinit var engine: NextActionEngine
    private lateinit var planner: AdaptivePlanner
    private val date = LocalDate.of(2026, 8, 15)
    private val zone = ZoneId.of("UTC")
    private val schedule = CollegeSchedule(
        wakeTimeMinutes = 7 * 60,
        sleepTimeMinutes = 23 * 60 + 30, // 11:30 PM (1410 min)
        collegeStartTimeMinutes = 9 * 60,
        collegeEndTimeMinutes = 16 * 60 + 30,
        commuteMinutes = 45,
        dinnerBufferMinutes = 45
    )

    @Before
    fun setUp() {
        engine = NextActionEngine()
        planner = AdaptivePlanner(TaskPriorityScorer())
    }

    private fun epochMs(dayOffset: Long, hour: Int, minute: Int): Long {
        return date.plusDays(dayOffset).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
    }

    private fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        val amPm = if (h >= 12) "PM" else "AM"
        val displayH = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format("%d:%02d %s", displayH, m, amPm)
    }

    @Test
    fun runMatrix() {
        // 1. Normal evening with an upcoming task
        runScenario(
            title = "1. Normal Evening with an Upcoming Task",
            currentTime = LocalTime.of(19, 45),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "DBMS Assignment", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45, priority = TaskPriority.HIGH, type = TaskType.ASSIGNMENT)
            )
        )

        // 2. Partially completed task
        runScenario(
            title = "2. Partially Completed Task (90m est, 40m done)",
            currentTime = LocalTime.of(19, 45),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 2, title = "Java Practical Code", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 90, completedMinutes = 40, priority = TaskPriority.CRITICAL, type = TaskType.PRACTICAL)
            )
        )

        // 3. Currently inside a break
        runScenario(
            title = "3. Currently Inside a Restorative Break",
            currentTime = LocalTime.of(18, 45), // Inside 6:45 PM - 7:00 PM break
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45),
                Task(id = 2, title = "Task B", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
            ),
            planGenerationTime = LocalTime.of(18, 0)
        )

        // 4. Currently inside dinner/meal
        runScenario(
            title = "4. Currently Inside Dinner / Meal Buffer",
            currentTime = LocalTime.of(19, 15), // Inside 7:00 PM - 7:45 PM dinner
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
            ),
            planGenerationTime = LocalTime.of(19, 0)
        )

        // 5. Student is >= 30 minutes behind schedule
        runScenario(
            title = "5. Student is >= 30 Minutes Behind Schedule",
            currentTime = LocalTime.of(20, 30),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
            ),
            customPaceStatus = SchedulePaceStatus.SIGNIFICANTLY_BEHIND,
            customMinutesBehind = 35
        )

        // 6. Student is exhausted near bedtime
        runScenario(
            title = "6. Student is Exhausted Near Bedtime (11:10 PM)",
            currentTime = LocalTime.of(23, 10),
            energy = EnergyLevel.EXHAUSTED,
            tasks = listOf(
                Task(id = 5, title = "Optional Chapter Reading", deadlineEpochMs = epochMs(3, 18, 0), estimatedMinutes = 40, priority = TaskPriority.LOW, type = TaskType.READING)
            )
        )

        // 7. All planned work completed
        runScenario(
            title = "7. All Planned Work Completed",
            currentTime = LocalTime.of(20, 0),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Finished Lab Practical", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45, status = TaskStatus.COMPLETED)
            )
        )

        // 8. Current time at bedtime
        runScenario(
            title = "8. Current Time At Bedtime (11:30 PM)",
            currentTime = LocalTime.of(23, 30),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Unfinished Project", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 60)
            )
        )

        // 9. Current time after bedtime
        runScenario(
            title = "9. Current Time After Bedtime (11:45 PM)",
            currentTime = LocalTime.of(23, 45),
            energy = EnergyLevel.NORMAL,
            tasks = listOf(
                Task(id = 1, title = "Unfinished Project", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 60)
            )
        )

        // 10. No tasks at all
        runScenario(
            title = "10. No Tasks At All",
            currentTime = LocalTime.of(19, 0),
            energy = EnergyLevel.NORMAL,
            tasks = emptyList()
        )
    }

    private fun runScenario(
        title: String,
        currentTime: LocalTime,
        energy: EnergyLevel,
        tasks: List<Task>,
        planGenerationTime: LocalTime = currentTime,
        customPaceStatus: SchedulePaceStatus? = null,
        customMinutesBehind: Int? = null
    ) {
        val currentMinutes = currentTime.hour * 60 + currentTime.minute
        val planGenMinutes = planGenerationTime.hour * 60 + planGenerationTime.minute
        var plan = planner.generatePlan(
            tasks = tasks,
            schedule = schedule,
            energyLevel = energy,
            currentMinutesInput = planGenMinutes,
            currentDateInput = kotlinx.datetime.LocalDate(2026, 8, 15),
            timeZone = kotlinx.datetime.TimeZone.UTC
        )

        if (customPaceStatus != null || customMinutesBehind != null) {
            plan = plan.copy(
                scheduleProgress = plan.scheduleProgress.copy(
                    paceStatus = customPaceStatus ?: plan.scheduleProgress.paceStatus,
                    minutesBehindSchedule = customMinutesBehind ?: plan.scheduleProgress.minutesBehindSchedule
                )
            )
        }

        val activeItem = plan.items.firstOrNull { it.startTimeMinutes <= currentMinutes && currentMinutes < it.endTimeMinutes }
        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = schedule,
            currentMinutesInput = currentMinutes,
            timeZone = kotlinx.datetime.TimeZone.UTC
        )

        println("================================================================================")
        println("SCENARIO: $title")
        println("================================================================================")
        println("  - Current Time: $currentTime (${formatTime(currentMinutes)})")
        println("  - Energy: $energy")
        println("  - Active Plan Item: ${activeItem?.let { "${it.taskTitle} (${it.type}, ${formatTime(it.startTimeMinutes)} - ${formatTime(it.endTimeMinutes)})" } ?: "None / Between Slots"}")
        println("  - Returned Action Type: ${nextAction.actionType}")
        println("  - Title: ${nextAction.title}")
        println("  - Duration: ${nextAction.durationMinutes} min")
        println("  - Reason: ${nextAction.reason}")
        println("  - Urgency Badge: ${nextAction.urgencyBadge}")
        println("================================================================================\n")
    }
}
