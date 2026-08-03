package com.flowtask.app.presentation.taskmanager

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.flowtask.app.R
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ProjectSummary
import com.flowtask.app.domain.model.Task

@Composable
fun WelcomeScreen(onStart: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(.25f))
        Image(
            painterResource(R.drawable.onboarding_illustration),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(30.dp)),
            contentScale = ContentScale.Fit,
        )
        Spacer(Modifier.height(20.dp))
        Text(
            "Task Management &\nTo-Do List",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This productive tool is designed to help you better manage your tasks project-wise conveniently!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(28.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppPrimary),
        ) {
            Text("Let’s Start", fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null)
        }
        Spacer(Modifier.weight(.15f))
    }
}

@Composable
fun HomeScreen(
    tasks: List<Task>,
    projectSummaries: List<ProjectSummary>,
    progress: Float,
    onViewToday: () -> Unit,
    onNotifications: () -> Unit,
    onAddProject: () -> Unit,
    onProjectClick: (Project) -> Unit,
    onTaskClick: (Task) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inProgress = tasks.filter { it.status == com.flowtask.app.domain.model.TaskStatus.IN_PROGRESS }
    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).background(AppPrimary.copy(alpha = .14f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text("LV", color = AppPrimary, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text("Hello!", style = MaterialTheme.typography.bodySmall)
                    Text("Livia Vaccaro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onNotifications) { Icon(Icons.Outlined.Notifications, "Notifications") }
            }
        }
        item {
            Box(
                Modifier.fillMaxWidth().background(
                    Brush.linearGradient(listOf(Color(0xFF6D38EF), Color(0xFF5125D6))),
                    RoundedCornerShape(20.dp),
                ).padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Your today’s task\nalmost done!", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = onViewToday,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = AppPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 15.dp, vertical = 8.dp),
                        ) { Text("View Task", style = MaterialTheme.typography.labelLarge) }
                    }
                    Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.matchParentSize(),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = .25f),
                            strokeWidth = 6.dp,
                        )
                        Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            SectionTitle("In Progress", count = inProgress.size)
        }
        item {
            if (inProgress.isEmpty()) {
                Text("No tasks in progress", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(inProgress, key = { it.id }) { task ->
                        val project = projectSummaries.firstOrNull { it.project.name == task.category }?.project
                        val accent = projectAccent(project?.colorId ?: "violet")
                        Surface(
                            onClick = { onTaskClick(task) },
                            modifier = Modifier.width(165.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = accent.copy(alpha = .1f),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(task.category ?: "General", style = MaterialTheme.typography.labelSmall)
                                Text(task.title, style = MaterialTheme.typography.titleSmall, minLines = 2, maxLines = 2)
                                LinearProgressIndicator(
                                    progress = { .62f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = accent,
                                    trackColor = accent.copy(alpha = .15f),
                                )
                            }
                        }
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Task Groups", count = projectSummaries.size, modifier = Modifier.weight(1f))
                FilledIconButton(
                    onClick = onAddProject,
                    modifier = Modifier.size(36.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(containerColor = AppPrimary),
                ) { Icon(Icons.Outlined.Add, "Add project", tint = Color.White) }
            }
        }
        items(projectSummaries, key = { it.project.id }) { summary ->
            ProjectSummaryRow(summary, onClick = { onProjectClick(summary.project) })
        }
    }
}

@Composable
private fun SectionTitle(text: String, count: Int, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(7.dp))
        Surface(shape = CircleShape, color = AppPrimary.copy(alpha = .12f)) {
            Text(count.toString(), Modifier.padding(horizontal = 7.dp, vertical = 2.dp), color = AppPrimary)
        }
    }
}
