package com.sarah.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sarah.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY reminderTimeEpochMs ASC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 AND dismissedAtEpochMs IS NULL AND reminderTimeEpochMs >= :nowMs ORDER BY reminderTimeEpochMs ASC")
    fun getActiveUpcomingReminders(nowMs: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE enabled = 1 AND dismissedAtEpochMs IS NULL AND reminderTimeEpochMs >= :nowMs ORDER BY reminderTimeEpochMs ASC")
    suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY reminderTimeEpochMs ASC")
    fun getRemindersForTask(taskId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE taskId = :taskId")
    suspend fun getRemindersForTaskSync(taskId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getReminderById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Long)

    @Query("DELETE FROM reminders WHERE taskId = :taskId")
    suspend fun deleteRemindersByTaskId(taskId: Long)

    @Query("UPDATE reminders SET dismissedAtEpochMs = :dismissedAt WHERE id = :id")
    suspend fun dismissReminder(id: Long, dismissedAt: Long)

    @Query("UPDATE reminders SET snoozedUntilEpochMs = :snoozedUntil, reminderTimeEpochMs = :newReminderTime WHERE id = :id")
    suspend fun snoozeReminder(id: Long, snoozedUntil: Long, newReminderTime: Long)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun setReminderEnabled(id: Long, enabled: Boolean)
}
