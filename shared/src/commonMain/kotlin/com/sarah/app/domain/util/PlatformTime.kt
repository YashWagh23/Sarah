package com.sarah.app.domain.util

expect fun currentTimeEpochMs(): Long

expect fun formatDueDate(deadlineEpochMs: Long?): String?

expect fun isDueDateOverdue(deadlineEpochMs: Long?): Boolean

expect fun formatReminderTime(reminderTimeEpochMs: Long): String

expect fun formatDateTime(epochMs: Long): String

expect fun formatEpochDay(epochDay: Long): String

expect fun getStartOfTodayEpochMs(): Long
