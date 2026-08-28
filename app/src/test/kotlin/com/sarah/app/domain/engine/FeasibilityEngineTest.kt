package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class FeasibilityEngineTest {

    private lateinit var engine: FeasibilityEngine
    private lateinit var schedule: CollegeSchedule
    private val fixedZone = TimeZone.UTC
    private val todayDate = LocalDate(2026, 8, 15)

    @Before
    fun setup() {
        engine = FeasibilityEngine(AgendaPlanner())
        schedule = CollegeSchedule(
            wakeTimeMinutes = 7 * 60,
            sleepTimeMinutes = 23 * 60 + 30,
            collegeStartTimeMinutes = 9 * 60,
            collegeEndTimeMinutes = 16 * 60 + 30,
            commuteMinutes = 45,
            dinnerBufferMinutes = 45,
            breakDurationMinutes = 15,
            preferredSessionLengthMinutes = 45
        )
    }

    @Test
    fun testEvaluateTodayCorrectlyTriagesUrgentTasks() {
        val deadlineTomorrow = todayDate.plus(1, DateTimeUnit.DAY).atTime(LocalTime(10, 0)).toInstant(fixedZone).toEpochMilliseconds()
        val deadlineNextWeek = todayDate.plus(7, DateTimeUnit.DAY).atTime(LocalTime(10, 0)).toInstant(fixedZone).toEpochMilliseconds()

        val urgentPractical = Task(
            id = 1,
            title = "Java Practical",
            type = TaskType.PRACTICAL,
            deadlineEpochMs = deadlineTomorrow,
            estimatedMinutes = 50,
            priority = TaskPriority.CRITICAL
        )

        val urgentAssignment = Task(
            id = 2,
            title = "DBMS Assignment",
            type = TaskType.ASSIGNMENT,
            deadlineEpochMs = deadlineTomorrow,
            estimatedMinutes = 45,
            priority = TaskPriority.HIGH
        )

        val futureTask = Task(
            id = 3,
            title = "Optional Reading",
            type = TaskType.READING,
            deadlineEpochMs = deadlineNextWeek,
            estimatedMinutes = 60,
            priority = TaskPriority.LOW
        )

        val report = engine.evaluateToday(
            tasks = listOf(urgentPractical, urgentAssignment, futureTask),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentMinutesInput = 18 * 60 + 30,
            currentDateInput = todayDate,
            timeZone = fixedZone
        )

        assertEquals(2, report.mustDoTasks.size)
        assertEquals("Java Practical", report.mustDoTasks[0].title)
        assertEquals("DBMS Assignment", report.mustDoTasks[1].title)
        assertEquals(1, report.canDeferTasks.size)
        assertEquals("Optional Reading", report.canDeferTasks[0].title)
    }

    @Test
    fun testEvaluateTodayFlagsOverloadedWorkload() {
        val deadlineTomorrow = todayDate.plus(1, DateTimeUnit.DAY).atTime(LocalTime(10, 0)).toInstant(fixedZone).toEpochMilliseconds()

        val heavyTask = Task(
            id = 1,
            title = "Heavy Compiler Project",
            type = TaskType.ASSIGNMENT,
            deadlineEpochMs = deadlineTomorrow,
            estimatedMinutes = 120,
            priority = TaskPriority.CRITICAL
        )

        val report = engine.evaluateToday(
            tasks = listOf(heavyTask),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentMinutesInput = 22 * 60 + 30,
            currentDateInput = todayDate,
            timeZone = fixedZone
        )

        assertEquals(FeasibilityStatus.OVERLOADED, report.status)
        assertTrue(report.guidanceMessage.contains("Tonight is overloaded"))
    }

    @Test
    fun testExhaustedEnergyLevelReducesCapacity() {
        val normalReport = engine.evaluateToday(
            tasks = emptyList(),
            schedule = schedule,
            energyLevel = EnergyLevel.HIGH,
            currentMinutesInput = 18 * 60,
            currentDateInput = todayDate,
            timeZone = fixedZone
        )

        val exhaustedReport = engine.evaluateToday(
            tasks = emptyList(),
            schedule = schedule,
            energyLevel = EnergyLevel.EXHAUSTED,
            currentMinutesInput = 18 * 60,
            currentDateInput = todayDate,
            timeZone = fixedZone
        )

        assertTrue(exhaustedReport.realisticProductiveMinutes < normalReport.realisticProductiveMinutes)
    }
}
