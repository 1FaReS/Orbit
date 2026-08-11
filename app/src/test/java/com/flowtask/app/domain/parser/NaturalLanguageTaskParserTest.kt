package com.flowtask.app.domain.parser

import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NaturalLanguageTaskParserTest {
    private val parser = NaturalLanguageTaskParser()
    private val tuesday = LocalDate.of(2026, 8, 11)

    @Test fun `parses tomorrow evening task`() {
        val result = parser.parse("Gym tomorrow at 6pm for one hour", tuesday)
        assertEquals("Gym", result.title)
        assertEquals(tuesday.plusDays(1), result.date)
        assertEquals(LocalTime.of(18, 0), result.time)
        assertEquals(60, result.durationMinutes)
    }

    @Test fun `parses upcoming weekday and time`() {
        val result = parser.parse("Meeting Friday 2pm", tuesday)
        assertEquals(LocalDate.of(2026, 8, 14), result.date)
        assertEquals(LocalTime.of(14, 0), result.time)
        assertEquals("Meeting", result.title)
    }

    @Test fun `parses tonight and hour duration`() {
        val result = parser.parse("Study Kotlin for 2 hours tonight", tuesday)
        assertEquals(LocalTime.of(19, 0), result.time)
        assertEquals(120, result.durationMinutes)
        assertEquals("Study Kotlin", result.title)
        assertTrue(result.recognizedTime)
    }

    @Test fun `uses morning default`() {
        val result = parser.parse("Call John Monday morning", tuesday)
        assertEquals(LocalDate.of(2026, 8, 17), result.date)
        assertEquals(LocalTime.of(9, 0), result.time)
    }
}
