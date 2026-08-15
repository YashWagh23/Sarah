package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskBucket
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.model.TemporaryInterruption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class AdaptiveDailyPlannerTest {

    private lateinit var scorer: TaskPriorityScorer
    private lateinit var planner: AdaptivePlanner
    private val fixedDate = LocalDate.of(2026, 8, 15)
    private val fixedZone = ZoneId.of("UTC")
    private val defaultSchedule = CollegeSchedule(
        wakeTimeMinutes = 7 * 60, // 7:00 AM
        sleepTimeMinutes = 23 * 60 + 30, // 11:30 PM (1410 mins)
        collegeStartTimeMinutes = 9 * 60, // 9:00 AM
        collegeEndTimeMinutes = 16 * 60 + 30, // 4:30 PM (990 mins)
        commuteMinutes = 45,
        dinnerBufferMinutes = 45
    )

    @Before
    fun setUp() {
        scorer = TaskPriorityScorer()
        planner = AdaptivePlanner(scorer)
    }

    private fun epochMsFor(dayOffset: Long, hour: Int, minute: Int): Long {
        return fixedDate.plusDays(dayOffset).atTime(hour, minute).atZone(fixedZone).toInstant().toEpochMilli()
    }

    @Test
    fun `test enough time schedules all tasks with breaks and wind down`() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "DBMS Quiz Prep",
                deadlineEpochMs = epochMsFor(1, 10, 0), // tomorrow morning
                estimatedMinutes = 45,
                priority = TaskPriority.HIGH,
                type = TaskType.ASSIGNMENT
            ),
            Task(
                id = 2,
                title = "OS Notes Reading",
                deadlineEpochMs = epochMsFor(2, 17, 0), // in 2 days
                estimatedMinutes = 30,
                priority = TaskPriority.MEDIUM,
                type = TaskType.READING
            )
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(19, 0), // 7:00 PM
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        // Verify dinner buffer is scheduled first from 7:00 PM to 7:45 PM
        val mealItem = plan.items.firstOrNull { it.type == PlanItemType.MEAL }
        assertTrue("Meal buffer should be scheduled", mealItem != null)
        assertEquals(19 * 60, mealItem!!.startTimeMinutes)
        assertEquals(19 * 60 + 45, mealItem.endTimeMinutes)

        // Verify task items exist
        val taskItems = plan.items.filter { it.type == PlanItemType.TASK }
        assertEquals(2, taskItems.size)
        assertEquals("DBMS Quiz Prep", taskItems[0].taskTitle)
        assertEquals("OS Notes Reading", taskItems[1].taskTitle)

        // Verify break was inserted between tasks
        val breakItems = plan.items.filter { it.type == PlanItemType.BREAK }
        assertTrue("Restorative break should be inserted", breakItems.isNotEmpty())

        // Verify wind down before bedtime
        val windDown = plan.items.firstOrNull { it.type == PlanItemType.WIND_DOWN }
        assertTrue("Wind down slot should precede sleep", windDown != null)
        assertEquals(defaultSchedule.sleepTimeMinutes, windDown!!.endTimeMinutes)

        // Zero tasks deferred
        assertTrue("No tasks should be deferred when time is sufficient", plan.deferredTasks.isEmpty())
        assertEquals(FeasibilityStatus.OPTIMAL, plan.feasibilityStatus)
    }

    @Test
    fun `test overloaded schedule identifies shortage and defers low priority work`() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Java Lab Practical",
                deadlineEpochMs = epochMsFor(1, 9, 0), // Tomorrow 9 AM
                estimatedMinutes = 90,
                priority = TaskPriority.CRITICAL,
                type = TaskType.PRACTICAL
            ),
            Task(
                id = 2,
                title = "Compiler Design Assignment",
                deadlineEpochMs = epochMsFor(1, 12, 0), // Tomorrow noon
                estimatedMinutes = 75,
                priority = TaskPriority.HIGH,
                type = TaskType.ASSIGNMENT
            ),
            Task(
                id = 3,
                title = "Elective Project Architecture",
                deadlineEpochMs = epochMsFor(3, 18, 0), // In 3 days
                estimatedMinutes = 120,
                priority = TaskPriority.MEDIUM,
                type = TaskType.PROJECT
            )
        )

        // Clock starts late at 10:00 PM (22:00 = 1320 mins), sleep is at 11:30 PM (1410 mins) -> only 90 mins raw
        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(22, 0),
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        assertEquals(FeasibilityStatus.OVERLOADED, plan.feasibilityStatus)
        // Verify deferrals occurred
        assertFalse("Overloaded day must defer unschedulable tasks", plan.deferredTasks.isEmpty())
        assertTrue("Elective Project should be deferred", plan.deferredTasks.any { it.id == 3L })

        // Check sleep boundary is strictly protected
        val lastItem = plan.items.last()
        assertTrue("Plan items must not exceed sleep boundary", lastItem.endTimeMinutes <= defaultSchedule.sleepTimeMinutes)
    }

    @Test
    fun `test exhausted energy shrinks session size and expands breaks`() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Maths Revision",
                deadlineEpochMs = epochMsFor(1, 10, 0),
                estimatedMinutes = 50,
                priority = TaskPriority.HIGH,
                type = TaskType.REVISION,
                energyRequirement = EnergyRequirement.LOW
            )
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.EXHAUSTED,
            currentTime = LocalTime.of(20, 0), // 8:00 PM
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        val taskItems = plan.items.filter { it.type == PlanItemType.TASK }
        // 50 mins task with 25 mins max chunk for EXHAUSTED should be split into 2 chunks of 25m
        assertEquals(2, taskItems.size)
        assertEquals(25, taskItems[0].durationMinutes)
        assertEquals(25, taskItems[1].durationMinutes)

        // Restorative break for EXHAUSTED is 20 minutes
        val breakItem = plan.items.firstOrNull { it.type == PlanItemType.BREAK }
        assertTrue("Break should exist between chunks", breakItem != null)
        assertEquals(20, breakItem!!.durationMinutes)
    }

    @Test
    fun `test urgent task prioritized over lower priority task`() {
        val lowPriorityTask = Task(
            id = 10,
            title = "Optional Background Article",
            deadlineEpochMs = epochMsFor(5, 18, 0),
            estimatedMinutes = 30,
            priority = TaskPriority.LOW,
            type = TaskType.READING
        )
        val urgentTask = Task(
            id = 20,
            title = "Urgent Lab Submission",
            deadlineEpochMs = epochMsFor(1, 8, 30),
            estimatedMinutes = 40,
            priority = TaskPriority.CRITICAL,
            type = TaskType.SUBMISSION
        )

        val scoredLow = scorer.scoreTask(lowPriorityTask, EnergyLevel.NORMAL, fixedDate, LocalTime.of(18, 0), fixedZone)
        val scoredUrgent = scorer.scoreTask(urgentTask, EnergyLevel.NORMAL, fixedDate, LocalTime.of(18, 0), fixedZone)

        assertTrue("Urgent task score (${scoredUrgent.score}) must exceed low priority score (${scoredLow.score})", scoredUrgent.score > scoredLow.score)
        assertEquals(TaskBucket.MUST_DO, scoredUrgent.bucket)
        assertEquals(TaskBucket.CAN_DEFER, scoredLow.bucket)

        val plan = planner.generatePlan(
            tasks = listOf(lowPriorityTask, urgentTask),
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(18, 0),
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        val firstTaskItem = plan.items.first { it.type == PlanItemType.TASK }
        assertEquals("Urgent Lab Submission", firstTaskItem.taskTitle)
    }

    @Test
    fun `test temporary interruption blocks planner from scheduling study`() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Java Practical",
                deadlineEpochMs = epochMsFor(1, 9, 0),
                estimatedMinutes = 60,
                priority = TaskPriority.HIGH,
                type = TaskType.PRACTICAL
            )
        )

        // Interruption: Out from 8:00 PM to 9:30 PM (20:00 = 1200, 21:30 = 1290)
        val interruption = TemporaryInterruption(
            id = 101,
            title = "Doctor Appointment",
            startMinutes = 20 * 60,
            endMinutes = 21 * 60 + 30,
            dateEpochDay = fixedDate.toEpochDay()
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            interruptions = listOf(interruption),
            currentTime = LocalTime.of(20, 0), // 8:00 PM (1200 mins)
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        val unavailItem = plan.items.firstOrNull { it.type == PlanItemType.UNAVAILABLE }
        assertTrue("Interruption should be emitted as UNAVAILABLE item", unavailItem != null)
        assertEquals("Doctor Appointment", unavailItem!!.taskTitle)
        assertEquals(20 * 60, unavailItem.startTimeMinutes)
        assertEquals(21 * 60 + 30, unavailItem.endTimeMinutes)

        // Verify task is scheduled after the doctor appointment
        val taskItem = plan.items.first { it.type == PlanItemType.TASK }
        assertTrue("Task should start at or after interruption end time", taskItem.startTimeMinutes >= 21 * 60 + 30)
    }

    @Test
    fun `test sleep boundary is never violated`() {
        val longTask = Task(
            id = 1,
            title = "Massive Project Codebase",
            deadlineEpochMs = epochMsFor(1, 9, 0),
            estimatedMinutes = 300,
            priority = TaskPriority.CRITICAL,
            type = TaskType.PROJECT
        )

        val plan = planner.generatePlan(
            tasks = listOf(longTask),
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(21, 0), // 9:00 PM (1260 mins). Sleep is at 11:30 PM (1410 mins) -> exactly 150 mins
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        for (item in plan.items) {
            assertTrue("Item ${item.taskTitle} endTime ${item.endTimeMinutes} must not exceed sleep ${defaultSchedule.sleepTimeMinutes}", item.endTimeMinutes <= defaultSchedule.sleepTimeMinutes)
        }
        assertTrue("Incomplete remainder of massive task must be deferred", plan.deferredTasks.isNotEmpty())
    }

    @Test
    fun `test partial task completion respects remaining minutes`() {
        val partiallyCompletedTask = Task(
            id = 1,
            title = "Long Lab Report",
            deadlineEpochMs = epochMsFor(1, 10, 0),
            estimatedMinutes = 90,
            completedMinutes = 55, // 35 mins remaining!
            priority = TaskPriority.HIGH,
            type = TaskType.PRACTICAL
        )

        assertEquals(35, partiallyCompletedTask.remainingMinutes)

        val plan = planner.generatePlan(
            tasks = listOf(partiallyCompletedTask),
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(20, 0),
            currentDate = fixedDate,
            zoneId = fixedZone
        )

        val taskItems = plan.items.filter { it.type == PlanItemType.TASK }
        assertEquals(1, taskItems.size)
        assertEquals(35, taskItems[0].durationMinutes)
    }

    @Test
    fun `test deterministic plan generation produces identical output`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMsFor(1, 10, 0), estimatedMinutes = 45, priority = TaskPriority.HIGH),
            Task(id = 2, title = "Task B", deadlineEpochMs = epochMsFor(2, 12, 0), estimatedMinutes = 30, priority = TaskPriority.MEDIUM)
        )

        val plan1 = planner.generatePlan(tasks, defaultSchedule, EnergyLevel.NORMAL, emptyList(), LocalTime.of(19, 0), fixedDate, fixedZone)
        val plan2 = planner.generatePlan(tasks, defaultSchedule, EnergyLevel.NORMAL, emptyList(), LocalTime.of(19, 0), fixedDate, fixedZone)

        assertEquals(plan1.items.size, plan2.items.size)
        assertEquals(plan1.availableMinutes, plan2.availableMinutes)
        assertEquals(plan1.realisticCapacityMinutes, plan2.realisticCapacityMinutes)
        assertEquals(plan1.feasibilityStatus, plan2.feasibilityStatus)

        for (i in plan1.items.indices) {
            assertEquals(plan1.items[i].taskTitle, plan2.items[i].taskTitle)
            assertEquals(plan1.items[i].startTimeMinutes, plan2.items[i].startTimeMinutes)
            assertEquals(plan1.items[i].endTimeMinutes, plan2.items[i].endTimeMinutes)
            assertEquals(plan1.items[i].type, plan2.items[i].type)
        }
    }
}
