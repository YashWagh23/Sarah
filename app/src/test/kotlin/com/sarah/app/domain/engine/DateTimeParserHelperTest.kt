package com.sarah.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateTimeParserHelperTest {

    private val zone = ZoneId.of("UTC")
    private val fixedDate = LocalDate.of(2026, 8, 15) // Saturday

    @Test
    fun `test tomorrow parsing produces next day timestamp`() {
        val result = DateTimeParserHelper.parseDeadline("due tomorrow", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 16), localDate)
    }

    @Test
    fun `test Monday deadline from Saturday returns next Monday`() {
        val result = DateTimeParserHelper.parseDeadline("submit by Monday", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 17), localDate) // Next Monday
    }

    @Test
    fun `test in 3 days offset parsing`() {
        val result = DateTimeParserHelper.parseDeadline("due in 3 days", fixedDate, zone)
        assertNotNull(result)

        val localDate = Instant.ofEpochMilli(result!!).atZone(zone).toLocalDate()
        assertEquals(LocalDate.of(2026, 8, 18), localDate)
    }

    @Test
    fun `test explicit 5 PM time parsing`() {
        val result = DateTimeParserHelper.parseDeadline("tomorrow at 5 pm", fixedDate, zone)
        assertNotNull(result)

        val localTime = Instant.ofEpochMilli(result!!).atZone(zone).toLocalTime()
        assertEquals(17, localTime.hour)
        assertEquals(0, localTime.minute)
    }
}
