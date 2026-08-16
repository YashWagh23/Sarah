package com.sarah.app.domain.model

import com.sarah.app.domain.util.currentTimeEpochMs

enum class ReminderType(val displayName: String) {
    TASK_REMINDER("Task Reminder"),
    DEADLINE_REMINDER("Deadline Reminder"),
    CUSTOM_REMINDER("Quick Reminder")
}

data class Reminder(
    val id: Long = 0,
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val title: String,
    val message: String,
    val reminderTimeEpochMs: Long,
    val type: ReminderType = ReminderType.TASK_REMINDER,
    val enabled: Boolean = true,
    val createdAtEpochMs: Long = currentTimeEpochMs(),
    val dismissedAtEpochMs: Long? = null,
    val snoozedUntilEpochMs: Long? = null
) {
    val isPending: Boolean
        get() = enabled && dismissedAtEpochMs == null && reminderTimeEpochMs > currentTimeEpochMs()

    val isSnoozed: Boolean
        get() = snoozedUntilEpochMs != null && snoozedUntilEpochMs > currentTimeEpochMs()
}
