package com.flowtask.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowtask.app.domain.model.Priority
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ProjectSummary
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskFilter
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.domain.model.UserPreferences
import com.flowtask.app.domain.repository.ProjectRepository
import com.flowtask.app.domain.repository.TaskRepository
import com.flowtask.app.domain.repository.UserPreferencesRepository
import com.flowtask.app.domain.reminder.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskListFilter { ALL, TODO, IN_PROGRESS, COMPLETED }

data class TaskManagerUiState(
    val preferences: UserPreferences = UserPreferences(),
    val tasks: List<Task> = emptyList(),
    val projects: List<Project> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val filter: TaskListFilter = TaskListFilter.ALL,
    val loading: Boolean = true,
) {
    val visibleTasks: List<Task> get() = tasks.filter { task ->
        task.dueDate == selectedDate && when (filter) {
            TaskListFilter.ALL -> true
            TaskListFilter.TODO -> task.status == TaskStatus.TODO
            TaskListFilter.IN_PROGRESS -> task.status == TaskStatus.IN_PROGRESS
            TaskListFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
        }
    }

    val todayTasks: List<Task> get() = tasks.filter { it.dueDate == LocalDate.now() }
    val completedToday: Int get() = todayTasks.count { it.status == TaskStatus.COMPLETED }
    val todayProgress: Float get() = if (todayTasks.isEmpty()) 0f else completedToday.toFloat() / todayTasks.size
    val inProgress: List<Task> get() = tasks.filter { it.status == TaskStatus.IN_PROGRESS }
    val projectSummaries: List<ProjectSummary> get() = projects.map { project ->
        val projectTasks = tasks.filter { it.category == project.name }
        ProjectSummary(project, projectTasks.size, projectTasks.count { it.status == TaskStatus.COMPLETED })
    }
}

sealed interface TaskManagerMessage {
    data class Success(val message: String) : TaskManagerMessage
    data class Error(val message: String) : TaskManagerMessage
}

@HiltViewModel
class TaskManagerViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val reminderScheduler: TaskReminderScheduler,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val filter = MutableStateFlow(TaskListFilter.ALL)
    val messages = MutableSharedFlow<TaskManagerMessage>(extraBufferCapacity = 1)

    val uiState = combine(
        preferencesRepository.preferences,
        taskRepository.observeTasks(TaskFilter()),
        projectRepository.observeProjects(),
        selectedDate,
        filter,
    ) { preferences, tasks, projects, date, activeFilter ->
        TaskManagerUiState(preferences, tasks, projects, date, activeFilter, loading = false)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TaskManagerUiState())

    init {
        viewModelScope.launch {
            runCatching { seedInitialContent() }
                .onFailure { messages.emit(TaskManagerMessage.Error("Unable to prepare sample data")) }
        }
    }

    fun finishOnboarding() = viewModelScope.launch {
        preferencesRepository.update { it.copy(onboardingCompleted = true) }
    }

    fun selectDate(date: LocalDate) { selectedDate.value = date }
    fun setFilter(value: TaskListFilter) { filter.value = value }

    fun saveTask(task: Task, successMessage: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching { taskRepository.saveTask(task) }
            .onSuccess { taskId ->
                if (uiState.value.preferences.notificationsEnabled) {
                    reminderScheduler.schedule(task.copy(id = taskId))
                }
                messages.emit(TaskManagerMessage.Success(successMessage))
                onSuccess()
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to save task")) }
    }

    fun toggleTask(task: Task) = viewModelScope.launch {
        val completed = task.status != TaskStatus.COMPLETED
        runCatching { taskRepository.setCompleted(task.id, completed) }
            .onSuccess {
                if (completed || !uiState.value.preferences.notificationsEnabled) {
                    reminderScheduler.cancel(task.id)
                } else {
                    reminderScheduler.schedule(task.copy(status = TaskStatus.TODO, completedAt = null))
                }
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to update task")) }
    }

    fun deleteTask(task: Task, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching { taskRepository.deleteTask(task.id) }
            .onSuccess {
                reminderScheduler.cancel(task.id)
                messages.emit(TaskManagerMessage.Success("Task deleted"))
                onSuccess()
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to delete task")) }
    }

    fun saveProject(project: Project, successMessage: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching { projectRepository.saveProject(project) }
            .onSuccess {
                messages.emit(TaskManagerMessage.Success(successMessage))
                onSuccess()
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to save project")) }
    }

    fun deleteProject(project: Project) = viewModelScope.launch {
        projectRepository.deleteProject(project.id)
    }

    fun updateNotifications(enabled: Boolean) = viewModelScope.launch {
        runCatching {
            preferencesRepository.update { it.copy(notificationsEnabled = enabled) }
            if (enabled) reminderScheduler.replaceAll(uiState.value.tasks) else reminderScheduler.cancelAll()
        }.onFailure {
            messages.emit(TaskManagerMessage.Error("Unable to update notification settings"))
        }
    }

    fun resetOnboarding() = viewModelScope.launch {
        preferencesRepository.update { it.copy(onboardingCompleted = false) }
    }

    private suspend fun seedInitialContent() {
        if (preferencesRepository.preferences.first().sampleDataSeeded) return

        val existingProjects = projectRepository.observeProjects().first()
        if (existingProjects.isEmpty()) {
            listOf(
                Project(name = "Office Project", groupName = "Work", colorId = "pink", icon = "briefcase"),
                Project(name = "Personal Project", groupName = "Personal", colorId = "violet", icon = "person"),
                Project(name = "Daily Study", groupName = "Learning", colorId = "orange", icon = "book"),
            ).forEach { projectRepository.saveProject(it) }
        }
        val existingTasks = taskRepository.observeTasks(TaskFilter()).first()
        if (existingTasks.isEmpty()) {
            val today = LocalDate.now()
            listOf(
                Task(
                    title = "Market Research",
                    category = "Office Project",
                    dueDate = today,
                    dueTime = LocalTime.of(10, 0),
                    status = TaskStatus.COMPLETED,
                    priority = Priority.HIGH,
                ),
                Task(
                    title = "Competitive Analysis",
                    category = "Office Project",
                    dueDate = today,
                    dueTime = LocalTime.of(12, 0),
                    status = TaskStatus.IN_PROGRESS,
                    priority = Priority.HIGH,
                ),
                Task(
                    title = "Create Low-fidelity Wireframe",
                    category = "Personal Project",
                    dueDate = today,
                    dueTime = LocalTime.of(19, 0),
                ),
                Task(
                    title = "How to pitch a Design Sprint",
                    category = "Daily Study",
                    dueDate = today,
                    dueTime = LocalTime.of(21, 0),
                ),
                Task(
                    title = "Grocery shopping app design",
                    category = "Office Project",
                    dueDate = today.plusDays(1),
                    dueTime = LocalTime.of(9, 30),
                    status = TaskStatus.IN_PROGRESS,
                ),
                Task(
                    title = "User Eats redesign challenge",
                    category = "Personal Project",
                    dueDate = today.plusDays(2),
                    dueTime = LocalTime.of(14, 0),
                    status = TaskStatus.IN_PROGRESS,
                ),
            ).forEach { taskRepository.saveTask(it) }
        }
        preferencesRepository.update { it.copy(sampleDataSeeded = true) }
    }
}
