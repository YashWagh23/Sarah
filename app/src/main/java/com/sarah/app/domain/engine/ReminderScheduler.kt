package com.sarah.app.domain.engine

import com.sarah.app.domain.model.Reminder

interface ReminderScheduler {
    fun scheduleReminder(reminder: Reminder)
    fun cancelReminder(reminderId: Long)
    fun cancelTaskReminders(taskId: Long)
    fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long)
    fun rescheduleAllActiveReminders()
}
