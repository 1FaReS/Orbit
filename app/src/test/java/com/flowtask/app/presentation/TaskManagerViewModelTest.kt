package com.flowtask.app.presentation

import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskFilter
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.domain.model.UserPreferences
import com.flowtask.app.domain.repository.ProjectRepository
import com.flowtask.app.domain.repository.TaskRepository
import com.flowtask.app.domain.repository.UserPreferencesRepository
import com.flowtask.app.domain.reminder.TaskReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TaskManagerViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `sample content is seeded once`() = runTest {
        val tasks = FakeTaskRepository()
        val projects = FakeProjectRepository()
        val preferences = FakePreferencesRepository()

        val reminders = FakeTaskReminderScheduler()
        TaskManagerViewModel(tasks, projects, preferences, reminders)

        assertEquals(6, tasks.savedCount)
        assertEquals(3, projects.savedCount)
        assertTrue(preferences.current.sampleDataSeeded)

        TaskManagerViewModel(tasks, projects, preferences, reminders)

        assertEquals(6, tasks.savedCount)
        assertEquals(3, projects.savedCount)
    }
}

private class FakeTaskReminderScheduler : TaskReminderScheduler {
    override fun schedule(task: Task) = Unit
    override fun cancel(taskId: Long) = Unit
    override fun replaceAll(tasks: List<Task>) = Unit
    override fun cancelAll() = Unit
}

private class FakePreferencesRepository : UserPreferencesRepository {
    private val state = MutableStateFlow(UserPreferences())
    override val preferences: Flow<UserPreferences> = state
    val current: UserPreferences get() = state.value

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        state.value = transform(state.value)
    }
}

private class FakeProjectRepository : ProjectRepository {
    private val state = MutableStateFlow<List<Project>>(emptyList())
    var savedCount = 0
        private set

    override fun observeProjects(): Flow<List<Project>> = state

    override suspend fun saveProject(project: Project): Long {
        savedCount += 1
        val saved = project.copy(id = project.id.takeIf { it != 0L } ?: savedCount.toLong())
        state.value = state.value.filterNot { it.id == saved.id } + saved
        return saved.id
    }

    override suspend fun deleteProject(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }
}

private class FakeTaskRepository : TaskRepository {
    private val state = MutableStateFlow<List<Task>>(emptyList())
    var savedCount = 0
        private set

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> = state

    override fun observeTask(id: Long): Flow<Task?> = MutableStateFlow(state.value.firstOrNull { it.id == id })

    override suspend fun saveTask(task: Task): Long {
        savedCount += 1
        val saved = task.copy(id = task.id.takeIf { it != 0L } ?: savedCount.toLong())
        state.value = state.value.filterNot { it.id == saved.id } + saved
        return saved.id
    }

    override suspend fun setCompleted(taskId: Long, completed: Boolean) {
        state.value = state.value.map { task ->
            if (task.id == taskId) {
                task.copy(status = if (completed) TaskStatus.COMPLETED else TaskStatus.TODO)
            } else {
                task
            }
        }
    }

    override suspend fun deleteTask(taskId: Long) {
        state.value = state.value.filterNot { it.id == taskId }
    }

    override suspend fun clearCompleted() {
        state.value = state.value.filterNot { it.status == TaskStatus.COMPLETED }
    }
}
