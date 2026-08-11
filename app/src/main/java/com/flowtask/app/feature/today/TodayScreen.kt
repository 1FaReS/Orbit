package com.flowtask.app.feature.today

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.core.designsystem.components.OrbitGradientButton
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.feature.timeline.OrbitTimeline
import com.flowtask.app.feature.timeline.durationLabel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TodayScreen(
    tasks: List<Task>,
    use24HourTime: Boolean,
    hapticsEnabled: Boolean,
    activeFocusTask: Task?,
    onToggle: (Task) -> Unit,
    onReschedule: (Task, LocalTime) -> Unit,
    onEdit: (Task) -> Unit,
    onStartFocus: (Task) -> Unit,
    onOpenFocus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    val completed = tasks.count { it.status == TaskStatus.COMPLETED }
    val progress = if (tasks.isEmpty()) 0f else completed.toFloat() / tasks.size

    BoxWithConstraints(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isTablet = maxWidth >= 840.dp
        Row(Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier.weight(if (isTablet) .66f else 1f).fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, top = 28.dp, end = 0.dp, bottom = 32.dp),
            ) {
        item {
            Column(Modifier.padding(end = 20.dp)) {
                OrbitBrandHeader(
                    eyebrow = greeting(),
                    title = "Make today count.",
                    subtitle = if (tasks.isEmpty()) {
                        "A clear day is a good place to begin."
                    } else {
                        "${tasks.size} plans in your orbit · ${LocalDate.now().format(DateTimeFormatter.ofPattern("EEE, MMM d"))}"
                    },
                )
                OrbitWeekRail(today = LocalDate.now(), modifier = Modifier.padding(top = 14.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                ) {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                        AnimatedContent(completed, transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(120)) }, label = "completion") { count ->
                            Text(
                                if (tasks.isEmpty()) "Nothing planned yet" else "$count of ${tasks.size} complete",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Text(
                            if (tasks.isEmpty()) "Add one small thing to get started." else "Little progress is still progress.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .76f),
                            modifier = Modifier.padding(top = 3.dp),
                        )
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.weight(1f).height(5.dp).semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f) }.background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .13f), MaterialTheme.shapes.extraSmall)) {
                                Box(Modifier.fillMaxWidth(progress).height(5.dp).background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.extraSmall))
                            }
                            Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.padding(start = 12.dp))
                        }
                    }
                }
                if (activeFocusTask != null) {
                    Surface(
                        onClick = onOpenFocus,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(32.dp).background(MaterialTheme.colorScheme.secondary, CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.PlayArrow, null, tint = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.size(18.dp))
                            }
                            Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                Text("Focus in progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                                Text(activeFocusTask.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                            Text("Open", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Your flow", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.weight(1f))
                    Text("Hold and drag to move", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (tasks.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 48.dp, end = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Your afternoon is clear.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Add something when you’re ready.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            item {
                OrbitTimeline(
                    date = LocalDate.now(),
                    tasks = tasks,
                    use24HourTime = use24HourTime,
                    hapticsEnabled = hapticsEnabled,
                    onTaskClick = { selectedTask = it },
                    onToggle = onToggle,
                    onReschedule = onReschedule,
                )
            }
        }
            }
            if (isTablet) {
                TabletDayPanel(tasks, completed, Modifier.weight(.34f).fillMaxSize())
            }
        }
    }

    selectedTask?.let { task ->
        TaskDetailsSheet(
            task = task,
            onDismiss = { selectedTask = null },
            onComplete = { onToggle(task); selectedTask = null },
            onStartFocus = { onStartFocus(task); selectedTask = null },
            onReschedule = {
                onReschedule(task, (task.dueTime ?: LocalTime.of(9, 0)).plusMinutes(30))
                selectedTask = null
            },
            onEdit = { onEdit(task); selectedTask = null },
        )
    }
}

@Composable
private fun TabletDayPanel(tasks: List<Task>, completed: Int, modifier: Modifier = Modifier) {
    Column(
        modifier.background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Text("Day overview", style = MaterialTheme.typography.titleLarge)
        Text("$completed of ${tasks.size} complete", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        HorizontalDivider(Modifier.padding(vertical = 24.dp))
        Text("UP NEXT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        tasks.filter { it.status != TaskStatus.COMPLETED }.sortedBy { it.dueTime }.take(4).forEach { task ->
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Top) {
                Text(task.dueTime?.toString() ?: "—", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Column(Modifier.padding(start = 16.dp)) {
                    Text(task.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text("${task.category ?: "Unsorted"} · ${durationLabel(task.estimatedDurationMinutes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TaskDetailsSheet(
    task: Task,
    onDismiss: () -> Unit,
    onComplete: () -> Unit,
    onStartFocus: () -> Unit,
    onReschedule: () -> Unit,
    onEdit: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 32.dp)) {
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Text(task.category ?: "Unsorted", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }
            Text(task.title, style = MaterialTheme.typography.headlineMedium)
            Text(
                "${task.category ?: "Unsorted"} · ${task.dueTime ?: "Any time"} · ${durationLabel(task.estimatedDurationMinutes)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (task.description.isNotBlank()) Text(task.description, modifier = Modifier.padding(top = 20.dp), style = MaterialTheme.typography.bodyLarge)
            if (task.subtasks.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical = 20.dp))
                Text("Subtasks", style = MaterialTheme.typography.labelLarge)
                task.subtasks.forEach { Text("•  ${it.title}", modifier = Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Row(Modifier.fillMaxWidth().padding(top = 28.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OrbitGradientButton(onClick = onStartFocus, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.PlayArrow, null)
                    Text("Start focus", Modifier.padding(start = 6.dp))
                }
                OutlinedButton(onClick = onComplete, shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Outlined.CheckCircle, "Complete")
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onReschedule, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Schedule, null, modifier = Modifier.size(18.dp))
                    Text("30 min later", Modifier.padding(start = 6.dp))
                }
                TextButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp))
                    Text("Edit", Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun OrbitWeekRail(today: LocalDate, modifier: Modifier = Modifier) {
    val days = List(7) { today.minusDays(3).plusDays(it.toLong()) }
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            days.forEach { date ->
                val selected = date == today
                Surface(
                    color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (selected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(date.format(DateTimeFormatter.ofPattern("EE")).take(1), style = MaterialTheme.typography.labelMedium)
                        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                    }
                }
            }
        }
    }
}

private fun greeting(): String = when (LocalTime.now().hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}
