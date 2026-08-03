package com.flowtask.app.data.repository

import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TaskCompletionConsistencyTest {
    private val now = Instant.parse("2026-08-03T12:00:00Z")

    @Test
    fun `completed task receives a completion timestamp`() {
        val task = Task(title = "Finish report", status = TaskStatus.COMPLETED)

        assertEquals(now, task.withConsistentCompletionTime(now).completedAt)
    }

    @Test
    fun `existing completion timestamp is preserved`() {
        val completedEarlier = now.minusSeconds(300)
        val task = Task(
            title = "Finish report",
            status = TaskStatus.COMPLETED,
            completedAt = completedEarlier,
        )

        assertEquals(completedEarlier, task.withConsistentCompletionTime(now).completedAt)
    }

    @Test
    fun `reopened task clears its completion timestamp`() {
        val task = Task(
            title = "Finish report",
            status = TaskStatus.TODO,
            completedAt = now.minusSeconds(300),
        )

        assertNull(task.withConsistentCompletionTime(now).completedAt)
    }
}
