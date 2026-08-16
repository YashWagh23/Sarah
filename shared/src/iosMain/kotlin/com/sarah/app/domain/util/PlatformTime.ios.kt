package com.sarah.app.domain.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSince1970

actual fun currentTimeEpochMs(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual fun formatDueDate(deadlineEpochMs: Long?): String? {
    if (deadlineEpochMs == null) return null
    val date = NSDate.dateWithTimeIntervalSince1970(deadlineEpochMs / 1000.0)
    val cal = NSCalendar.currentCalendar
    return when {
        cal.isDateInToday(date) -> "Today"
        cal.isDateInTomorrow(date) -> "Tomorrow"
        date.timeIntervalSince1970 < NSDate().timeIntervalSince1970 -> "Overdue"
        else -> {
            val formatter = NSDateFormatter().apply { dateFormat = "MMM d" }
            formatter.stringFromDate(date)
        }
    }
}

actual fun isDueDateOverdue(deadlineEpochMs: Long?): Boolean {
    if (deadlineEpochMs == null) return false
    val date = NSDate.dateWithTimeIntervalSince1970(deadlineEpochMs / 1000.0)
    return date.timeIntervalSince1970 < NSDate().timeIntervalSince1970
}

actual fun formatReminderTime(reminderTimeEpochMs: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(reminderTimeEpochMs / 1000.0)
    val cal = NSCalendar.currentCalendar
    val timeFormatter = NSDateFormatter().apply { dateFormat = "h:mm a" }
    val timeStr = timeFormatter.stringFromDate(date)
    return when {
        cal.isDateInToday(date) -> "Today, $timeStr"
        cal.isDateInTomorrow(date) -> "Tomorrow, $timeStr"
        else -> {
            val dateFormatter = NSDateFormatter().apply { dateFormat = "MMM d" }
            "${dateFormatter.stringFromDate(date)}, $timeStr"
        }
    }
}

actual fun formatDateTime(epochMs: Long): String {
    val date = NSDate.dateWithTimeIntervalSince1970(epochMs / 1000.0)
    val formatter = NSDateFormatter().apply { dateFormat = "EEE, MMM d · h:mm a" }
    return formatter.stringFromDate(date)
}

actual fun formatEpochDay(epochDay: Long): String {
    val epochMs = epochDay * 86_400_000L
    val date = NSDate.dateWithTimeIntervalSince1970(epochMs / 1000.0)
    val formatter = NSDateFormatter().apply { dateFormat = "EEEE, MMMM d, yyyy" }
    return formatter.stringFromDate(date)
}

actual fun getStartOfTodayEpochMs(): Long {
    val cal = NSCalendar.currentCalendar
    val date = NSDate()
    val comps = cal.components(0u, fromDate = date)
    val startOfDay = cal.startOfDayForDate(date)
    return (startOfDay.timeIntervalSince1970 * 1000.0).toLong()
}
