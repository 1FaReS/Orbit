package com.flowtask.app.presentation

import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskManagerUiStateTest {
    private val today = LocalDate.now()
    private val office = Project(id = 1, name = "Office")

    @Test
    fun `filters tasks for the selected date and status`() {
        val completed = Task(id = 1, title = "Finished", category = office.name, dueDate = today, status = TaskStatus.COMPLETED)
        val pending = Task(id = 2, title = "Pending", category = office.name, dueDate = today, status = TaskStatus.TODO)
        val tomorrow = Task(id = 3, title = "Tomorrow", category = office.name, dueDate = today.plusDays(1), status = TaskStatus.COMPLETED)
        val unscheduled = Task(id = 4, title = "Unscheduled", category = office.name, status = TaskStatus.COMPLETED)

        val state = TaskManagerUiState(
            tasks = listOf(completed, pending, tomorrow, unscheduled),
            projects = listOf(office),
            selectedDate = today,
            filter = TaskListFilter.COMPLETED,
            loading = false,
        )

        assertEquals(listOf(completed), state.visibleTasks)
    }

    @Test
    fun `calculates dashboard and project progress`() {
        val state = TaskManagerUiState(
            tasks = listOf(
                Task(id = 1, title = "Finished", category = office.name, dueDate = today, status = TaskStatus.COMPLETED),
                Task(id = 2, title = "Pending", category = office.name, dueDate = today, status = TaskStatus.TODO),
            ),
            projects = listOf(office),
            loading = false,
        )

        assertEquals(0.5f, state.todayProgress)
        assertEquals(2, state.projectSummaries.single().taskCount)
        assertEquals(1, state.projectSummaries.single().completedCount)
        assertTrue(state.projectSummaries.single().progress == 0.5f)
    }
}
