package com.flowtask.app.presentation.taskmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ProjectSummary
import com.flowtask.app.domain.model.Task
import com.flowtask.app.presentation.TaskListFilter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun TodayTasksScreen(
    selectedDate: LocalDate,
    filter: TaskListFilter,
    tasks: List<Task>,
    projects: List<Project>,
    onBack: () -> Unit,
    onNotifications: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onFilterSelected: (TaskListFilter) -> Unit,
    onToggle: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
    onAddTask: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask, containerColor = AppPrimary, contentColor = Color.White) {
                Icon(Icons.Outlined.Add, "Add task")
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                    Text(
                        selectedDate.screenTitle(),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(onClick = onNotifications) { Icon(Icons.Outlined.Notifications, "Notifications") }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val dates = (-2L..2L).map { selectedDate.plusDays(it) }
                    items(dates, key = { it.toEpochDay() }) { date ->
                        DateCard(date, selected = date == selectedDate, onClick = { onDateSelected(date) })
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TaskListFilter.entries) { item ->
                        FilterChip(
                            selected = filter == item,
                            onClick = { onFilterSelected(item) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
            if (tasks.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Your schedule is clear", style = MaterialTheme.typography.titleMedium)
                        Text("Add a task for this day.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        project = projects.firstOrNull { it.name == task.category },
                        onToggle = { onToggle(task) },
                        onClick = { onTaskClick(task) },
                    )
                }
            }
        }
    }
}

private val TaskListFilter.label: String get() = when (this) {
    TaskListFilter.ALL -> "All"
    TaskListFilter.TODO -> "To do"
    TaskListFilter.IN_PROGRESS -> "In Progress"
    TaskListFilter.COMPLETED -> "Completed"
}

private fun LocalDate.screenTitle(): String = if (this == LocalDate.now()) {
    "Today’s Tasks"
} else {
    format(DateTimeFormatter.ofPattern("EEE, MMM d"))
}

@Composable
private fun DateCard(date: LocalDate, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AppPrimary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            Modifier.width(58.dp).padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelSmall)
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun ProjectsScreen(
    summaries: List<ProjectSummary>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onProjectClick: (Project) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = AppPrimary, contentColor = Color.White) {
                Icon(Icons.Outlined.Add, "Add project")
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                    Text("Task Groups", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
            if (summaries.isEmpty()) {
                item {
                    EmptyState("No task groups yet", "Create a project to organize your tasks.")
                }
            }
            items(summaries, key = { it.project.id }) { summary ->
                ProjectSummaryRow(summary, onClick = { onProjectClick(summary.project) })
            }
        }
    }
}

@Composable
fun ProjectDetailScreen(
    project: Project,
    tasks: List<Task>,
    onBack: () -> Unit,
    onAddTask: () -> Unit,
    onToggle: (Task) -> Unit,
    onTaskClick: (Task) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTask, containerColor = AppPrimary, contentColor = Color.White) {
                Icon(Icons.Outlined.Add, "Add task")
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp, 12.dp, 18.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                    Column {
                        Text(project.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(project.groupName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (project.description.isNotBlank()) item {
                SoftCard(Modifier.fillMaxWidth()) {
                    Text(project.description, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            }
            val projectTasks = tasks.filter { it.category == project.name }
            if (projectTasks.isEmpty()) {
                item {
                    EmptyState("Nothing here yet", "Add the first task to this project.")
                }
            }
            items(projectTasks, key = { it.id }) { task ->
                TaskRow(task, project, { onToggle(task) }, { onTaskClick(task) })
            }
        }
    }
}

@Composable
private fun EmptyState(title: String, message: String) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
