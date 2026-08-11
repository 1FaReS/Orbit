package com.flowtask.app.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flowtask.app.domain.model.FocusSession
import com.flowtask.app.domain.model.FocusSessionStatus
import com.flowtask.app.domain.model.Priority
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ProjectSummary
import com.flowtask.app.domain.model.Routine
import com.flowtask.app.domain.model.RoutineItem
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskFilter
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.domain.model.ThemeMode
import com.flowtask.app.domain.model.UserPreferences
import com.flowtask.app.domain.reminder.TaskReminderScheduler
import com.flowtask.app.domain.repository.FocusSessionRepository
import com.flowtask.app.domain.repository.ProjectRepository
import com.flowtask.app.domain.repository.RoutineRepository
import com.flowtask.app.domain.repository.TaskRepository
import com.flowtask.app.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
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
    val focusSessions: List<FocusSession> = emptyList(),
    val routines: List<Routine> = emptyList(),
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
    val activeFocus: FocusSession? get() = focusSessions.firstOrNull {
        it.status == FocusSessionStatus.ACTIVE || it.status == FocusSessionStatus.PAUSED
    }
    val focusedMinutes: Int get() = focusSessions.filter { it.status == FocusSessionStatus.COMPLETED }
        .sumOf { it.durationMinutes }
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
    private val focusSessionRepository: FocusSessionRepository,
    private val routineRepository: RoutineRepository,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())
    private val filter = MutableStateFlow(TaskListFilter.ALL)
    val messages = MutableSharedFlow<TaskManagerMessage>(extraBufferCapacity = 1)

    private data class OrbitData(
        val preferences: UserPreferences,
        val tasks: List<Task>,
        val projects: List<Project>,
        val sessions: List<FocusSession>,
        val routines: List<Routine>,
    )

    private val orbitData = combine(
        preferencesRepository.preferences,
        taskRepository.observeTasks(TaskFilter()),
        projectRepository.observeProjects(),
        focusSessionRepository.observeSessions(),
        routineRepository.observeRoutines(),
    ) { preferences, tasks, projects, sessions, routines ->
        OrbitData(preferences, tasks, projects, sessions, routines)
    }

    val uiState = combine(orbitData, selectedDate, filter) { data, date, activeFilter ->
        TaskManagerUiState(
            preferences = data.preferences,
            tasks = data.tasks,
            projects = data.projects,
            focusSessions = data.sessions,
            routines = data.routines,
            selectedDate = date,
            filter = activeFilter,
            loading = false,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TaskManagerUiState())

    init {
        viewModelScope.launch {
            runCatching { seedInitialContent() }
                .onFailure { messages.emit(TaskManagerMessage.Error("Unable to prepare sample data")) }
        }
    }

    fun finishOnboarding() = updatePreferences { it.copy(onboardingCompleted = true) }
    fun resetOnboarding() = updatePreferences { it.copy(onboardingCompleted = false) }
    fun selectDate(date: LocalDate) { selectedDate.value = date }
    fun setFilter(value: TaskListFilter) { filter.value = value }

    fun saveTask(task: Task, successMessage: String, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching { taskRepository.saveTask(task) }
            .onSuccess { taskId ->
                if (uiState.value.preferences.notificationsEnabled) reminderScheduler.schedule(task.copy(id = taskId))
                messages.emit(TaskManagerMessage.Success(successMessage))
                onSuccess()
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to save task")) }
    }

    fun toggleTask(task: Task) = viewModelScope.launch {
        val completed = task.status != TaskStatus.COMPLETED
        runCatching { taskRepository.setCompleted(task.id, completed) }
            .onSuccess {
                if (completed || !uiState.value.preferences.notificationsEnabled) reminderScheduler.cancel(task.id)
                else reminderScheduler.schedule(task.copy(status = TaskStatus.TODO, completedAt = null))
            }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to update task")) }
    }

    fun rescheduleTask(task: Task, date: LocalDate, time: LocalTime) = viewModelScope.launch {
        val scheduled = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant()
        runCatching { taskRepository.saveTask(task.copy(dueDate = date, dueTime = time, scheduledStart = scheduled)) }
            .onSuccess { messages.emit(TaskManagerMessage.Success("Moved to ${time.withSecond(0).withNano(0)}")) }
            .onFailure { messages.emit(TaskManagerMessage.Error("Couldn’t reschedule task")) }
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
            .onSuccess { messages.emit(TaskManagerMessage.Success(successMessage)); onSuccess() }
            .onFailure { messages.emit(TaskManagerMessage.Error(it.message ?: "Unable to save project")) }
    }

    fun deleteProject(project: Project) = viewModelScope.launch { projectRepository.deleteProject(project.id) }

    fun startFocus(task: Task, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching {
            focusSessionRepository.saveSession(
                FocusSession(taskId = task.id, startedAt = Instant.now(), durationMinutes = task.estimatedDurationMinutes),
            )
            taskRepository.saveTask(task.copy(status = TaskStatus.IN_PROGRESS))
        }.onSuccess { onSuccess() }
            .onFailure { messages.emit(TaskManagerMessage.Error("Couldn’t start focus")) }
    }

    fun setFocusPaused(session: FocusSession, paused: Boolean) = viewModelScope.launch {
        focusSessionRepository.saveSession(
            session.copy(status = if (paused) FocusSessionStatus.PAUSED else FocusSessionStatus.ACTIVE),
        )
    }

    fun finishFocus(session: FocusSession, completeTask: Boolean, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        runCatching {
            val elapsed = ChronoUnit.MINUTES.between(session.startedAt, Instant.now()).toInt().coerceAtLeast(1)
            focusSessionRepository.saveSession(
                session.copy(endedAt = Instant.now(), durationMinutes = elapsed, status = FocusSessionStatus.COMPLETED),
            )
            if (completeTask) session.taskId?.let { taskRepository.setCompleted(it, true) }
        }.onSuccess { onSuccess() }
            .onFailure { messages.emit(TaskManagerMessage.Error("Couldn’t finish focus")) }
    }

    fun cancelFocus(session: FocusSession, onSuccess: () -> Unit = {}) = viewModelScope.launch {
        focusSessionRepository.saveSession(session.copy(endedAt = Instant.now(), status = FocusSessionStatus.CANCELLED))
        onSuccess()
    }

    fun updateNotifications(enabled: Boolean) = viewModelScope.launch {
        runCatching {
            preferencesRepository.update { it.copy(notificationsEnabled = enabled) }
            if (enabled) reminderScheduler.replaceAll(uiState.value.tasks) else reminderScheduler.cancelAll()
        }.onFailure { messages.emit(TaskManagerMessage.Error("Unable to update notification settings")) }
    }

    fun updateTheme(mode: ThemeMode) = updatePreferences { it.copy(themeMode = mode) }
    fun updateHaptics(enabled: Boolean) = updatePreferences { it.copy(hapticsEnabled = enabled) }
    fun update24HourTime(enabled: Boolean) = updatePreferences { it.copy(use24HourTime = enabled) }
    fun updateWeekStart(monday: Boolean) = updatePreferences { it.copy(weekStartsOnMonday = monday) }
    fun updateDefaultDuration(minutes: Int) = updatePreferences { it.copy(defaultTaskDurationMinutes = minutes) }

    fun saveRoutine(routine: Routine) = viewModelScope.launch {
        runCatching { routineRepository.saveRoutine(routine) }
            .onSuccess { messages.emit(TaskManagerMessage.Success("Routine saved")) }
            .onFailure { messages.emit(TaskManagerMessage.Error("Couldn’t save routine")) }
    }

    fun deleteRoutine(routine: Routine) = viewModelScope.launch { routineRepository.deleteRoutine(routine.id) }

    private fun updatePreferences(transform: (UserPreferences) -> UserPreferences) = viewModelScope.launch {
        preferencesRepository.update(transform)
    }

    private suspend fun seedInitialContent() {
        val existingProjects = projectRepository.observeProjects().first()
        if (existingProjects.isEmpty()) {
            listOf(
                Project(name = "Work", groupName = "Work", colorId = "blue", icon = "briefcase"),
                Project(name = "Personal", groupName = "Personal", colorId = "violet", icon = "person"),
                Project(name = "Study", groupName = "Learning", colorId = "green", icon = "book"),
                Project(name = "Fitness", groupName = "Personal", colorId = "brown", icon = "fitness"),
            ).forEach { projectRepository.saveProject(it) }
        }
        val existingTasks = taskRepository.observeTasks(TaskFilter()).first()
        if (existingTasks.isEmpty()) {
            val today = LocalDate.now()
            listOf(
                Task(title = "Morning routine", category = "Personal", dueDate = today, dueTime = LocalTime.of(8, 0), estimatedDurationMinutes = 45, status = TaskStatus.COMPLETED),
                Task(title = "Check emails", category = "Work", dueDate = today, dueTime = LocalTime.of(9, 30), estimatedDurationMinutes = 25, status = TaskStatus.COMPLETED),
                Task(title = "Work on Android portfolio", description = "Refine the timeline interaction and capture the final walkthrough.", category = "Work", dueDate = today, dueTime = LocalTime.of(10, 15), estimatedDurationMinutes = 90, status = TaskStatus.IN_PROGRESS, priority = Priority.HIGH),
                Task(title = "Lunch", category = "Personal", dueDate = today, dueTime = LocalTime.of(12, 30), estimatedDurationMinutes = 45),
                Task(title = "Study Kotlin Coroutines", category = "Study", dueDate = today, dueTime = LocalTime.of(14, 0), estimatedDurationMinutes = 75),
                Task(title = "Gym", category = "Fitness", dueDate = today, dueTime = LocalTime.of(17, 30), estimatedDurationMinutes = 60),
                Task(title = "Personal project", category = "Personal", dueDate = today, dueTime = LocalTime.of(20, 0), estimatedDurationMinutes = 60),
            ).forEach { taskRepository.saveTask(it) }
        }
        if (routineRepository.observeRoutines().first().isEmpty()) {
            routineRepository.saveRoutine(
                Routine(
                    name = "Morning routine",
                    items = listOf("Drink water", "Plan the day", "Check calendar").mapIndexed { index, title ->
                        RoutineItem(title = title, position = index)
                    },
                    repeatDays = DayOfWeek.entries.filter { it.value <= 5 }.toSet(),
                    reminderTime = LocalTime.of(7, 45),
                ),
            )
        }
        preferencesRepository.update { it.copy(sampleDataSeeded = true) }
    }
}
