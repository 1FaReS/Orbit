package com.flowtask.app.feature.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.flowtask.app.core.designsystem.components.OrbitBrandHeader
import com.flowtask.app.core.designsystem.components.OrbitGradientButton
import com.flowtask.app.domain.model.Routine
import com.flowtask.app.domain.model.RoutineItem
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RoutinesScreen(
    routines: List<Routine>,
    onBack: () -> Unit,
    onSave: (Routine) -> Unit,
    onDelete: (Routine) -> Unit,
) {
    var editorOpen by remember { mutableStateOf(false) }
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 40.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { editorOpen = true }) { Icon(Icons.Outlined.Add, "Add routine") }
            }
            OrbitBrandHeader(
                eyebrow = "Orbit routines",
                title = "Repeat what matters.",
                subtitle = "Build a little structure for the parts of your day that return.",
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        items(routines, key = { it.id }) { routine ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(routine.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "${routine.repeatDays.sorted().joinToString(" ") { it.name.take(1) }} · ${routine.reminderTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "No reminder"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                        }
                        Switch(routine.enabled, onCheckedChange = { onSave(routine.copy(enabled = it)) })
                    }
                    routine.items.forEachIndexed { index, item ->
                        Text("${index + 1}.  ${item.title}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 9.dp))
                    }
                    TextButton(onClick = { onDelete(routine) }, modifier = Modifier.align(Alignment.End)) {
                        Icon(Icons.Outlined.DeleteOutline, null)
                        Text("Remove", Modifier.padding(start = 5.dp))
                    }
                }
            }
        }
    }
    if (editorOpen) RoutineEditor(onDismiss = { editorOpen = false }) { onSave(it); editorOpen = false }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun RoutineEditor(onDismiss: () -> Unit, onSave: (Routine) -> Unit) {
    var name by remember { mutableStateOf("") }
    var items by remember { mutableStateOf("") }
    var days by remember { mutableStateOf(setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("New routine", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium)
            OutlinedTextField(items, { items = it }, label = { Text("Steps, one per line") }, modifier = Modifier.fillMaxWidth(), minLines = 4, shape = MaterialTheme.shapes.medium)
            Text("Repeat", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = day in days,
                        onClick = { days = if (day in days) days - day else days + day },
                        label = { Text(day.name.take(2)) },
                    )
                }
            }
            OrbitGradientButton(
                onClick = {
                    onSave(
                        Routine(
                            name = name.trim(),
                            items = items.lines().filter(String::isNotBlank).mapIndexed { index, value -> RoutineItem(title = value.trim(), position = index) },
                            repeatDays = days,
                            reminderTime = LocalTime.of(8, 0),
                        ),
                    )
                },
                enabled = name.isNotBlank() && items.lines().any(String::isNotBlank),
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Save routine") }
        }
    }
}
