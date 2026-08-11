package com.flowtask.app.presentation.taskmanager

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flowtask.app.core.designsystem.components.OrbitGradientButton
import com.flowtask.app.domain.model.Priority
import com.flowtask.app.domain.model.Project
import com.flowtask.app.domain.model.RecurrenceFrequency
import com.flowtask.app.domain.model.RecurrenceRule
import com.flowtask.app.domain.model.ReminderSettings
import com.flowtask.app.domain.model.Subtask
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ProjectEditorScreen(
    initial: Project?,
    onBack: () -> Unit,
    onSave: (Project) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf(initial?.name.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var group by rememberSaveable { mutableStateOf(initial?.groupName ?: "Work") }
    var startDate by rememberSaveable { mutableStateOf(initial?.startDate ?: LocalDate.now()) }
    var endDate by rememberSaveable { mutableStateOf(initial?.endDate ?: LocalDate.now().plusMonths(1)) }
    var colorId by rememberSaveable { mutableStateOf(initial?.colorId ?: "pink") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    EditorScaffold(title = if (initial == null) "Add Project" else "Edit Project", onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item { FormLabel("Task Group") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Work", "Personal", "Learning").forEach { value ->
                        FilterChip(selected = group == value, onClick = { group = value }, label = { Text(value) })
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    shape = RoundedCornerShape(14.dp),
                )
            }
            item {
                DateField("Start Date", startDate) {
                    showDatePicker(context, startDate) { startDate = it }
                }
            }
            item {
                DateField("End Date", endDate) {
                    showDatePicker(context, endDate) { endDate = it }
                }
            }
            item { FormLabel("Project Color") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("pink", "violet", "orange", "blue").forEach { value ->
                        FilterChip(
                            selected = colorId == value,
                            onClick = { colorId = value },
                            label = { Text(value.replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                androidx.compose.foundation.layout.Box(Modifier.size(10.dp).background(projectAccent(value), CircleShape))
                            },
                        )
                    }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                PrimaryAction(if (initial == null) "Add Project" else "Save Changes") {
                    when {
                        name.isBlank() -> error = "Enter a project name"
                        endDate.isBefore(startDate) -> error = "End date cannot be before start date"
                        else -> onSave(
                            (initial ?: Project(name = name)).copy(
                                name = name.trim(),
                                description = description.trim(),
                                groupName = group,
                                startDate = startDate,
                                endDate = endDate,
                                colorId = colorId,
                                icon = when (group) { "Work" -> "briefcase"; "Learning" -> "book"; else -> "person" },
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskEditorScreen(
    initial: Task?,
    projects: List<Project>,
    preselectedProject: String?,
    onBack: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: ((Task) -> Unit)?,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var description by rememberSaveable { mutableStateOf(initial?.description.orEmpty()) }
    var projectName by rememberSaveable { mutableStateOf(initial?.category ?: preselectedProject ?: projects.firstOrNull()?.name.orEmpty()) }
    var date by rememberSaveable { mutableStateOf(initial?.dueDate ?: LocalDate.now()) }
    var time by rememberSaveable { mutableStateOf(initial?.dueTime ?: LocalTime.of(10, 0)) }
    var duration by rememberSaveable { mutableStateOf((initial?.estimatedDurationMinutes ?: 30).toString()) }
    var status by rememberSaveable { mutableStateOf(initial?.status ?: TaskStatus.TODO) }
    var priority by rememberSaveable { mutableStateOf(initial?.priority ?: Priority.MEDIUM) }
    var advancedVisible by rememberSaveable { mutableStateOf(initial != null && (initial.description.isNotBlank() || initial.subtasks.isNotEmpty())) }
    var subtasksText by rememberSaveable { mutableStateOf(initial?.subtasks?.joinToString("\n") { it.title }.orEmpty()) }
    var reminderEnabled by rememberSaveable { mutableStateOf(initial?.reminder?.enabled ?: false) }
    var repeatWeekly by rememberSaveable { mutableStateOf(initial?.recurrenceRule?.frequency == RecurrenceFrequency.WEEKLY) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    EditorScaffold(title = if (initial == null) "Add Task" else "Edit Task", onBack = onBack) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Task Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            item { FormLabel("Project") }
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    projects.forEach { project ->
                        FilterChip(
                            selected = projectName == project.name,
                            onClick = { projectName = project.name },
                            label = { Text(project.name) },
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField("Date", date, Modifier.weight(1f)) {
                        showDatePicker(context, date) { date = it }
                    }
                    OutlinedButton(
                        onClick = { showTimePicker(context, time) { time = it } },
                        modifier = Modifier.weight(1f).height(58.dp),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Outlined.Schedule, null)
                        Spacer(Modifier.padding(3.dp))
                        Text(time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)))
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                )
            }
            if (initial != null) {
                item { FormLabel("Status") }
                item {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(TaskStatus.TODO, TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED).forEach { value ->
                            FilterChip(
                                selected = status == value,
                                onClick = { status = value },
                                label = { Text(value.displayName()) },
                            )
                        }
                    }
                }
            }
            item { FormLabel("Priority") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Priority.LOW, Priority.MEDIUM, Priority.HIGH).forEach { value ->
                        FilterChip(
                            selected = priority == value,
                            onClick = { priority = value },
                            label = { Text(value.displayName()) },
                        )
                    }
                }
            }
            item {
                OutlinedButton(onClick = { advancedVisible = !advancedVisible }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                    Text(if (advancedVisible) "Hide details" else "Notes, subtasks & repeat")
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Outlined.ExpandMore, null)
                }
            }
            item {
                AnimatedVisibility(advancedVisible) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            label = { Text("Notes") },
                            shape = MaterialTheme.shapes.medium,
                        )
                        OutlinedTextField(
                            value = subtasksText,
                            onValueChange = { subtasksText = it },
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            label = { Text("Subtasks, one per line") },
                            shape = MaterialTheme.shapes.medium,
                        )
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Reminder"); Text("10 minutes before", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Switch(reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text("Repeat weekly"); Text("On this weekday", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            Switch(repeatWeekly, onCheckedChange = { repeatWeekly = it })
                        }
                    }
                }
            }
            error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
            item {
                PrimaryAction(if (initial == null) "Add Task" else "Save Changes") {
                    when {
                        title.isBlank() -> error = "Enter a task name"
                        projectName.isBlank() -> error = "Create or select a project"
                        duration.toIntOrNull() == null || duration.toInt() <= 0 -> error = "Enter a valid duration"
                        else -> onSave(
                            (initial ?: Task(title = title)).copy(
                                title = title.trim(), description = description.trim(), category = projectName,
                                dueDate = date, dueTime = time, estimatedDurationMinutes = duration.toInt(),
                                status = status, priority = priority,
                                subtasks = subtasksText.lines().filter(String::isNotBlank).mapIndexed { index, value ->
                                    Subtask(parentTaskId = initial?.id ?: 0, title = value.trim(), position = index)
                                },
                                reminder = if (reminderEnabled) ReminderSettings(minutesBefore = 10) else null,
                                recurrenceRule = if (repeatWeekly) RecurrenceRule(RecurrenceFrequency.WEEKLY, daysOfWeek = setOf(date.dayOfWeek.value)) else null,
                            ),
                        )
                    }
                }
            }
            if (initial != null && onDelete != null) item {
                OutlinedButton(
                    onClick = { onDelete(initial) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(14.dp),
                ) { Icon(Icons.Outlined.Delete, null); Spacer(Modifier.padding(4.dp)); Text("Delete Task") }
            }
        }
    }
}

@Composable
private fun EditorScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back") }
                Column(Modifier.weight(1f)) {
                    Text("ORBIT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.size(48.dp))
            }
        },
        content = content,
    )
}

@Composable
private fun DateField(label: String, date: LocalDate, modifier: Modifier = Modifier, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(14.dp)) {
        Icon(Icons.Outlined.CalendarMonth, null)
        Spacer(Modifier.padding(4.dp))
        Column { Text(label, style = MaterialTheme.typography.labelSmall); Text(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))) }
    }
}

@Composable
private fun FormLabel(text: String) = Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)

@Composable
private fun PrimaryAction(text: String, onClick: () -> Unit) {
    OrbitGradientButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) { Text(text, fontWeight = FontWeight.SemiBold) }
}

private fun showDatePicker(context: android.content.Context, initial: LocalDate, onDate: (LocalDate) -> Unit) {
    DatePickerDialog(
        context,
        { _, year, month, day -> onDate(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth,
    ).show()
}

private fun showTimePicker(context: android.content.Context, initial: LocalTime, onTime: (LocalTime) -> Unit) {
    TimePickerDialog(context, { _, hour, minute -> onTime(LocalTime.of(hour, minute)) }, initial.hour, initial.minute, false).show()
}

private fun Enum<*>.displayName(): String = name
    .replace('_', ' ')
    .lowercase()
    .replaceFirstChar { it.uppercase() }
