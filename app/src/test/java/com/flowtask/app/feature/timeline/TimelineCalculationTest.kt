package com.flowtask.app.feature.timeline

import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class TimelineCalculationTest {
    @Test fun `minute offset is relative to timeline start`() {
        assertEquals(0, minuteOffset(LocalTime.of(7, 0)))
        assertEquals(195, minuteOffset(LocalTime.of(10, 15)))
    }

    @Test fun `drag result snaps to quarter hour`() {
        assertEquals(LocalTime.of(10, 30), snappedTime(LocalTime.of(10, 7), 17))
        assertEquals(LocalTime.of(9, 45), snappedTime(LocalTime.of(10, 7), -20))
    }

    @Test fun `drag result remains inside visible day`() {
        assertEquals(LocalTime.of(7, 0), snappedTime(LocalTime.of(7, 15), -300))
        assertEquals(LocalTime.of(22, 45), snappedTime(LocalTime.of(22, 30), 300))
    }
}
