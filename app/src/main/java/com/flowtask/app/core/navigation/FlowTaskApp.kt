package com.flowtask.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.flowtask.app.presentation.TaskManagerMessage
import com.flowtask.app.presentation.TaskManagerViewModel
import com.flowtask.app.presentation.taskmanager.AppPrimary
import com.flowtask.app.presentation.taskmanager.HomeScreen
import com.flowtask.app.presentation.taskmanager.ProfileScreen
import com.flowtask.app.presentation.taskmanager.ProjectDetailScreen
import com.flowtask.app.presentation.taskmanager.ProjectEditorScreen
import com.flowtask.app.presentation.taskmanager.ProjectsScreen
import com.flowtask.app.presentation.taskmanager.TaskEditorScreen
import com.flowtask.app.presentation.taskmanager.TodayTasksScreen
import com.flowtask.app.presentation.taskmanager.WelcomeScreen

private object Routes {
    const val Home = "home"
    const val Today = "today"
    const val Projects = "projects"
    const val Profile = "profile"
    const val NewProject = "project/new"
    const val ProjectDetail = "project/{projectId}"
    const val NewTask = "task/new/{projectId}"
    const val EditTask = "task/edit/{taskId}"
}

@Composable
fun FlowTaskApp(viewModel: TaskManagerViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    if (state.loading) return
    if (!state.preferences.onboardingCompleted) {
        WelcomeScreen(onStart = viewModel::finishOnboarding)
        return
    }

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val bottomRoutes = setOf(Routes.Home, Routes.Today, Routes.Projects, Routes.Profile)

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
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    AppNavItem(route == Routes.Home, { navController.navigateTop(Routes.Home) }, Icons.Outlined.Home, "Home")
                    AppNavItem(route == Routes.Today, { navController.navigateTop(Routes.Today) }, Icons.Outlined.CalendarMonth, "Today")
                    AppNavItem(false, { navController.navigate("task/new/-1") }, Icons.Filled.Add, "Add")
                    AppNavItem(route == Routes.Projects, { navController.navigateTop(Routes.Projects) }, Icons.Outlined.Folder, "Projects")
                    AppNavItem(route == Routes.Profile, { navController.navigateTop(Routes.Profile) }, Icons.Outlined.Person, "Profile")
                }
            }
        },
    ) { outerPadding ->
        NavHost(navController, startDestination = Routes.Home, modifier = Modifier.padding(outerPadding)) {
            composable(Routes.Home) {
                HomeScreen(
                    tasks = state.tasks,
                    projectSummaries = state.projectSummaries,
                    progress = state.todayProgress,
                    onViewToday = { navController.navigateTop(Routes.Today) },
                    onNotifications = { navController.navigateTop(Routes.Profile) },
                    onAddProject = { navController.navigate(Routes.NewProject) },
                    onProjectClick = { navController.navigate("project/${it.id}") },
                    onTaskClick = { navController.navigate("task/edit/${it.id}") },
                )
            }
            composable(Routes.Today) {
                TodayTasksScreen(
                    selectedDate = state.selectedDate,
                    filter = state.filter,
                    tasks = state.visibleTasks,
                    projects = state.projects,
                    onBack = { navController.navigateTop(Routes.Home) },
                    onNotifications = { navController.navigateTop(Routes.Profile) },
                    onDateSelected = viewModel::selectDate,
                    onFilterSelected = viewModel::setFilter,
                    onToggle = viewModel::toggleTask,
                    onTaskClick = { navController.navigate("task/edit/${it.id}") },
                    onAddTask = { navController.navigate("task/new/-1") },
                )
            }
            composable(Routes.Projects) {
                ProjectsScreen(
                    state.projectSummaries,
                    onBack = { navController.navigateTop(Routes.Home) },
                    onAdd = { navController.navigate(Routes.NewProject) },
                    onProjectClick = { navController.navigate("project/${it.id}") },
                )
            }
            composable(Routes.Profile) {
                ProfileScreen(
                    state.preferences.notificationsEnabled,
                    viewModel::updateNotifications,
                    viewModel::resetOnboarding,
                )
            }
            composable(Routes.NewProject) {
                ProjectEditorScreen(null, { navController.popBackStack() }) { project ->
                    viewModel.saveProject(project, "Project added") {
                        navController.popBackStack()
                    }
                }
            }
            composable(
                Routes.ProjectDetail,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("projectId") ?: return@composable
                val project = state.projects.firstOrNull { it.id == id } ?: return@composable
                ProjectDetailScreen(
                    project,
                    state.tasks,
                    { navController.popBackStack() },
                    onAddTask = { navController.navigate("task/new/$id") },
                    onToggle = viewModel::toggleTask,
                    onTaskClick = { navController.navigate("task/edit/${it.id}") },
                )
            }
            composable(
                Routes.NewTask,
                arguments = listOf(navArgument("projectId") { type = NavType.LongType }),
            ) { entry ->
                val projectId = entry.arguments?.getLong("projectId") ?: -1
                TaskEditorScreen(
                    initial = null,
                    projects = state.projects,
                    preselectedProject = state.projects.firstOrNull { it.id == projectId }?.name,
                    onBack = { navController.popBackStack() },
                    onSave = { task ->
                        viewModel.saveTask(task, "Task added") {
                            navController.popBackStack()
                        }
                    },
                    onDelete = null,
                )
            }
            composable(
                Routes.EditTask,
                arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("taskId") ?: return@composable
                val task = state.tasks.firstOrNull { it.id == id } ?: return@composable
                TaskEditorScreen(
                    initial = task,
                    projects = state.projects,
                    preselectedProject = null,
                    onBack = { navController.popBackStack() },
                    onSave = { updated ->
                        viewModel.saveTask(updated, "Task updated") {
                            navController.popBackStack()
                        }
                    },
                    onDelete = { deleting ->
                        viewModel.deleteTask(deleting) {
                            navController.popBackStack()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.AppNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, label, tint = if (selected || label == "Add") AppPrimary else MaterialTheme.colorScheme.onSurfaceVariant) },
        alwaysShowLabel = false,
    )
}

private fun androidx.navigation.NavHostController.navigateTop(route: String) {
    navigate(route) {
        popUpTo(Routes.Home) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
