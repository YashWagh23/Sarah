package com.sarah.app.domain.repository

import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.util.currentTimeEpochMs
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun getAllReminders(): Flow<List<Reminder>>
    fun getActiveUpcomingReminders(): Flow<List<Reminder>>
    fun getRemindersForTask(taskId: Long): Flow<List<Reminder>>
    suspend fun getUpcomingPendingRemindersSync(nowMs: Long = currentTimeEpochMs()): List<Reminder>
    suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder>
    suspend fun getReminderById(id: Long): Reminder?
    suspend fun insertReminder(reminder: Reminder): Long
    suspend fun updateReminder(reminder: Reminder)
    suspend fun deleteReminder(reminder: Reminder)
    suspend fun deleteReminderById(id: Long)
    suspend fun deleteRemindersByTaskId(taskId: Long)
    suspend fun dismissReminder(id: Long)
    suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int)
    suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long)
    suspend fun setReminderEnabled(id: Long, enabled: Boolean)
}
