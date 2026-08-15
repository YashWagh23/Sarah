package com.sarah.app

import com.sarah.app.domain.engine.AgendaPlanner
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class Phase1VerificationTest {

    private lateinit var feasibilityEngine: FeasibilityEngine
    private lateinit var agendaPlanner: AgendaPlanner
    private val zone = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 8, 15)

    @Before
    fun setup() {
        agendaPlanner = AgendaPlanner()
        feasibilityEngine = FeasibilityEngine(agendaPlanner)
    }

    // 1. ONBOARDING & PROFILE VERIFICATION
    @Test
    fun verifyOnboardingAndProfileSetup() {
        val profile = UserProfile(
            id = 1,
            name = "Alex Wagh",
            collegeName = "College of Engineering",
            department = "Computer Science",
            semesterYear = "3rd Year",
            isOnboardingCompleted = true,
            defaultEnergyLevel = EnergyLevel.NORMAL
        )

        assertEquals("Alex Wagh", profile.name)
        assertEquals("Computer Science", profile.department)
        assertTrue(profile.isOnboardingCompleted)
        assertEquals(EnergyLevel.NORMAL, profile.defaultEnergyLevel)
    }

    // 2. SCHEDULE CONSTRAINTS & TIME ARITHMETIC VERIFICATION
    @Test
    fun verifyScheduleConstraintsCalculation() {
        val schedule = CollegeSchedule(
            wakeTimeMinutes = 7 * 60, // 07:00 AM (420)
            sleepTimeMinutes = 23 * 60 + 30, // 11:30 PM (1410)
            collegeStartTimeMinutes = 9 * 60, // 09:00 AM (540)
            collegeEndTimeMinutes = 16 * 60 + 30, // 04:30 PM (990)
            commuteMinutes = 45,
            dinnerBufferMinutes = 45,
            breakDurationMinutes = 15,
            preferredSessionLengthMinutes = 45
        )

        // At 18:30 (6:30 PM), minutes until sleep = 1410 - 1110 = 300 mins (5h)
        val report = feasibilityEngine.evaluateToday(
            tasks = emptyList(),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(18, 30),
            currentDate = today,
            zoneId = zone
        )

        assertEquals(300, report.minutesUntilSleep)
        // Raw available = 300 - 45 (dinner) = 255 mins
        assertEquals(255, report.rawAvailableMinutes)
        // Productive capacity = 255 * 0.85 (NORMAL energy) = 217 mins (~3h 37m)
        assertEquals(217, report.realisticProductiveMinutes)
        assertEquals(FeasibilityStatus.OPTIMAL, report.status)
    }

    // 3. SUBJECTS MANAGEMENT VERIFICATION
    @Test
    fun verifySubjectsDataAndAttendance() {
        val subjects = listOf(
            Subject(id = 1, name = "Java & OOP", code = "CS301", professorName = "Prof. Sharma", weeklyHours = 4, targetAttendancePercentage = 75, currentAttendancePercentage = 88),
            Subject(id = 2, name = "DBMS", code = "CS302", professorName = "Dr. Rao", weeklyHours = 4, targetAttendancePercentage = 75, currentAttendancePercentage = 78),
            Subject(id = 3, name = "Operating Systems", code = "CS303", professorName = "Prof. Gupta", weeklyHours = 3, targetAttendancePercentage = 75, currentAttendancePercentage = 92)
        )

        assertEquals(3, subjects.size)
        val avgAttendance = subjects.map { it.currentAttendancePercentage }.average().toInt()
        assertEquals(86, avgAttendance)
        assertTrue(subjects.all { it.currentAttendancePercentage >= it.targetAttendancePercentage })
    }

    // 4. TASKS TRIAGE & FEASIBILITY REPORT VERIFICATION
    @Test
    fun verifyTaskTriageAndRiskAssessment() {
        val tomorrowEpochMs = today.plusDays(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val nextWeekEpochMs = today.plusDays(7).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()

        val schedule = CollegeSchedule(sleepTimeMinutes = 23 * 60 + 30, dinnerBufferMinutes = 45)

        val task1 = Task(id = 1, title = "Java Practical", type = TaskType.PRACTICAL, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 50, priority = TaskPriority.CRITICAL)
        val task2 = Task(id = 2, title = "DBMS Assignment", type = TaskType.ASSIGNMENT, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 45, priority = TaskPriority.HIGH)
        val task3 = Task(id = 3, title = "OS Revision", type = TaskType.REVISION, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 30, priority = TaskPriority.MEDIUM)
        val task4 = Task(id = 4, title = "Optional Reading", type = TaskType.READING, deadlineEpochMs = nextWeekEpochMs, estimatedMinutes = 60, priority = TaskPriority.LOW)

        val report = feasibilityEngine.evaluateToday(
            tasks = listOf(task1, task2, task3, task4),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(18, 30),
            currentDate = today,
            zoneId = zone
        )

        // Must Do = Java Practical + DBMS Assignment (due tomorrow, high urgency)
        assertEquals(2, report.mustDoTasks.size)
        assertEquals("Java Practical", report.mustDoTasks[0].title)
        assertEquals("DBMS Assignment", report.mustDoTasks[1].title)

        // Should Do = OS Revision
        assertEquals(1, report.shouldDoTasks.size)
        assertEquals("OS Revision", report.shouldDoTasks[0].title)

        // Can Defer = Optional Reading (due next week)
        assertEquals(1, report.canDeferTasks.size)
        assertEquals("Optional Reading", report.canDeferTasks[0].title)

        // Required time = 50 + 45 + 30 = 125 mins
        assertEquals(125, report.totalRequiredMinutes)
        assertEquals(FeasibilityStatus.OPTIMAL, report.status)
    }

    // 5. ENERGY LEVEL DYNAMIC ADAPTATION VERIFICATION
    @Test
    fun verifyEnergyLevelDynamicAdaptation() {
        val tomorrowEpochMs = today.plusDays(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val schedule = CollegeSchedule(sleepTimeMinutes = 23 * 60 + 30, dinnerBufferMinutes = 45)

        val heavyTask = Task(id = 1, title = "Demanding Project", type = TaskType.ASSIGNMENT, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 90, priority = TaskPriority.HIGH, energyRequirement = EnergyRequirement.HIGH)

        val highEnergyReport = feasibilityEngine.evaluateToday(
            tasks = listOf(heavyTask),
            schedule = schedule,
            energyLevel = EnergyLevel.HIGH,
            currentTime = LocalTime.of(20, 0), // 8:00 PM (210 mins until sleep, 210 * 1.0 = 210 productive mins)
            currentDate = today,
            zoneId = zone
        )
        assertEquals(FeasibilityStatus.OPTIMAL, highEnergyReport.status)
        assertEquals(210, highEnergyReport.realisticProductiveMinutes)

        val exhaustedReport = feasibilityEngine.evaluateToday(
            tasks = listOf(heavyTask),
            schedule = schedule,
            energyLevel = EnergyLevel.EXHAUSTED,
            currentTime = LocalTime.of(20, 0), // 210 mins * 0.45 = 95 productive mins
            currentDate = today,
            zoneId = zone
        )
        // With 90 min task and only 95 productive mins, workload becomes TIGHT or OVERLOADED
        assertEquals(95, exhaustedReport.realisticProductiveMinutes)
    }

    // 6. TASK COMPLETION TOGGLE RECALCULATION VERIFICATION
    @Test
    fun verifyTaskCompletionRecalculation() {
        val tomorrowEpochMs = today.plusDays(1).atTime(10, 0).atZone(zone).toInstant().toEpochMilli()
        val schedule = CollegeSchedule(sleepTimeMinutes = 23 * 60 + 30, dinnerBufferMinutes = 45)

        val task1 = Task(id = 1, title = "Java Practical", type = TaskType.PRACTICAL, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 50, priority = TaskPriority.CRITICAL, status = TaskStatus.PENDING)
        val task2 = Task(id = 2, title = "DBMS Assignment", type = TaskType.ASSIGNMENT, deadlineEpochMs = tomorrowEpochMs, estimatedMinutes = 45, priority = TaskPriority.HIGH, status = TaskStatus.PENDING)

        val beforeReport = feasibilityEngine.evaluateToday(
            tasks = listOf(task1, task2),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(18, 30),
            currentDate = today,
            zoneId = zone
        )
        assertEquals(95, beforeReport.totalRequiredMinutes)

        // Student completes Java Practical
        val completedTask1 = task1.copy(status = TaskStatus.COMPLETED, completedAtEpochMs = System.currentTimeMillis())
        val afterReport = feasibilityEngine.evaluateToday(
            tasks = listOf(completedTask1, task2),
            schedule = schedule,
            energyLevel = EnergyLevel.NORMAL,
            currentTime = LocalTime.of(19, 30),
            currentDate = today,
            zoneId = zone
        )
        assertEquals(45, afterReport.totalRequiredMinutes)
        assertEquals(1, afterReport.mustDoTasks.size)
        assertEquals("DBMS Assignment", afterReport.mustDoTasks[0].title)
    }

    // 7. AGENDA TIMELINE GENERATION VERIFICATION
    @Test
    fun verifyAgendaTimelineGeneration() {
        val schedule = CollegeSchedule(sleepTimeMinutes = 23 * 60 + 30, dinnerBufferMinutes = 45)
        val tasks = listOf(
            Task(id = 1, title = "Java Practical", estimatedMinutes = 50, type = TaskType.PRACTICAL, deadlineEpochMs = 0),
            Task(id = 2, title = "DBMS Assignment", estimatedMinutes = 45, type = TaskType.ASSIGNMENT, deadlineEpochMs = 0)
        )

        val agenda = agendaPlanner.generateAgenda(
            tasks = tasks,
            currentMinutes = 18 * 60 + 30, // 6:30 PM
            sleepMinutes = schedule.sleepTimeMinutes,
            energyLevel = EnergyLevel.NORMAL,
            schedule = schedule
        )

        assertNotNull(agenda)
        assertTrue(agenda.isNotEmpty())

        // Check dinner buffer slot
        val dinnerSlot = agenda.find { it.title.contains("Dinner") }
        assertNotNull(dinnerSlot)
        assertEquals("6:30 PM", dinnerSlot!!.startTimeFormatted)
        assertEquals("7:15 PM", dinnerSlot.endTimeFormatted)

        // Check task slot
        val javaSlot = agenda.find { it.title.contains("Java Practical") }
        assertNotNull(javaSlot)

        // Check break slot
        val breakSlot = agenda.find { it.isBreak && it.title.contains("Break") }
        assertNotNull(breakSlot)
    }
}
