package com.sarah.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderModelTest {

    @Test
    fun `test reminder properties and defaults`() {
        val now = System.currentTimeMillis()
        val future = now + 60_000L
        val reminder = Reminder(
            id = 1,
            taskId = 10,
            taskTitle = "Java Practical",
            title = "Java Practical is due tomorrow",
            message = "Lab manual submission due at 9 AM",
            reminderTimeEpochMs = future,
            type = ReminderType.DEADLINE_REMINDER,
            enabled = true,
            createdAtEpochMs = now
        )

        assertEquals(1L, reminder.id)
        assertEquals(10L, reminder.taskId)
        assertEquals("Java Practical", reminder.taskTitle)
        assertEquals(ReminderType.DEADLINE_REMINDER, reminder.type)
        assertTrue("Future enabled reminder should be pending", reminder.isPending)
        assertFalse("Not snoozed by default", reminder.isSnoozed)
    }

    @Test
    fun `test past or disabled reminder is not pending`() {
        val past = System.currentTimeMillis() - 10_000L
        val pastReminder = Reminder(
            id = 2,
            title = "Old Reminder",
            message = "Past time",
            reminderTimeEpochMs = past,
            enabled = true
        )
        assertFalse("Past reminder is not pending", pastReminder.isPending)

        val disabledReminder = Reminder(
            id = 3,
            title = "Disabled",
            message = "Disabled",
            reminderTimeEpochMs = System.currentTimeMillis() + 100_000L,
            enabled = false
        )
        assertFalse("Disabled reminder is not pending", disabledReminder.isPending)

        val dismissedReminder = Reminder(
            id = 4,
            title = "Dismissed",
            message = "Dismissed",
            reminderTimeEpochMs = System.currentTimeMillis() + 100_000L,
            enabled = true,
            dismissedAtEpochMs = System.currentTimeMillis()
        )
        assertFalse("Dismissed reminder is not pending", dismissedReminder.isPending)
    }

    @Test
    fun `test snoozed reminder property`() {
        val now = System.currentTimeMillis()
        val futureSnooze = now + 600_000L // 10 minutes
        val snoozedReminder = Reminder(
            id = 5,
            title = "Snoozed Task",
            message = "Will ring later",
            reminderTimeEpochMs = futureSnooze,
            snoozedUntilEpochMs = futureSnooze
        )

        assertTrue("Should indicate snoozed state", snoozedReminder.isSnoozed)
    }
}
