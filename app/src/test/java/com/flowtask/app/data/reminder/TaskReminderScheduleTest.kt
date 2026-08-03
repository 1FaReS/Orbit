package com.flowtask.app.data.reminder

import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskReminderScheduleTest {
    private val now = Instant.parse("2026-08-03T10:00:00Z")

    @Test
    fun `future task returns its delay`() {
        val task = Task(
            id = 7,
            title = "Design review",
            dueDate = LocalDate.of(2026, 8, 3),
            dueTime = LocalTime.of(10, 30),
        )

        assertEquals(30 * 60 * 1000L, task.reminderDelayMillis(now, ZoneOffset.UTC))
    }

    @Test
    fun `completed and overdue tasks are not scheduled`() {
        val overdue = Task(
            id = 7,
            title = "Design review",
            dueDate = LocalDate.of(2026, 8, 3),
            dueTime = LocalTime.of(9, 30),
        )
        val completed = overdue.copy(
            dueTime = LocalTime.of(10, 30),
            status = TaskStatus.COMPLETED,
        )

        assertNull(overdue.reminderDelayMillis(now, ZoneOffset.UTC))
        assertNull(completed.reminderDelayMillis(now, ZoneOffset.UTC))
    }
}
