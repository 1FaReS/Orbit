package com.flowtask.app.feature.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.domain.model.Task
import com.flowtask.app.feature.timeline.OrbitTimeline
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarScreen(
    selectedDate: LocalDate,
    tasks: List<Task>,
    weekStartsOnMonday: Boolean,
    use24HourTime: Boolean,
    hapticsEnabled: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit,
    onReschedule: (Task, LocalDate, LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val firstDay = if (weekStartsOnMonday) DayOfWeek.MONDAY else DayOfWeek.SUNDAY
    var weekAnchor by remember(selectedDate) { mutableStateOf(selectedDate.with(TemporalAdjusters.previousOrSame(firstDay))) }
    val days = remember(weekAnchor) { List(7) { weekAnchor.plusDays(it.toLong()) } }
    val dayTasks = tasks.filter { it.dueDate == selectedDate }

    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 28.dp, bottom = 32.dp),
    ) {
        item {
            Column(Modifier.padding(end = 20.dp)) {
                OrbitBrandHeader(
                    eyebrow = "Calendar",
                    title = "Give your week a shape.",
                    subtitle = "Move through your plans one day at a time.",
                )
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                ) {
                    Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(weekAnchor.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { weekAnchor = weekAnchor.minusWeeks(1) }) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Previous week") }
                        IconButton(onClick = { weekAnchor = weekAnchor.plusWeeks(1) }) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Next week") }
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    days.forEach { date ->
                        DayCell(
                            date = date,
                            selected = date == selectedDate,
                            hasTasks = tasks.any { it.dueDate == date },
                            onClick = { onDateSelected(date) },
                        )
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp), verticalAlignment = Alignment.Bottom) {
                    Column {
                        Text("SCHEDULE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text(selectedDate.format(DateTimeFormatter.ofPattern("EEEE")), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 3.dp))
                        Text("${dayTasks.size} scheduled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.weight(1f))
                    if (selectedDate != LocalDate.now()) Text("Jump to today", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onDateSelected(LocalDate.now()); weekAnchor = LocalDate.now().with(TemporalAdjusters.previousOrSame(firstDay)) })
                }
            }
        }
        if (dayTasks.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(top = 72.dp, end = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nothing planned", style = MaterialTheme.typography.titleMedium)
                    Text("This day is open.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            item {
                OrbitTimeline(
                    date = selectedDate,
                    tasks = dayTasks,
                    use24HourTime = use24HourTime,
                    hapticsEnabled = hapticsEnabled,
                    onTaskClick = onTaskClick,
                    onToggle = onToggle,
                    onReschedule = { task, time -> onReschedule(task, selectedDate, time) },
                )
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, selected: Boolean, hasTasks: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (selected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
    ) {
        Column(
            Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(date.format(DateTimeFormatter.ofPattern("EEE")).take(1).uppercase(), style = MaterialTheme.typography.labelMedium, color = if (selected) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
            androidx.compose.foundation.layout.Box(
                Modifier.padding(top = 6.dp).size(4.dp).background(
                    if (hasTasks) MaterialTheme.colorScheme.secondary else androidx.compose.ui.graphics.Color.Transparent,
                    androidx.compose.foundation.shape.CircleShape,
                ),
            )
        }
    }
}
