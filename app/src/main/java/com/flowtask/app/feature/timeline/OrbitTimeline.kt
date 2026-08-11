package com.flowtask.app.feature.timeline

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.flowtask.app.core.designsystem.theme.OrbitFitness
import com.flowtask.app.core.designsystem.theme.OrbitPersonal
import com.flowtask.app.core.designsystem.theme.OrbitStudy
import com.flowtask.app.core.designsystem.theme.OrbitWork
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val StartHour = 7
private const val EndHour = 22
private val HourHeight = 84.dp
private val TimeColumnWidth = 58.dp

fun minuteOffset(time: LocalTime, startHour: Int = StartHour): Int =
    ((time.hour - startHour) * 60 + time.minute).coerceAtLeast(0)

fun snappedTime(time: LocalTime, deltaMinutes: Int): LocalTime {
    val total = (time.hour * 60 + time.minute + deltaMinutes).coerceIn(StartHour * 60, EndHour * 60 + 45)
    val snapped = ((total / 15f).roundToInt() * 15).coerceIn(StartHour * 60, EndHour * 60 + 45)
    return LocalTime.of(snapped / 60, snapped % 60)
}

@Composable
fun OrbitTimeline(
    date: LocalDate,
    tasks: List<Task>,
    use24HourTime: Boolean,
    hapticsEnabled: Boolean,
    onTaskClick: (Task) -> Unit,
    onToggle: (Task) -> Unit,
    onReschedule: (Task, LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hours = EndHour - StartHour + 1
    val totalHeight = HourHeight * hours
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val faintLine = MaterialTheme.colorScheme.outlineVariant
    val nowColor = MaterialTheme.colorScheme.secondary
    val now = LocalTime.now()

    Box(modifier.fillMaxWidth().requiredHeight(totalHeight)) {
        Canvas(Modifier.matchParentSize()) {
            val hourPx = HourHeight.toPx()
            val x = TimeColumnWidth.toPx()
            drawLine(lineColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
            repeat(hours) { index ->
                val y = index * hourPx
                drawLine(faintLine, Offset(x, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                if (index < hours - 1) {
                    drawLine(
                        faintLine.copy(alpha = .55f),
                        Offset(x, y + hourPx / 2),
                        Offset(size.width, y + hourPx / 2),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 6.dp.toPx())),
                    )
                }
            }
            if (date == LocalDate.now() && now.hour in StartHour..EndHour) {
                val y = minuteOffset(now) / 60f * hourPx
                drawCircle(nowColor, radius = 4.dp.toPx(), center = Offset(x, y))
                drawLine(nowColor, Offset(x, y), Offset(size.width, y), strokeWidth = 1.5.dp.toPx())
            }
        }

        repeat(hours) { index ->
            val hour = LocalTime.of(StartHour + index, 0)
            Text(
                text = hour.format(if (use24HourTime) DateTimeFormatter.ofPattern("HH:mm") else DateTimeFormatter.ofPattern("h a")),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.offset(y = HourHeight * index).width(TimeColumnWidth).padding(top = 4.dp),
            )
        }

        tasks.filter { it.dueTime != null }.sortedBy { it.dueTime }.forEach { task ->
            TimelineTask(
                task = task,
                hapticsEnabled = hapticsEnabled,
                onClick = { onTaskClick(task) },
                onToggle = { onToggle(task) },
                onReschedule = { onReschedule(task, it) },
            )
        }
    }
}

@Composable
private fun TimelineTask(
    task: Task,
    hapticsEnabled: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onReschedule: (LocalTime) -> Unit,
) {
    val start = task.dueTime ?: return
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val hourPx = with(density) { HourHeight.toPx() }
    var dragOffset by remember(task.id, start) { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    var lastSnap by remember { mutableIntStateOf(0) }
    val baseY = HourHeight * (minuteOffset(start) / 60f)
    val animatedBaseY by animateDpAsState(baseY, label = "task-position")
    val taskHeight: Dp = (HourHeight * (task.estimatedDurationMinutes / 60f)).coerceIn(48.dp, 124.dp)
    val categoryColor = categoryColor(task.category)
    val background by animateColorAsState(
        when (task.status) {
            TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primaryContainer
            TaskStatus.COMPLETED -> MaterialTheme.colorScheme.surfaceVariant
            else -> categoryColor.copy(alpha = .13f)
        },
        label = "task-state",
    )
    val previewStart = snappedTime(start, (dragOffset / hourPx * 60f).roundToInt())
    val previewEnd = previewStart.plusMinutes(task.estimatedDurationMinutes.toLong())

    Surface(
        color = background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TimeColumnWidth + 10.dp, end = 14.dp, bottom = 5.dp)
            .offset { IntOffset(0, animatedBaseY.roundToPx()) }
            .height(taskHeight)
            .zIndex(if (dragging) 3f else 1f)
            .then(if (dragging) Modifier.shadow(10.dp, RoundedCornerShape(18.dp)) else Modifier)
            .border(
                1.dp,
                if (task.status == TaskStatus.IN_PROGRESS) MaterialTheme.colorScheme.primary.copy(alpha = .28f) else categoryColor.copy(alpha = .12f),
                RoundedCornerShape(18.dp),
            )
            .alpha(if (task.status == TaskStatus.COMPLETED) .62f else 1f)
            .offset { IntOffset(0, dragOffset.roundToInt()) }
            .pointerInput(task.id, start) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDragCancel = { dragOffset = 0f; dragging = false },
                    onDragEnd = {
                        val deltaMinutes = (dragOffset / hourPx * 60f).roundToInt()
                        onReschedule(snappedTime(start, deltaMinutes))
                        dragOffset = 0f
                        dragging = false
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        dragOffset += amount.y
                        val snap = (dragOffset / hourPx * 4).roundToInt()
                        if (snap != lastSnap) {
                            lastSnap = snap
                            if (hapticsEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                )
            }
            .clickable(onClick = onClick),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.width(5.dp).height(taskHeight).background(categoryColor))
            Column(Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 9.dp)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.status == TaskStatus.COMPLETED) TextDecoration.LineThrough else null,
                    maxLines = 2,
                )
                if (taskHeight >= 64.dp) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (dragging) "$previewStart — $previewEnd" else "${task.category ?: "Unsorted"} · ${durationLabel(task.estimatedDurationMinutes)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
            if (task.status == TaskStatus.COMPLETED) {
                Icon(Icons.Outlined.Check, "Completed", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 10.dp, end = 10.dp).size(18.dp))
            } else {
                Icon(
                    Icons.Outlined.CheckCircle,
                    "Mark complete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .55f),
                    modifier = Modifier.padding(top = 10.dp, end = 4.dp).size(18.dp).clickable(onClick = onToggle),
                )
            }
        }
    }
}

@Composable
private fun categoryColor(category: String?): Color = when (category?.lowercase()) {
    "work" -> OrbitWork
    "study", "learning" -> OrbitStudy
    "fitness" -> OrbitFitness
    else -> OrbitPersonal
}

fun durationLabel(minutes: Int): String = when {
    minutes < 60 -> "${minutes}m"
    minutes % 60 == 0 -> "${minutes / 60}h"
    else -> "${minutes / 60}h ${minutes % 60}m"
}
