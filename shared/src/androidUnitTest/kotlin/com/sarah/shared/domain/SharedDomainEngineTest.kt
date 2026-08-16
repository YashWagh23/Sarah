package com.sarah.shared.domain

import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.DateTimeParserHelper
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.NaturalLanguageTaskParser
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedDomainEngineTest {

    @Test
    fun testNaturalLanguageTaskParserBasicParsing() {
        val parser = NaturalLanguageTaskParser()
        val subjects = listOf(
            Subject(id = 1L, name = "Operating Systems", code = "CS301", targetAttendancePercentage = 85),
            Subject(id = 2L, name = "Computer Networks", code = "CS302", targetAttendancePercentage = 80)
        )

        val text = "Sir gave 3 Java programs for Operating Systems. Submit Monday 5pm. Urgent!"
        val draft = parser.parse(rawText = text, availableSubjects = subjects)

        assertNotNull(draft)
        assertEquals(1L, draft.subjectId)
        assertEquals("Operating Systems", draft.subjectName)
        assertEquals(TaskType.PRACTICAL, draft.type)
        assertEquals(TaskPriority.CRITICAL, draft.priority)
        assertTrue(draft.title.isNotBlank())
        assertTrue(draft.deadlineEpochMs > 0)
    }

    @Test
    fun testDateTimeParserHelperRelativeDays() {
        val baseDate = LocalDate(2026, 8, 17) // Monday
        val timeZone = TimeZone.UTC

        val tomorrowDeadline = DateTimeParserHelper.parseDeadline("Submit assignment tomorrow at 5pm", baseDate, timeZone)
        assertNotNull(tomorrowDeadline)

        val todayDeadline = DateTimeParserHelper.parseDeadline("Due today at 9:30 pm", baseDate, timeZone)
        assertNotNull(todayDeadline)

        val nextFridayDeadline = DateTimeParserHelper.parseDeadline("Submit report by friday 11:00 am", baseDate, timeZone)
        assertNotNull(nextFridayDeadline)
    }

    @Test
    fun testFeasibilityEngineEvaluation() {
        val engine = FeasibilityEngine()
        val schedule = CollegeSchedule(
            wakeTimeMinutes = 7 * 60,
            sleepTimeMinutes = 23 * 60, // 11:00 PM
            collegeStartTimeMinutes = 9 * 60,
            collegeEndTimeMinutes = 17 * 60, // 5:00 PM
            commuteMinutes = 30,
            dinnerBufferMinutes = 45
        )

        val tasks = listOf(
            Task(
                id = 1L,
                title = "OS Lab Assignment",
                subjectId = 1L,
                subjectName = "Operating Systems",
                type = TaskType.ASSIGNMENT,
                priority = TaskPriority.HIGH,
                difficulty = Difficulty.MEDIUM,
                energyRequirement = EnergyRequirement.MEDIUM,
                estimatedMinutes = 60,
                completedMinutes = 0,
                deadlineEpochMs = Clock.System.now().toEpochMilliseconds() + (12 * 3600 * 1000L),
                status = TaskStatus.PENDING
            )
        )

        // Evaluate at 18:00 (6:00 PM)
        val report = engine.evaluateToday(
            tasks = tasks,
            schedule = schedule,
            energyLevel = EnergyLevel.HIGH,
            currentMinutesInput = 18 * 60
        )

        assertNotNull(report)
        assertTrue(report.realisticProductiveMinutes > 0)
        assertEquals(60, report.mustDoMinutes)
        assertEquals(FeasibilityStatus.OPTIMAL, report.status)
        assertEquals(1, report.mustDoTasks.size)
    }

    @Test
    fun testDeadlineReminderEngineGeneration() {
        val reminderEngine = DeadlineReminderEngine()
        val now = 1750000000000L
        val deadline = now + (30 * 3600 * 1000L) // 30 hours away

        val task = Task(
            id = 10L,
            title = "DBMS Schema Design",
            subjectName = "Database Systems",
            type = TaskType.ASSIGNMENT,
            priority = TaskPriority.HIGH,
            deadlineEpochMs = deadline,
            status = TaskStatus.PENDING
        )

        val reminders = reminderEngine.generateDeadlineReminders(task = task, nowEpochMs = now)
        assertEquals(2, reminders.size) // 1 day before + 2 hours before
        assertTrue(reminders.any { it.title.contains("due tomorrow") })
        assertTrue(reminders.any { it.title.contains("2 hours") })
    }

    @Test
    fun testNextActionEngineComputation() {
        val nextActionEngine = NextActionEngine()
        val schedule = CollegeSchedule(
            wakeTimeMinutes = 7 * 60,
            sleepTimeMinutes = 23 * 60 // 11:00 PM
        )

        val task = Task(
            id = 5L,
            title = "Networks Socket Programming",
            subjectName = "Computer Networks",
            type = TaskType.PRACTICAL,
            priority = TaskPriority.HIGH,
            estimatedMinutes = 45,
            completedMinutes = 0,
            deadlineEpochMs = Clock.System.now().toEpochMilliseconds() + (24 * 3600 * 1000L),
            status = TaskStatus.PENDING
        )

        val emptyPlan = DailyPlan(dateEpochDay = 20000L)

        val nextAction = nextActionEngine.computeNextAction(
            plan = emptyPlan,
            tasks = listOf(task),
            schedule = schedule,
            currentMinutesInput = 19 * 60 // 7:00 PM
        )

        assertEquals(NextActionType.START_TASK, nextAction.actionType)
        assertEquals(5L, nextAction.taskId)
        assertTrue(nextAction.title.contains("Networks Socket Programming"))
    }

    @Test
    fun testAdaptivePlannerPlanGeneration() {
        val planner = AdaptivePlanner()
        val schedule = CollegeSchedule(
            wakeTimeMinutes = 7 * 60,
            sleepTimeMinutes = 23 * 60,
            collegeStartTimeMinutes = 9 * 60,
            collegeEndTimeMinutes = 17 * 60,
            commuteMinutes = 30,
            dinnerBufferMinutes = 45
        )

        val tasks = listOf(
            Task(
                id = 1L,
                title = "Study Process Synchronization",
                subjectName = "Operating Systems",
                type = TaskType.EXAM_PREP,
                priority = TaskPriority.HIGH,
                estimatedMinutes = 45,
                completedMinutes = 0,
                deadlineEpochMs = Clock.System.now().toEpochMilliseconds() + (24 * 3600 * 1000L),
                status = TaskStatus.PENDING
            )
        )

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentMinutesInput = 18 * 60 // 6:00 PM
        )

        assertNotNull(plan)
        assertTrue(plan.items.isNotEmpty())
        assertTrue(plan.items.any { it.taskTitle.contains("Study Process Synchronization") })
    }
}
