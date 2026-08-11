package com.flowtask.app.feature.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitGradientButton
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.ReminderSettings
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.parser.NaturalLanguageTaskParser
import com.flowtask.app.feature.timeline.durationLabel
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun QuickAddSheet(
    projects: List<Project>,
    defaultDurationMinutes: Int,
    defaultReminderMinutes: Int,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onOpenFullEditor: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val parser = remember { NaturalLanguageTaskParser() }
    val parsed = remember(text) { parser.parse(text) }
    val defaultProject = projects.firstOrNull()?.name

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp).animateContentSize()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(34.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.AutoAwesome, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text("Quick add", style = MaterialTheme.typography.titleLarge)
                        Text("Type it the way you would say it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = .72f))
                    }
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Gym tomorrow at 6pm for one hour") },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                minLines = 2,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                ),
            )
            AnimatedVisibility(text.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("I’LL ADD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(parsed.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 4.dp))
                        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            PreviewValue(Icons.Outlined.CalendarMonth, parsed.date.format(DateTimeFormatter.ofPattern("EEE, MMM d")))
                            PreviewValue(Icons.Outlined.Schedule, parsed.time.format(DateTimeFormatter.ofPattern("HH:mm")))
                            PreviewValue(Icons.Outlined.Timer, durationLabel(if (parsed.durationMinutes == 45) defaultDurationMinutes else parsed.durationMinutes))
                        }
                        if (!parsed.recognizedDate || !parsed.recognizedTime) {
                            Text(
                                "${if (!parsed.recognizedDate) "Today" else "Date found"} · ${if (!parsed.recognizedTime) "09:00 assumed" else "Time found"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = .72f),
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(top = 24.dp))
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onOpenFullEditor) { Text("More options") }
                Spacer(Modifier.weight(1f))
                OrbitGradientButton(
                    enabled = text.isNotBlank(),
                    onClick = {
                        onSave(
                            Task(
                                title = parsed.title,
                                dueDate = parsed.date,
                                dueTime = parsed.time,
                                estimatedDurationMinutes = if (parsed.durationMinutes == 45) defaultDurationMinutes else parsed.durationMinutes,
                                category = defaultProject,
                                reminder = ReminderSettings(minutesBefore = defaultReminderMinutes),
                            ),
                        )
                    },
                ) { Text("Add to timeline") }
            }
        }
    }
}

@Composable
private fun PreviewValue(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 5.dp))
    }
}
