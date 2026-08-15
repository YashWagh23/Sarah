package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AgendaPlannerTest {

    private lateinit var planner: AgendaPlanner
    private lateinit var schedule: CollegeSchedule

    @Before
    fun setup() {
        planner = AgendaPlanner()
        schedule = CollegeSchedule(
            sleepTimeMinutes = 23 * 60 + 30,
            dinnerBufferMinutes = 45
        )
    }

    @Test
    fun testGenerateAgendaInsertsDinnerBufferAndBreaks() {
        val tasks = listOf(
            Task(
                id = 1,
                title = "Java Practical",
                type = TaskType.PRACTICAL,
                deadlineEpochMs = System.currentTimeMillis() + 86400000,
                estimatedMinutes = 50,
                priority = TaskPriority.HIGH
            ),
            Task(
                id = 2,
                title = "DBMS Assignment",
                type = TaskType.ASSIGNMENT,
                deadlineEpochMs = System.currentTimeMillis() + 86400000,
                estimatedMinutes = 45,
                priority = TaskPriority.HIGH
            )
        )

        val currentMinutes = 18 * 60 + 30
        val agenda = planner.generateAgenda(
            tasks = tasks,
            currentMinutes = currentMinutes,
            sleepMinutes = schedule.sleepTimeMinutes,
            energyLevel = EnergyLevel.NORMAL,
            schedule = schedule
        )

        assertFalse(agenda.isEmpty())
        assertTrue(agenda.first().title.contains("Dinner"))
        assertTrue(agenda.any { it.title.contains("Java Practical") })
        assertTrue(agenda.any { it.isBreak })
    }
}
