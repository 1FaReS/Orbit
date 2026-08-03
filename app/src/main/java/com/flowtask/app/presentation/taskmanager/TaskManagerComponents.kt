package com.flowtask.app.presentation.taskmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ProjectSummary
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val AppPrimary = Color(0xFF5F2EEA)
val AppPink = Color(0xFFFF6B9D)
val AppOrange = Color(0xFFFF8A3D)
val AppBlue = Color(0xFF58B7FF)
val AppGreen = Color(0xFF3CBF8A)
fun projectAccent(colorId: String): Color = when (colorId) {
    "pink" -> AppPink
    "orange" -> AppOrange
    "blue" -> AppBlue
    else -> AppPrimary
}

fun projectIcon(project: Project): ImageVector = when (project.icon) {
    "briefcase" -> Icons.Outlined.BusinessCenter
    "book" -> Icons.Outlined.Book
    else -> Icons.Outlined.Person
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val cardModifier = if (onClick == null) modifier else modifier.clickable(onClick = onClick)
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) { content() }
}

@Composable
fun ProjectSummaryRow(summary: ProjectSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = projectAccent(summary.project.colorId)
    SoftCard(modifier.fillMaxWidth(), onClick) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(42.dp).background(accent.copy(alpha = .14f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(projectIcon(summary.project), null, tint = accent, modifier = Modifier.size(21.dp))
            }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(summary.project.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    summary.taskCount.taskCountLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { summary.progress },
                    modifier = Modifier.matchParentSize(),
                    color = accent,
                    trackColor = accent.copy(alpha = .15f),
                    strokeWidth = 3.dp,
                )
                Text("${(summary.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun Int.taskCountLabel(): String = "$this ${if (this == 1) "task" else "tasks"}"

@Composable
fun TaskRow(
    task: Task,
    project: Project?,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = projectAccent(project?.colorId ?: "violet")
    SoftCard(modifier.fillMaxWidth(), onClick) {
        Row(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier.size(40.dp).background(accent.copy(alpha = .13f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) { Icon(project?.let(::projectIcon) ?: Icons.Outlined.Book, null, tint = accent, modifier = Modifier.size(20.dp)) }
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(
                    task.category ?: "General",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    task.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                task.dueTime?.let {
                    Text(
                        it.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            val statusColor = when (task.status) {
                TaskStatus.COMPLETED -> AppGreen
                TaskStatus.IN_PROGRESS -> AppOrange
                else -> AppBlue
            }
            Surface(
                onClick = onToggle,
                shape = CircleShape,
                color = statusColor.copy(alpha = .12f),
            ) {
                Text(
                    when (task.status) {
                        TaskStatus.COMPLETED -> "Done"
                        TaskStatus.IN_PROGRESS -> "In progress"
                        else -> "To do"
                    },
                    Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                    color = statusColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
