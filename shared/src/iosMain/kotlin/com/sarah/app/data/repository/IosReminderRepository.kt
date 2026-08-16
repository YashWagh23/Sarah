package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.util.currentTimeEpochMs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosReminderRepository(
    private val database: IosSarahDatabase
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return database.remindersFlow
    }

    override fun getActiveUpcomingReminders(): Flow<List<Reminder>> {
        return database.remindersFlow.map { list ->
            val now = currentTimeEpochMs()
            list.filter { it.enabled && it.dismissedAtEpochMs == null && it.reminderTimeEpochMs > now }
        }
    }

    override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> {
        return database.remindersFlow.map { list ->
            list.filter { it.taskId == taskId }
        }
    }

    override suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<Reminder> {
        return database.remindersFlow.value.filter {
            it.enabled && it.dismissedAtEpochMs == null && it.reminderTimeEpochMs > nowMs
        }
    }

    override suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder> {
        return database.remindersFlow.value.filter { it.taskId == taskId }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return database.remindersFlow.value.find { it.id == id }
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        val currentList = database.remindersFlow.value.toMutableList()
        val newId = if (reminder.id > 0) reminder.id else (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = reminder.copy(id = newId)
        val existingIndex = currentList.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = toInsert
        } else {
            currentList.add(toInsert)
        }
        database.remindersFlow.value = currentList
        database.saveReminders()
        return newId
    }

    override suspend fun updateReminder(reminder: Reminder) {
        val currentList = database.remindersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == reminder.id }
        if (index >= 0) {
            currentList[index] = reminder
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        deleteReminderById(reminder.id)
    }

    override suspend fun deleteReminderById(id: Long) {
        val currentList = database.remindersFlow.value.toMutableList()
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun deleteRemindersByTaskId(taskId: Long) {
        val currentList = database.remindersFlow.value.toMutableList()
        val removed = currentList.removeAll { it.taskId == taskId }
        if (removed) {
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun dismissReminder(id: Long) {
        val currentList = database.remindersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(dismissedAtEpochMs = currentTimeEpochMs())
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int) {
        val currentList = database.remindersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            val newTime = currentTimeEpochMs() + (snoozeDurationMinutes * 60_000L)
            currentList[index] = existing.copy(
                snoozedUntilEpochMs = newTime,
                reminderTimeEpochMs = newTime
            )
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long) {
        val currentList = database.remindersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(
                snoozedUntilEpochMs = newTimeEpochMs,
                reminderTimeEpochMs = newTimeEpochMs
            )
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }

    override suspend fun setReminderEnabled(id: Long, enabled: Boolean) {
        val currentList = database.remindersFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(enabled = enabled)
            database.remindersFlow.value = currentList
            database.saveReminders()
        }
    }
}
