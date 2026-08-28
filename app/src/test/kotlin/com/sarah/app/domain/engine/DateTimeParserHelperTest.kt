package com.sarah.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class DateTimeParserHelperTest {

    private val zone = TimeZone.UTC
    private val fixedDate = LocalDate(2026, 8, 15) // Saturday

    @Test
    fun `test tomorrow parsing produces next day timestamp`() {
        val result = DateTimeParserHelper.parseDeadline("due tomorrow", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.fromEpochMilliseconds(result!!).toLocalDateTime(zone).date
        assertEquals(LocalDate(2026, 8, 16), localDate)
    }

    @Test
    fun `test Monday deadline from Saturday returns next Monday`() {
        val result = DateTimeParserHelper.parseDeadline("submit by Monday", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.fromEpochMilliseconds(result!!).toLocalDateTime(zone).date
        assertEquals(LocalDate(2026, 8, 17), localDate) // Next Monday
    }

    @Test
    fun `test in 3 days offset parsing`() {
        val result = DateTimeParserHelper.parseDeadline("due in 3 days", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.fromEpochMilliseconds(result!!).toLocalDateTime(zone).date
        assertEquals(LocalDate(2026, 8, 18), localDate)
    }

    @Test
    fun `test explicit 5 PM time parsing`() {
        val result = DateTimeParserHelper.parseDeadline("tomorrow at 5 pm", fixedDate, zone)
        assertNotNull(result)

        val localTime = Instant.fromEpochMilliseconds(result!!).toLocalDateTime(zone).time
        assertEquals(17, localTime.hour)
        assertEquals(0, localTime.minute)
    }
}
