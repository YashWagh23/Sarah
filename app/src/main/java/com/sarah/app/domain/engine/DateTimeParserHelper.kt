package com.sarah.app.domain.engine

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale
import java.util.regex.Pattern

object DateTimeParserHelper {

    fun parseDeadline(
        text: String,
        currentDate: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        val lower = text.lowercase(Locale.US)

        // 1. Check relative day offsets
        val targetDate: LocalDate = when {
            lower.contains("day after tomorrow") -> currentDate.plusDays(2)
            lower.contains("tomorrow") || lower.contains("tmrw") || lower.contains("tomm") -> currentDate.plusDays(1)
            lower.contains("today") || lower.contains("tonight") -> currentDate
            else -> {
                // Check "in X days"
                val inDaysMatch = Pattern.compile("in\\s+(\\d+)\\s+days?").matcher(lower)
                if (inDaysMatch.find()) {
                    val days = inDaysMatch.group(1)?.toLongOrNull() ?: 1L
                    currentDate.plusDays(days)
                } else {
                    // Check named days of the week
                    findNextDayOfWeek(lower, currentDate) ?: return null
                }
            }
        }

        // 2. Check explicit time markers (e.g., "5 pm", "9:30 am", "17:00", "by 11pm")
        val targetTime = parseTimeMarker(lower) ?: LocalTime.of(23, 59)

        return targetDate.atTime(targetTime).atZone(zoneId).toInstant().toEpochMilli()
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
            val regex = Pattern.compile("\\b(next\\s+|this\\s+|by\\s+|on\\s+)?$name\\b")
            val matcher = regex.matcher(text)
            if (matcher.find()) {
                var daysUntil = targetDay.value - currentDate.dayOfWeek.value
                if (daysUntil <= 0) {
                    daysUntil += 7
                }
                return currentDate.plusDays(daysUntil.toLong())
            }
        }
        return null
    }

    private fun parseTimeMarker(text: String): LocalTime? {
        // 12-hour format e.g. "5 pm", "5:30 pm", "9am", "11:15 AM"
        val time12Regex = Pattern.compile("\\b(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
        val match12 = time12Regex.matcher(text)
        if (match12.find()) {
            val rawHour = match12.group(1)?.toIntOrNull() ?: 9
            val minute = match12.group(2)?.toIntOrNull() ?: 0
            val amPm = match12.group(3)?.lowercase(Locale.US) ?: "pm"

            var hour24 = rawHour % 12
            if (amPm == "pm") {
                hour24 += 12
            }
            return runCatching { LocalTime.of(hour24, minute) }.getOrNull()
        }

        // 24-hour format e.g. "17:00", "09:30"
        val time24Regex = Pattern.compile("\\b([01]?\\d|2[0-3]):([0-5]\\d)\\b")
        val match24 = time24Regex.matcher(text)
        if (match24.find()) {
            val hour = match24.group(1)?.toIntOrNull() ?: 12
            val minute = match24.group(2)?.toIntOrNull() ?: 0
            return runCatching { LocalTime.of(hour, minute) }.getOrNull()
        }

        if (text.contains("morning")) return LocalTime.of(9, 0)
        if (text.contains("afternoon")) return LocalTime.of(14, 0)
        if (text.contains("evening") || text.contains("tonight")) return LocalTime.of(20, 0)

        return null
    }
}
