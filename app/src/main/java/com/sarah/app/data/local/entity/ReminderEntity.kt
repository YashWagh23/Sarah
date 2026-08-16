package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["reminderTimeEpochMs"]),
        Index(value = ["enabled"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val title: String,
    val message: String,
    val reminderTimeEpochMs: Long,
    val type: String,
    val enabled: Boolean = true,
    val createdAtEpochMs: Long,
    val dismissedAtEpochMs: Long? = null,
    val snoozedUntilEpochMs: Long? = null
) {
    fun toDomain(): Reminder {
        return Reminder(
            id = id,
            taskId = taskId,
            taskTitle = taskTitle,
            title = title,
            message = message,
            reminderTimeEpochMs = reminderTimeEpochMs,
            type = runCatching { ReminderType.valueOf(type) }.getOrDefault(ReminderType.TASK_REMINDER),
            enabled = enabled,
            createdAtEpochMs = createdAtEpochMs,
            dismissedAtEpochMs = dismissedAtEpochMs,
            snoozedUntilEpochMs = snoozedUntilEpochMs
        )
    }

    companion object {
        fun fromDomain(reminder: Reminder): ReminderEntity {
            return ReminderEntity(
                id = reminder.id,
                taskId = reminder.taskId,
                taskTitle = reminder.taskTitle,
                title = reminder.title,
                message = reminder.message,
                reminderTimeEpochMs = reminder.reminderTimeEpochMs,
                type = reminder.type.name,
                enabled = reminder.enabled,
                createdAtEpochMs = reminder.createdAtEpochMs,
                dismissedAtEpochMs = reminder.dismissedAtEpochMs,
                snoozedUntilEpochMs = reminder.snoozedUntilEpochMs
            )
        }
    }
}
