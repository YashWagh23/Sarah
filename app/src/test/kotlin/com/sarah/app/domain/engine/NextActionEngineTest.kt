package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.PlanItem
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.SchedulePaceStatus
import com.sarah.app.domain.model.ScheduleProgress
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class NextActionEngineTest {

    private lateinit var engine: NextActionEngine
    private lateinit var planner: AdaptivePlanner
    private val date = LocalDate.of(2026, 8, 15)
    private val zone = ZoneId.of("UTC")
    private val defaultSchedule = CollegeSchedule(
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

    @Test
    fun `test task should start when top focus session begins`() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "DBMS Assignment",
                deadlineEpochMs = epochMs(1, 9, 0),
                estimatedMinutes = 45,
                priority = TaskPriority.HIGH,
                type = TaskType.ASSIGNMENT
            )
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(19, 45), // 7:45 PM
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = LocalTime.of(19, 45),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.START_TASK, nextAction.actionType)
        assertEquals(1L, nextAction.taskId)
        assertEquals("Start: DBMS Assignment", nextAction.title)
        assertEquals(45, nextAction.durationMinutes)
    }

    @Test
    fun `test task should continue when partially completed`() {
        val tasks = listOf(
            Task(
                id = 2,
                title = "Java Practical Code",
                deadlineEpochMs = epochMs(1, 9, 0),
                estimatedMinutes = 90,
                completedMinutes = 40, // 50m remaining
                priority = TaskPriority.CRITICAL,
                type = TaskType.PRACTICAL
            )
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(19, 45),
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = LocalTime.of(19, 45),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.CONTINUE_TASK, nextAction.actionType)
        assertEquals(2L, nextAction.taskId)
        assertEquals("Continue: Java Practical Code", nextAction.title)
        assertEquals("CRITICAL", nextAction.urgencyBadge)
    }

    @Test
    fun `test break should be recommended during break slot`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45),
            Task(id = 2, title = "Task B", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(18, 0),
            currentDate = date,
            zoneId = zone
        )

        // Find break slot in plan (e.g. 6:45 PM - 7:00 PM)
        val breakItem = plan.items.first { it.type == PlanItemType.BREAK }
        val breakTime = LocalTime.of(breakItem.startTimeMinutes / 60, breakItem.startTimeMinutes % 60)

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = breakTime,
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.TAKE_BREAK, nextAction.actionType)
        assertEquals("BREAK", nextAction.urgencyBadge)
    }

    @Test
    fun `test meal should be recommended during dinner buffer`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(19, 0), // 7:00 PM (Dinner start)
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = LocalTime.of(19, 15), // 7:15 PM inside dinner
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.MEAL, nextAction.actionType)
        assertEquals("MEAL", nextAction.urgencyBadge)
    }

    @Test
    fun `test stop for tonight when bedtime is reached`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(23, 40), // 11:40 PM (Past 11:30 PM bedtime)
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = LocalTime.of(23, 40),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.STOP_FOR_TONIGHT, nextAction.actionType)
        assertEquals("Time for Sleep & Rest", nextAction.title)
        assertEquals("REST", nextAction.urgencyBadge)
    }

    @Test
    fun `test overloaded and behind schedule triggers delay recovery`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45)
        )

        val basePlan = planner.generatePlan(
            tasks = tasks,
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(20, 0),
            currentDate = date,
            zoneId = zone
        )

        val delayedPlan = basePlan.copy(
            scheduleProgress = basePlan.scheduleProgress.copy(
                paceStatus = SchedulePaceStatus.SIGNIFICANTLY_BEHIND,
                minutesBehindSchedule = 40
            )
        )

        val nextAction = engine.computeNextAction(
            plan = delayedPlan,
            tasks = tasks,
            schedule = defaultSchedule,
            currentTime = LocalTime.of(20, 40),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.RECOVER_FROM_DELAY, nextAction.actionType)
        assertEquals("Readjust Schedule Pace", nextAction.title)
        assertEquals("ADAPT", nextAction.urgencyBadge)
    }

    @Test
    fun `test exhausted energy behavior near bedtime stops work to prevent burnout`() {
        val nonCriticalTask = Task(
            id = 5,
            title = "Optional Chapter Reading",
            deadlineEpochMs = epochMs(3, 18, 0),
            estimatedMinutes = 40,
            priority = TaskPriority.LOW,
            type = TaskType.READING
        )

        val plan = planner.generatePlan(
            tasks = listOf(nonCriticalTask),
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.EXHAUSTED,
            currentTime = LocalTime.of(23, 10), // 11:10 PM (20m to 11:30 PM bedtime)
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = listOf(nonCriticalTask),
            schedule = defaultSchedule,
            currentTime = LocalTime.of(23, 10),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.STOP_FOR_TONIGHT, nextAction.actionType)
        assertEquals("Call It a Night", nextAction.title)
        assertEquals("RECHARGE", nextAction.urgencyBadge)
    }

    @Test
    fun `test no remaining tasks recommends stop for tonight`() {
        val completedTask = Task(
            id = 1,
            title = "Finished Practical",
            deadlineEpochMs = epochMs(1, 9, 0),
            estimatedMinutes = 45,
            status = TaskStatus.COMPLETED
        )

        val plan = planner.generatePlan(
            tasks = listOf(completedTask),
            schedule = defaultSchedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(20, 0),
            currentDate = date,
            zoneId = zone
        )

        val nextAction = engine.computeNextAction(
            plan = plan,
            tasks = listOf(completedTask),
            schedule = defaultSchedule,
            currentTime = LocalTime.of(20, 0),
            currentDate = date,
            zoneId = zone
        )

        assertEquals(NextActionType.STOP_FOR_TONIGHT, nextAction.actionType)
        assertEquals("All Done for Tonight!", nextAction.title)
        assertEquals("COMPLETE", nextAction.urgencyBadge)
    }

    @Test
    fun `test deterministic next action derivation`() {
        val tasks = listOf(
            Task(id = 1, title = "Task A", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 30)
        )

        val plan = planner.generatePlan(tasks, defaultSchedule, EnergyLevel.NORMAL, emptyList(), LocalTime.of(19, 45), date, zone)

        val action1 = engine.computeNextAction(plan, tasks, defaultSchedule, LocalTime.of(19, 45), date, zone)
        val action2 = engine.computeNextAction(plan, tasks, defaultSchedule, LocalTime.of(19, 45), date, zone)

        assertEquals(action1.actionType, action2.actionType)
        assertEquals(action1.taskId, action2.taskId)
        assertEquals(action1.title, action2.title)
        assertEquals(action1.durationMinutes, action2.durationMinutes)
        assertEquals(action1.reason, action2.reason)
    }
}
