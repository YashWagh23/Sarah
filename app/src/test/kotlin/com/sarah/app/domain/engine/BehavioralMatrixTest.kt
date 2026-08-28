package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.PlanItemType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.model.TemporaryInterruption
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant

class BehavioralMatrixTest {

    private lateinit var scorer: TaskPriorityScorer
    private lateinit var planner: AdaptivePlanner
    private val date = LocalDate(2026, 8, 15)
    private val zone = TimeZone.UTC
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
        scorer = TaskPriorityScorer()
        planner = AdaptivePlanner(scorer)
    }

    private fun epochMs(dayOffset: Long, hour: Int, minute: Int): Long {
        return date.plus(dayOffset, DateTimeUnit.DAY).atTime(LocalTime(hour, minute)).toInstant(zone).toEpochMilliseconds()
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

    private fun printScenario(
        title: String,
        tasks: List<Task>,
        energy: EnergyLevel,
        currentTime: LocalTime,
        interruptions: List<TemporaryInterruption> = emptyList()
    ) {
        println("================================================================================")
        println("SCENARIO: $title")
        println("================================================================================")
        println("Energy: $energy | Current Time: ${currentTime} | Bedtime: ${formatTime(schedule.sleepTimeMinutes)}")
        println("\nINPUT TASKS:")
        val curInstant = date.atTime(currentTime).toInstant(zone).toEpochMilliseconds()
        for (t in tasks) {
            val dueHours = (t.deadlineEpochMs - curInstant) / (1000.0 * 3600.0)
            println("  - [ID ${t.id}] ${t.title} | Est: ${t.estimatedMinutes}m (Done: ${t.completedMinutes}m, Rem: ${t.remainingMinutes}m) | Priority: ${t.priority} | Type: ${t.type} | EnergyReq: ${t.energyRequirement} | Due in: ${String.format("%.1f", dueHours)}h")
        }
        if (interruptions.isNotEmpty()) {
            println("\nINTERRUPTIONS:")
            for (i in interruptions) {
                println("  - ${i.title}: ${formatTime(i.startMinutes)} - ${formatTime(i.endMinutes)} (${i.endMinutes - i.startMinutes}m)")
            }
        }

        val plan = planner.generatePlan(
            tasks = tasks,
            schedule = schedule,
            energyLevel = energy,
            interruptions = interruptions,
            currentMinutesInput = currentTime.hour * 60 + currentTime.minute,
            currentDateInput = date,
            timeZone = zone
        )

        println("\nMETRICS:")
        println("  - Raw Available Time: ${plan.availableMinutes} min (${plan.availableMinutes / 60}h ${plan.availableMinutes % 60}m)")
        println("  - Realistic Capacity: ${plan.realisticCapacityMinutes} min (${plan.realisticCapacityMinutes / 60}h ${plan.realisticCapacityMinutes % 60}m)")
        println("  - Work Required: ${plan.requiredMinutes} min (${plan.requiredMinutes / 60}h ${plan.requiredMinutes % 60}m)")
        println("  - Feasibility Status: ${plan.feasibilityStatus}")

        println("\nSCHEDULED TIME BLOCKS:")
        for (item in plan.items) {
            val typeTag = if (item.isBreak) "[REST/BREAK]" else "[FOCUS/TASK]"
            println("  ${formatTime(item.startTimeMinutes).padEnd(9)} - ${formatTime(item.endTimeMinutes).padEnd(9)} | ${item.durationMinutes.toString().padStart(3)}m | $typeTag ${item.taskTitle} (${item.subjectName}) | Reason: ${item.reason}")
        }

        println("\nDEFERRED TASKS:")
        if (plan.deferredTasks.isEmpty()) {
            println("  None (all tasks scheduled)")
        } else {
            for (d in plan.deferredTasks) {
                println("  - [ID ${d.id}] ${d.title} (Remaining: ${d.remainingMinutes}m)")
            }
        }
        println("================================================================================\n")
    }

    @Test
    fun runAllScenarios() {
        // Scenario 1: Normal evening - 3 tasks with different deadlines/priorities
        printScenario(
            title = "1. Normal Evening (3 tasks: different deadlines/priorities)",
            tasks = listOf(
                Task(id = 1, title = "DBMS Lab Report", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45, priority = TaskPriority.HIGH, type = TaskType.PRACTICAL, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 2, title = "Data Structures Problem Set", deadlineEpochMs = epochMs(2, 17, 0), estimatedMinutes = 60, priority = TaskPriority.MEDIUM, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.HIGH),
                Task(id = 3, title = "Technical Communication Reading", deadlineEpochMs = epochMs(4, 18, 0), estimatedMinutes = 30, priority = TaskPriority.LOW, type = TaskType.READING, energyRequirement = EnergyRequirement.LOW)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(18, 0)
        )

        // Scenario 2: Exhausted student - urgent hard task + easy short task
        printScenario(
            title = "2. Exhausted Student (Urgent Hard Task + Easy Short Task)",
            tasks = listOf(
                Task(id = 1, title = "Algorithms Dynamic Programming Assignment", deadlineEpochMs = epochMs(1, 10, 0), estimatedMinutes = 60, priority = TaskPriority.HIGH, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.HIGH),
                Task(id = 2, title = "Software Eng Glossary Review", deadlineEpochMs = epochMs(1, 14, 0), estimatedMinutes = 25, priority = TaskPriority.MEDIUM, type = TaskType.REVISION, energyRequirement = EnergyRequirement.LOW)
            ),
            energy = EnergyLevel.EXHAUSTED,
            currentTime = LocalTime(19, 45)
        )

        // Scenario 3: Overloaded evening - 4-5 hours of work with ~2 hours available
        printScenario(
            title = "3. Overloaded Evening (4.5h work, ~2h available time)",
            tasks = listOf(
                Task(id = 1, title = "OS Kernel Lab Submission", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 90, priority = TaskPriority.CRITICAL, type = TaskType.SUBMISSION, energyRequirement = EnergyRequirement.HIGH),
                Task(id = 2, title = "Computer Networks Quiz Prep", deadlineEpochMs = epochMs(1, 11, 0), estimatedMinutes = 60, priority = TaskPriority.HIGH, type = TaskType.EXAM_PREP, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 3, title = "Database Normalization Exercises", deadlineEpochMs = epochMs(2, 16, 0), estimatedMinutes = 60, priority = TaskPriority.MEDIUM, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 4, title = "Web Dev Project Milestone", deadlineEpochMs = epochMs(3, 18, 0), estimatedMinutes = 60, priority = TaskPriority.LOW, type = TaskType.PROJECT, energyRequirement = EnergyRequirement.MEDIUM)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(21, 0) // 9:00 PM to 11:30 PM = 2.5h (150 min raw)
        )

        // Scenario 4: New urgent assignment at 8 PM - verify reordering
        printScenario(
            title = "4. New Urgent Assignment at 8 PM (Dynamic Reordering)",
            tasks = listOf(
                Task(id = 1, title = "OOP Architecture Diagram", deadlineEpochMs = epochMs(2, 17, 0), estimatedMinutes = 45, priority = TaskPriority.MEDIUM, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 2, title = "Surprise Quiz Revision: Discrete Math", deadlineEpochMs = epochMs(1, 8, 30), estimatedMinutes = 40, priority = TaskPriority.CRITICAL, type = TaskType.EXAM_PREP, energyRequirement = EnergyRequirement.HIGH)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(20, 0)
        )

        // Scenario 5: Partial completion - 90 min task, 35 min completed
        printScenario(
            title = "5. Partial Completion (90 min task, 35 min already completed)",
            tasks = listOf(
                Task(id = 1, title = "Microprocessor Lab Manual", deadlineEpochMs = epochMs(1, 10, 0), estimatedMinutes = 90, completedMinutes = 35, priority = TaskPriority.HIGH, type = TaskType.PRACTICAL, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 2, title = "Physics Problem Sheet", deadlineEpochMs = epochMs(2, 12, 0), estimatedMinutes = 45, completedMinutes = 0, priority = TaskPriority.MEDIUM, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.MEDIUM)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(19, 45)
        )

        // Scenario 6: Temporary interruption - unavailable from 8:00 PM to 10:00 PM
        printScenario(
            title = "6. Temporary Interruption (Doctor Appointment 8:00 PM - 10:00 PM)",
            tasks = listOf(
                Task(id = 1, title = "Automata Homework", deadlineEpochMs = epochMs(1, 10, 0), estimatedMinutes = 45, priority = TaskPriority.HIGH, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.MEDIUM),
                Task(id = 2, title = "Linear Algebra Review", deadlineEpochMs = epochMs(2, 14, 0), estimatedMinutes = 30, priority = TaskPriority.MEDIUM, type = TaskType.REVISION, energyRequirement = EnergyRequirement.LOW)
            ),
            interruptions = listOf(
                TemporaryInterruption(id = 10, title = "Family Dinner / Doctor", startMinutes = 20 * 60, endMinutes = 22 * 60, dateEpochDay = date.toEpochDays().toLong())
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(19, 0)
        )

        // Scenario 7: Tomorrow deadline vs next-week deadline
        printScenario(
            title = "7. Tomorrow Deadline vs Next-Week Deadline",
            tasks = listOf(
                Task(id = 1, title = "Cloud Computing Term Paper", deadlineEpochMs = epochMs(7, 18, 0), estimatedMinutes = 60, priority = TaskPriority.CRITICAL, type = TaskType.PROJECT, energyRequirement = EnergyRequirement.HIGH),
                Task(id = 2, title = "Tomorrow Morning Stats Problem Set", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 45, priority = TaskPriority.HIGH, type = TaskType.ASSIGNMENT, energyRequirement = EnergyRequirement.MEDIUM)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(19, 0)
        )

        // Scenario 8: Sleep protection - verify nothing crosses bedtime
        printScenario(
            title = "8. Sleep Boundary Protection (Massive late workload)",
            tasks = listOf(
                Task(id = 1, title = "Full Stack Capstone Coding", deadlineEpochMs = epochMs(1, 9, 0), estimatedMinutes = 180, priority = TaskPriority.CRITICAL, type = TaskType.PROJECT, energyRequirement = EnergyRequirement.HIGH)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(22, 0) // Bedtime 11:30 PM (90 mins total window)
        )

        // Scenario 9: Quick task vs long task (quick-win boost)
        printScenario(
            title = "9. Quick Task vs Long Task (Quick-win evaluation)",
            tasks = listOf(
                Task(id = 1, title = "Submit Lab Observation Slip", deadlineEpochMs = epochMs(2, 17, 0), estimatedMinutes = 15, priority = TaskPriority.MEDIUM, type = TaskType.OTHER, energyRequirement = EnergyRequirement.LOW),
                Task(id = 2, title = "Deep Machine Learning Chapter", deadlineEpochMs = epochMs(2, 17, 0), estimatedMinutes = 90, priority = TaskPriority.MEDIUM, type = TaskType.READING, energyRequirement = EnergyRequirement.HIGH)
            ),
            energy = EnergyLevel.NORMAL,
            currentTime = LocalTime(19, 45)
        )
    }
}
