package com.flowtask.app.data.local

import com.flowtask.app.domain.model.Priority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FlowTaskTypeConvertersTest {
    private val converters = FlowTaskTypeConverters()

    @Test
    fun `persistent value types round trip without losing information`() {
        val instant = Instant.parse("2026-08-03T10:15:30Z")
        val date = LocalDate.of(2026, 8, 3)
        val time = LocalTime.of(16, 45)

        assertEquals(instant, converters.longToInstant(converters.instantToLong(instant)))
        assertEquals(date, converters.stringToDate(converters.dateToString(date)))
        assertEquals(time, converters.stringToTime(converters.timeToString(time)))
        assertEquals(Priority.HIGH, converters.stringToPriority(converters.priorityToString(Priority.HIGH)))
        assertEquals(setOf(1, 3, 7), converters.stringToIntSet(converters.intSetToString(setOf(7, 1, 3))))
    }
}
