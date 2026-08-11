package com.flowtask.app.core.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flowtask.app.core.designsystem.theme.OrbitTheme
import com.flowtask.app.domain.model.ThemeMode
import com.flowtask.app.feature.analytics.AnalyticsScreen
import com.flowtask.app.feature.calendar.CalendarScreen
import com.flowtask.app.feature.focus.FocusScreen
import com.flowtask.app.feature.replay.DayReplayScreen
import com.flowtask.app.feature.routines.RoutinesScreen
import com.flowtask.app.feature.settings.SettingsScreen
import com.flowtask.app.feature.tasks.QuickAddSheet
import com.flowtask.app.feature.today.TodayScreen
import com.flowtask.app.presentation.TaskManagerMessage
import com.flowtask.app.presentation.TaskManagerViewModel
import com.flowtask.app.presentation.taskmanager.TaskEditorScreen
import java.time.LocalDate

private object Routes {
    const val Today = "today"
    const val Calendar = "calendar"
    const val Analytics = "analytics"
    const val Settings = "settings"
    const val Focus = "focus"
    const val Replay = "replay"
    const val Routines = "routines"
    const val NewTask = "task/new"
    const val EditTask = "task/edit/{taskId}"
}

@Composable
fun OrbitApp(viewModel: TaskManagerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (state.preferences.themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }
    OrbitTheme(darkTheme = darkTheme) {
        OrbitNavigation(viewModel)
    }
}

@Composable
private fun OrbitNavigation(viewModel: TaskManagerViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val bottomRoutes = setOf(Routes.Today, Routes.Calendar, Routes.Analytics, Routes.Settings)
    var quickAddOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(
                when (message) {
                    is TaskManagerMessage.Error -> message.message
                    is TaskManagerMessage.Success -> message.message
                },
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (route in bottomRoutes) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
                    shadowElevation = 8.dp,
                ) {
                    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.Transparent, tonalElevation = 0.dp) {
                        OrbitNavItem(route == Routes.Today, { navController.navigateTop(Routes.Today) }, Icons.Outlined.Today, "Today")
                        OrbitNavItem(route == Routes.Calendar, { navController.navigateTop(Routes.Calendar) }, Icons.Outlined.CalendarMonth, "Calendar")
                        OrbitNavItem(false, { quickAddOpen = true }, Icons.Filled.Add, "Add")
                        OrbitNavItem(route == Routes.Analytics, { navController.navigateTop(Routes.Analytics) }, Icons.Outlined.BarChart, "Analytics")
                        OrbitNavItem(route == Routes.Settings, { navController.navigateTop(Routes.Settings) }, Icons.Outlined.Settings, "Settings")
                    }
                }
            }
        },
    ) { outerPadding ->
        NavHost(navController, startDestination = Routes.Today, modifier = Modifier.padding(outerPadding)) {
            composable(Routes.Today) {
                val activeTask = state.activeFocus?.taskId?.let { id -> state.tasks.firstOrNull { it.id == id } }
                TodayScreen(
                    tasks = state.todayTasks,
                    use24HourTime = state.preferences.use24HourTime,
                    hapticsEnabled = state.preferences.hapticsEnabled,
                    activeFocusTask = activeTask,
                    onToggle = viewModel::toggleTask,
                    onReschedule = { task, time -> viewModel.rescheduleTask(task, LocalDate.now(), time) },
                    onEdit = { navController.navigate("task/edit/${it.id}") },
                    onStartFocus = { task -> viewModel.startFocus(task) { navController.navigate(Routes.Focus) } },
                    onOpenFocus = { navController.navigate(Routes.Focus) },
                )
            }
            composable(Routes.Calendar) {
                CalendarScreen(
                    selectedDate = state.selectedDate,
                    tasks = state.tasks,
                    weekStartsOnMonday = state.preferences.weekStartsOnMonday,
                    use24HourTime = state.preferences.use24HourTime,
                    hapticsEnabled = state.preferences.hapticsEnabled,
                    onDateSelected = viewModel::selectDate,
                    onTaskClick = { navController.navigate("task/edit/${it.id}") },
                    onToggle = viewModel::toggleTask,
                    onReschedule = viewModel::rescheduleTask,
                )
            }
            composable(Routes.Analytics) {
                AnalyticsScreen(state.tasks, state.focusSessions, onOpenReplay = { navController.navigate(Routes.Replay) })
            }
            composable(Routes.Settings) {
                SettingsScreen(
                    preferences = state.preferences,
                    onThemeChanged = viewModel::updateTheme,
                    onNotificationsChanged = viewModel::updateNotifications,
                    onHapticsChanged = viewModel::updateHaptics,
                    on24HourChanged = viewModel::update24HourTime,
                    onWeekStartChanged = viewModel::updateWeekStart,
                    onDefaultDurationChanged = viewModel::updateDefaultDuration,
                    onOpenRoutines = { navController.navigate(Routes.Routines) },
                    onOpenReplay = { navController.navigate(Routes.Replay) },
                    exportText = state.toExportJson(),
                )
            }
            composable(Routes.Focus) {
                val session = state.activeFocus
                val task = session?.taskId?.let { id -> state.tasks.firstOrNull { it.id == id } }
                FocusScreen(
                    session = session,
                    task = task,
                    onPauseChanged = { paused -> session?.let { viewModel.setFocusPaused(it, paused) } },
                    onFinish = { session?.let { viewModel.finishFocus(it, completeTask = true) { navController.popBackStack() } } },
                    onCancel = { session?.let { viewModel.cancelFocus(it) { navController.popBackStack() } } },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.Replay) { DayReplayScreen(state.tasks, state.focusSessions, onBack = { navController.popBackStack() }) }
            composable(Routes.Routines) {
                RoutinesScreen(state.routines, onBack = { navController.popBackStack() }, onSave = viewModel::saveRoutine, onDelete = viewModel::deleteRoutine)
            }
            composable(Routes.NewTask) {
                TaskEditorScreen(
                    initial = null,
                    projects = state.projects,
                    preselectedProject = state.projects.firstOrNull()?.name,
                    onBack = { navController.popBackStack() },
                    onSave = { task -> viewModel.saveTask(task, "Added to timeline") { navController.popBackStack() } },
                    onDelete = null,
                )
            }
            composable(Routes.EditTask, arguments = listOf(navArgument("taskId") { type = NavType.LongType })) { entry ->
                val id = entry.arguments?.getLong("taskId") ?: return@composable
                val task = state.tasks.firstOrNull { it.id == id } ?: return@composable
                TaskEditorScreen(
                    initial = task,
                    projects = state.projects,
                    preselectedProject = null,
                    onBack = { navController.popBackStack() },
                    onSave = { updated -> viewModel.saveTask(updated, "Task updated") { navController.popBackStack() } },
                    onDelete = { deleting -> viewModel.deleteTask(deleting) { navController.popBackStack() } },
                )
            }
        }
    }

    if (quickAddOpen) {
        QuickAddSheet(
            projects = state.projects,
            defaultDurationMinutes = state.preferences.defaultTaskDurationMinutes,
            defaultReminderMinutes = state.preferences.defaultReminderMinutes,
            onDismiss = { quickAddOpen = false },
            onSave = { task -> viewModel.saveTask(task, "Added to timeline"); quickAddOpen = false },
            onOpenFullEditor = { quickAddOpen = false; navController.navigate(Routes.NewTask) },
        )
    }
}

private fun com.flowtask.app.presentation.TaskManagerUiState.toExportJson(): String {
    fun String.json(): String = replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    return buildString {
        append("{\n  \"version\": 1,\n  \"tasks\": [\n")
        tasks.forEachIndexed { index, task ->
            append("    {\"id\": ${task.id}, \"title\": \"${task.title.json()}\", \"date\": \"${task.dueDate ?: ""}\", \"time\": \"${task.dueTime ?: ""}\", \"durationMinutes\": ${task.estimatedDurationMinutes}, \"completed\": ${task.status == com.flowtask.app.domain.model.TaskStatus.COMPLETED}}")
            if (index != tasks.lastIndex) append(',')
            append('\n')
        }
        append("  ],\n  \"routines\": [\n")
        routines.forEachIndexed { index, routine ->
            append("    {\"id\": ${routine.id}, \"name\": \"${routine.name.json()}\", \"enabled\": ${routine.enabled}}")
            if (index != routines.lastIndex) append(',')
            append('\n')
        }
        append("  ]\n}")
    }
}

@Composable
private fun RowScope.OrbitNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, label, tint = if (selected || label == "Add") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant) },
        label = { androidx.compose.material3.Text(label) },
        alwaysShowLabel = false,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.tertiaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

private fun androidx.navigation.NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(Routes.Today) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
