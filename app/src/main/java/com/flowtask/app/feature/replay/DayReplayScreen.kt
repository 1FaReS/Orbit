package com.flowtask.app.feature.replay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.domain.model.FocusSession
import com.flowtask.app.domain.model.FocusSessionStatus
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.feature.timeline.durationLabel
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

private data class ReplayMoment(val time: String, val title: String, val detail: String)

@Composable
fun DayReplayScreen(
    tasks: List<Task>,
    sessions: List<FocusSession>,
    onBack: () -> Unit,
) {
    val today = LocalDate.now()
    val dayTasks = tasks.filter { it.dueDate == today }
    val completed = dayTasks.filter { it.status == TaskStatus.COMPLETED }
    val daySessions = sessions.filter {
        it.startedAt.atZone(ZoneId.systemDefault()).toLocalDate() == today && it.status == FocusSessionStatus.COMPLETED
    }
    val moments = remember(dayTasks, sessions) {
        buildList {
            add(ReplayMoment("07:42", "Day started", "Your first plan was ready"))
            completed.sortedBy { it.dueTime }.forEach { task ->
                add(ReplayMoment(task.dueTime?.toString() ?: "—", task.title, "${task.category ?: "Personal"} · ${durationLabel(task.actualDurationMinutes.takeIf { it > 0 } ?: task.estimatedDurationMinutes)}"))
            }
            daySessions.forEach { session ->
                val start = session.startedAt.atZone(ZoneId.systemDefault()).toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                val end = session.endedAt?.atZone(ZoneId.systemDefault())?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "—"
                val task = tasks.firstOrNull { it.id == session.taskId }
                add(ReplayMoment("$start — $end", task?.title ?: "Focus session", "${durationLabel(session.durationMinutes)} focused"))
            }
        }.distinctBy { it.time to it.title }.sortedBy { it.time.take(5) }
    }

    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 40.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                Spacer(Modifier.weight(1f))
            }
            OrbitBrandHeader(
                eyebrow = "Your ${today.dayOfWeek.name.lowercase().replaceFirstChar(Char::uppercase)}",
                title = "A day in motion.",
                subtitle = today.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
            )
        }
        itemsIndexed(moments, key = { index, moment -> "$index-${moment.title}" }) { index, moment ->
            ReplayRow(moment, index, index == moments.lastIndex)
        }
        item {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 28.dp),
            ) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                    SummaryValue("${completed.size} / ${dayTasks.size}", "tasks")
                    Spacer(Modifier.weight(1f))
                    SummaryValue(durationLabel(daySessions.sumOf { it.durationMinutes }), "focused")
                }
            }
        }
    }
}

@Composable
private fun ReplayRow(moment: ReplayMoment, index: Int, last: Boolean) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 70L); visible = true }
    AnimatedVisibility(visible, enter = fadeIn() + slideInVertically { it / 3 }) {
        Row(Modifier.fillMaxWidth().height(108.dp).padding(horizontal = 24.dp)) {
            Column(Modifier.width(48.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(top = 8.dp).size(9.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                if (!last) Box(Modifier.width(1.dp).weight(1f).background(MaterialTheme.colorScheme.outline))
            }
            Column(Modifier.padding(start = 10.dp, top = 3.dp)) {
                Text(moment.time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(moment.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
                Text(moment.detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
            }
        }
    }
}

@Composable
private fun SummaryValue(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
