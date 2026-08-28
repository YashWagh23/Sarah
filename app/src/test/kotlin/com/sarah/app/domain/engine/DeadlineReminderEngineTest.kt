package com.sarah.app.domain.engine

import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.TimeZone

class DeadlineReminderEngineTest {

    private lateinit var engine: DeadlineReminderEngine
    private val zoneId = TimeZone.UTC

    @Before
    fun setUp() {
        engine = DeadlineReminderEngine(zoneId)
    }

    @Test
    fun `test deadline more than 24 hours away generates 1-day and 2-hour reminders`() {
        val now = 1700000000000L
        val deadline = now + (48 * 60 * 60 * 1000L) // 48 hours away

        val task = Task(
            id = 1,
            title = "DBMS Assignment",
            subjectName = "Database Systems",
            type = TaskType.ASSIGNMENT,
            deadlineEpochMs = deadline
        )

        val reminders = engine.generateDeadlineReminders(task, nowEpochMs = now)

        assertEquals("Should create 2 reminders (1 day before + 2 hours before)", 2, reminders.size)
        assertEquals(ReminderType.DEADLINE_REMINDER, reminders[0].type)
        assertEquals(ReminderType.DEADLINE_REMINDER, reminders[1].type)

        // Reminder 1: 1 day before
        assertEquals(deadline - (24 * 60 * 60 * 1000L), reminders[0].reminderTimeEpochMs)
        assertTrue(reminders[0].title.contains("is due tomorrow"))
        assertTrue(reminders[0].message.contains("Database Systems"))

        // Reminder 2: 2 hours before
        assertEquals(deadline - (2 * 60 * 60 * 1000L), reminders[1].reminderTimeEpochMs)
        assertTrue(reminders[1].title.contains("is due in 2 hours"))
    }

    @Test
    fun `test deadline between 2 and 24 hours away generates only 2-hour reminder`() {
        val now = 1700000000000L
        val deadline = now + (5 * 60 * 60 * 1000L) // 5 hours away

        val task = Task(
            id = 2,
            title = "Java Practical Prep",
            type = TaskType.PRACTICAL,
            deadlineEpochMs = deadline
        )

        val reminders = engine.generateDeadlineReminders(task, nowEpochMs = now)

        assertEquals("Should create only 1 reminder (2 hours before)", 1, reminders.size)
        assertEquals(deadline - (2 * 60 * 60 * 1000L), reminders[0].reminderTimeEpochMs)
        assertTrue(reminders[0].title.contains("is due in 2 hours"))
    }

    @Test
    fun `test deadline less than 2 hours away generates urgent reminder`() {
        val now = 1700000000000L
        val deadline = now + (45 * 60 * 1000L) // 45 minutes away

        val task = Task(
            id = 3,
            title = "Maths Quiz Submission",
            type = TaskType.SUBMISSION,
            deadlineEpochMs = deadline
        )

        val reminders = engine.generateDeadlineReminders(task, nowEpochMs = now)

        assertEquals(1, reminders.size)
        assertTrue(reminders[0].title.contains("is due soon!"))
        assertEquals(now + 5 * 60 * 1000L, reminders[0].reminderTimeEpochMs)
    }

    @Test
    fun `test past deadline produces zero reminders`() {
        val now = 1700000000000L
        val deadline = now - (60 * 60 * 1000L) // 1 hour in the past

        val task = Task(
            id = 4,
            title = "Old Task",
            deadlineEpochMs = deadline
        )

        val reminders = engine.generateDeadlineReminders(task, nowEpochMs = now)
        assertTrue("Past deadline must yield zero reminders", reminders.isEmpty())
    }
}
