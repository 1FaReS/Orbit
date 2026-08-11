package com.flowtask.app.feature.analytics

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.domain.model.FocusSession
import com.flowtask.app.domain.model.FocusSessionStatus
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.feature.timeline.durationLabel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Composable
fun AnalyticsScreen(
    tasks: List<Task>,
    sessions: List<FocusSession>,
    onOpenReplay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val days = remember(weekStart) { List(7) { weekStart.plusDays(it.toLong()) } }
    val rates = days.map { date ->
        val dayTasks = tasks.filter { it.dueDate == date }
        if (dayTasks.isEmpty()) 0f else dayTasks.count { it.status == TaskStatus.COMPLETED }.toFloat() / dayTasks.size
    }
    val weekTasks = tasks.filter { it.dueDate in weekStart..weekStart.plusDays(6) }
    val completed = weekTasks.count { it.status == TaskStatus.COMPLETED }
    val completion = if (weekTasks.isEmpty()) 0 else (completed * 100f / weekTasks.size).toInt()
    val focusMinutes = sessions.filter { session ->
        session.status == FocusSessionStatus.COMPLETED &&
            session.startedAt.atZone(ZoneId.systemDefault()).toLocalDate() in weekStart..weekStart.plusDays(6)
    }.sumOf { it.durationMinutes }
    val bestIndex = rates.indices.maxByOrNull { rates[it] } ?: 0

    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            OrbitBrandHeader(
                eyebrow = "Your rhythm",
                title = "A week in motion.",
                subtitle = "Small wins add up faster than you think.",
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text("THIS WEEK", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth().padding(top = 5.dp), verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
                        Text("$completion%", style = MaterialTheme.typography.displaySmall)
                        Text(" complete", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f), modifier = Modifier.padding(bottom = 5.dp))
                    }
                    Text("${completed} finished tasks · ${durationLabel(focusMinutes)} in focus", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f), modifier = Modifier.padding(top = 2.dp))
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("WEEKLY PULSE", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    WeeklyChart(days, rates, Modifier.fillMaxWidth().height(192.dp).padding(top = 14.dp))
                }
            }
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            ) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    MetricRow("Focus", durationLabel(focusMinutes), "Across completed sessions")
                    HorizontalDivider()
                    MetricRow("Tasks completed", completed.toString(), "${weekTasks.size} scheduled")
                    HorizontalDivider()
                    MetricRow("Average daily focus", durationLabel(focusMinutes / 7), "Monday through Sunday")
                    HorizontalDivider()
                    MetricRow(
                        "Most productive day",
                        days[bestIndex].format(DateTimeFormatter.ofPattern("EEEE")),
                        if (rates[bestIndex] > 0f) "${(rates[bestIndex] * 100).toInt()}% complete" else "No completed tasks yet",
                    )
                    HorizontalDivider()
                    Row(
                        Modifier.fillMaxWidth().clickable(onClick = onOpenReplay).padding(vertical = 18.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Day Replay", style = MaterialTheme.typography.titleMedium)
                            Text("See today unfold chronologically", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Open Day Replay", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyChart(days: List<LocalDate>, values: List<Float>, modifier: Modifier = Modifier) {
    val progress by animateFloatAsState(1f, label = "chart-reveal")
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val track = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(modifier) {
        val chartHeight = size.height - 32.dp.toPx()
        val slot = size.width / days.size
        val barWidth = 9.dp.toPx()
        values.forEachIndexed { index, value ->
            val centerX = slot * index + slot / 2
            drawLine(track, Offset(centerX, 0f), Offset(centerX, chartHeight), strokeWidth = barWidth, cap = StrokeCap.Round)
            if (value > 0f) {
                val color = when (index % 3) {
                    0 -> secondary
                    1 -> primary
                    else -> tertiary
                }
                drawLine(color, Offset(centerX, chartHeight), Offset(centerX, chartHeight * (1f - value * progress)), strokeWidth = barWidth, cap = StrokeCap.Round)
            }
            drawContext.canvas.nativeCanvas.drawText(
                days[index].format(DateTimeFormatter.ofPattern("E")).take(1),
                centerX,
                size.height,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.argb((labelColor.alpha * 255).toInt(), (labelColor.red * 255).toInt(), (labelColor.green * 255).toInt(), (labelColor.blue * 255).toInt())
                    textSize = 11.dp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    isAntiAlias = true
                },
            )
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String, note: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        }
        Text(value, style = MaterialTheme.typography.titleLarge)
    }
}
