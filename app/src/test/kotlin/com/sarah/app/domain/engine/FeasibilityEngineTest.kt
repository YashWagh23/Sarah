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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class FeasibilityEngineTest {

    private lateinit var engine: FeasibilityEngine
    private lateinit var schedule: CollegeSchedule
    private val fixedZone = ZoneId.of("UTC")
    private val todayDate = LocalDate.of(2026, 8, 15)

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
        val deadlineTomorrow = todayDate.plusDays(1).atTime(10, 0).atZone(fixedZone).toInstant().toEpochMilli()
        val deadlineNextWeek = todayDate.plusDays(7).atTime(10, 0).atZone(fixedZone).toInstant().toEpochMilli()

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
            currentTime = LocalTime.of(18, 30),
            currentDate = todayDate,
            zoneId = fixedZone
        )

        assertEquals(2, report.mustDoTasks.size)
        assertEquals("Java Practical", report.mustDoTasks[0].title)
        assertEquals("DBMS Assignment", report.mustDoTasks[1].title)
        assertEquals(1, report.canDeferTasks.size)
        assertEquals("Optional Reading", report.canDeferTasks[0].title)
    }

    @Test
    fun testEvaluateTodayFlagsOverloadedWorkload() {
        val deadlineTomorrow = todayDate.plusDays(1).atTime(10, 0).atZone(fixedZone).toInstant().toEpochMilli()

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
            currentTime = LocalTime.of(22, 30),
            currentDate = todayDate,
            zoneId = fixedZone
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
            currentTime = LocalTime.of(18, 0),
            currentDate = todayDate,
            zoneId = fixedZone
        )

        val exhaustedReport = engine.evaluateToday(
            tasks = emptyList(),
            schedule = schedule,
            energyLevel = EnergyLevel.EXHAUSTED,
            currentTime = LocalTime.of(18, 0),
            currentDate = todayDate,
            zoneId = fixedZone
        )

        assertTrue(exhaustedReport.realisticProductiveMinutes < normalReport.realisticProductiveMinutes)
    }
}
