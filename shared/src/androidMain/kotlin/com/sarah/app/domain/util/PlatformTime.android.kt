package com.sarah.app.domain.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

actual fun currentTimeEpochMs(): Long = System.currentTimeMillis()

actual fun formatDueDate(deadlineEpochMs: Long?): String? {
    if (deadlineEpochMs == null) return null
    val zone = ZoneId.systemDefault()
    val dueDate = Instant.ofEpochMilli(deadlineEpochMs).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    return when {
        dueDate == today -> "Today"
        dueDate == today.plusDays(1) -> "Tomorrow"
        dueDate.isBefore(today) -> "Overdue"
        else -> dueDate.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

actual fun isDueDateOverdue(deadlineEpochMs: Long?): Boolean {
    if (deadlineEpochMs == null) return false
    val zone = ZoneId.systemDefault()
    val dueDate = Instant.ofEpochMilli(deadlineEpochMs).atZone(zone).toLocalDate()
    return dueDate.isBefore(LocalDate.now(zone))
}

actual fun formatReminderTime(reminderTimeEpochMs: Long): String {
    val zone = ZoneId.systemDefault()
    val remDate = Instant.ofEpochMilli(reminderTimeEpochMs).atZone(zone).toLocalDate()
    val today = LocalDate.now(zone)
    val timeStr = Instant.ofEpochMilli(reminderTimeEpochMs).atZone(zone).format(DateTimeFormatter.ofPattern("h:mm a"))
    return when {
        remDate == today -> "Today, $timeStr"
        remDate == today.plusDays(1) -> "Tomorrow, $timeStr"
        else -> "${remDate.format(DateTimeFormatter.ofPattern("MMM d"))}, $timeStr"
    }
}

actual fun formatDateTime(epochMs: Long): String {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(epochMs).atZone(zone).format(DateTimeFormatter.ofPattern("EEE, MMM d · h:mm a"))
}

actual fun formatEpochDay(epochDay: Long): String {
    val date = LocalDate.ofEpochDay(epochDay)
    return date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
}

actual fun getStartOfTodayEpochMs(): Long {
    val zone = ZoneId.systemDefault()
    return LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
}
