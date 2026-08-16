package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.ReminderDao
import com.sarah.app.data.local.entity.ReminderEntity
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepositoryImpl(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveUpcomingReminders(): Flow<List<Reminder>> {
        val now = System.currentTimeMillis()
        return reminderDao.getActiveUpcomingReminders(now).map { list -> list.map { it.toDomain() } }
    }

    override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> {
        return reminderDao.getRemindersForTask(taskId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<Reminder> {
        return reminderDao.getUpcomingPendingRemindersSync(nowMs).map { it.toDomain() }
    }

    override suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder> {
        return reminderDao.getRemindersForTaskSync(taskId).map { it.toDomain() }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(ReminderEntity.fromDomain(reminder))
    }

    override suspend fun deleteReminderById(id: Long) {
        reminderDao.deleteReminderById(id)
    }

    override suspend fun deleteRemindersByTaskId(taskId: Long) {
        reminderDao.deleteRemindersByTaskId(taskId)
    }

    override suspend fun dismissReminder(id: Long) {
        reminderDao.dismissReminder(id, System.currentTimeMillis())
    }

    override suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int) {
        val snoozedUntil = System.currentTimeMillis() + (snoozeDurationMinutes * 60 * 1000L)
        reminderDao.snoozeReminder(id, snoozedUntil, snoozedUntil)
    }

    override suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long) {
        reminderDao.snoozeReminder(id, newTimeEpochMs, newTimeEpochMs)
    }

    override suspend fun setReminderEnabled(id: Long, enabled: Boolean) {
        reminderDao.setReminderEnabled(id, enabled)
    }
}
