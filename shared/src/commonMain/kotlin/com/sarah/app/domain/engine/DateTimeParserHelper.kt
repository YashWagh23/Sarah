package com.sarah.app.domain.engine

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

object DateTimeParserHelper {

    fun parseDeadline(
        text: String,
        currentDate: LocalDate? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): Long? {
        val lower = text.lowercase()
        val baseDate = currentDate ?: Clock.System.now().toLocalDateTime(timeZone).date

        // 1. Check relative day offsets
        val targetDate: LocalDate = when {
            lower.contains("day after tomorrow") -> baseDate.plus(2, DateTimeUnit.DAY)
            lower.contains("tomorrow") || lower.contains("tmrw") || lower.contains("tomm") -> baseDate.plus(1, DateTimeUnit.DAY)
            lower.contains("today") || lower.contains("tonight") -> baseDate
            else -> {
                // Check "in X days"
                val inDaysMatch = Regex("in\\s+(\\d+)\\s+days?").find(lower)
                if (inDaysMatch != null) {
                    val days = inDaysMatch.groupValues[1].toIntOrNull() ?: 1
                    baseDate.plus(days, DateTimeUnit.DAY)
                } else {
                    // Check named days of the week
                    findNextDayOfWeek(lower, baseDate) ?: return null
                }
            }
        }

        // 2. Check explicit time markers (e.g., "5 pm", "9:30 am", "17:00", "by 11pm")
        val targetTime = parseTimeMarker(lower) ?: LocalTime(23, 59)

        return targetDate.atTime(targetTime).toInstant(timeZone).toEpochMilliseconds()
    }

    private fun findNextDayOfWeek(text: String, currentDate: LocalDate): LocalDate? {
        val daysMap = mapOf(
            "monday" to DayOfWeek.MONDAY,
            "mon" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "tue" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "wed" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "thu" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "fri" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sat" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY,
            "sun" to DayOfWeek.SUNDAY
        )

        for ((name, targetDay) in daysMap) {
            val regex = Regex("\\b(next\\s+|this\\s+|by\\s+|on\\s+)?$name\\b")
            if (regex.containsMatchIn(text)) {
                var daysUntil = targetDay.isoDayNumber - currentDate.dayOfWeek.isoDayNumber
                if (daysUntil <= 0) {
                    daysUntil += 7
                }
                return currentDate.plus(daysUntil, DateTimeUnit.DAY)
            }
        }
        return null
    }

    private fun parseTimeMarker(text: String): LocalTime? {
        // 12-hour format e.g. "5 pm", "5:30 pm", "9am", "11:15 AM"
        val time12Regex = Regex("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
        val match12 = time12Regex.find(text)
        if (match12 != null) {
            val rawHour = match12.groupValues[1].toIntOrNull() ?: 9
            val minute = match12.groupValues.getOrNull(2)?.toIntOrNull() ?: 0
            val amPm = match12.groupValues.getOrNull(3)?.lowercase() ?: "pm"

            var hour24 = rawHour % 12
            if (amPm == "pm") {
                hour24 += 12
            }
            return runCatching { LocalTime(hour24, minute) }.getOrNull()
        }

        // 24-hour format e.g. "17:00", "09:30"
        val time24Regex = Regex("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b")
        val match24 = time24Regex.find(text)
        if (match24 != null) {
            val hour = match24.groupValues[1].toIntOrNull() ?: 12
            val minute = match24.groupValues[2].toIntOrNull() ?: 0
            return runCatching { LocalTime(hour, minute) }.getOrNull()
        }

        if (text.contains("morning")) return LocalTime(9, 0)
        if (text.contains("afternoon")) return LocalTime(14, 0)
        if (text.contains("evening") || text.contains("tonight")) return LocalTime(20, 0)

        return null
    }
}
