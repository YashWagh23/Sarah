package com.sarah.app.domain.engine

import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DeadlineReminderEngine(
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) {

    fun generateDeadlineReminders(
        task: Task,
        nowEpochMs: Long = Clock.System.now().toEpochMilliseconds()
    ): List<Reminder> {
        val deadline = task.deadlineEpochMs
        val timeUntilDeadline = deadline - nowEpochMs

        if (timeUntilDeadline <= 0) {
            return emptyList()
        }

        val reminders = mutableListOf<Reminder>()
        val oneDayMs = 24 * 60 * 60 * 1000L
        val twoHoursMs = 2 * 60 * 60 * 1000L

        val localDateTime = Instant.fromEpochMilliseconds(deadline).toLocalDateTime(timeZone)
        val hour24 = localDateTime.hour
        val minute = localDateTime.minute
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        val amPm = if (hour24 < 12) "AM" else "PM"
        val minStr = if (minute < 10) "0$minute" else "$minute"
        val formattedDeadlineTime = "$hour12:$minStr $amPm"

        val subjectSuffix = if (!task.subjectName.isNullOrBlank()) " (${task.subjectName})" else ""

        // 1. One day before reminder (if deadline > 24 hours away)
        if (timeUntilDeadline > oneDayMs) {
            val oneDayBefore = deadline - oneDayMs
            if (oneDayBefore > nowEpochMs) {
                reminders.add(
                    Reminder(
                        taskId = task.id,
                        taskTitle = task.title,
                        title = "${task.title} is due tomorrow",
                        message = "Your ${task.type.displayName.lowercase()} is due tomorrow at $formattedDeadlineTime$subjectSuffix.",
                        reminderTimeEpochMs = oneDayBefore,
                        type = ReminderType.DEADLINE_REMINDER
                    )
                )
            }
        }

        // 2. Two hours before reminder (if deadline > 2 hours away)
        if (timeUntilDeadline > twoHoursMs) {
            val twoHoursBefore = deadline - twoHoursMs
            if (twoHoursBefore > nowEpochMs) {
                reminders.add(
                    Reminder(
                        taskId = task.id,
                        taskTitle = task.title,
                        title = "${task.title} is due in 2 hours",
                        message = "Final stretch! Submission deadline is at $formattedDeadlineTime$subjectSuffix.",
                        reminderTimeEpochMs = twoHoursBefore,
                        type = ReminderType.DEADLINE_REMINDER
                    )
                )
            }
        } else if (timeUntilDeadline > 15 * 60 * 1000L) {
            // If less than 2 hours away, create an urgent reminder
            val reminderTime = nowEpochMs + (5 * 60 * 1000L)
            if (reminderTime < deadline) {
                reminders.add(
                    Reminder(
                        taskId = task.id,
                        taskTitle = task.title,
                        title = "${task.title} is due soon!",
                        message = "Due today at $formattedDeadlineTime$subjectSuffix.",
                        reminderTimeEpochMs = reminderTime,
                        type = ReminderType.DEADLINE_REMINDER
                    )
                )
            }
        }

        return reminders
    }
}
