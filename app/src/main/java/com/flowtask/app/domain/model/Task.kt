package com.flowtask.app.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val estimatedDurationMinutes: Int = 25,
    val actualDurationMinutes: Int = 0,
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val importance: Importance = Importance.MEDIUM,
    val createdAt: Instant = Instant.now(),
    val completedAt: Instant? = null,
    val recurrenceRule: RecurrenceRule? = null,
    val category: String? = null,
    val tags: List<Tag> = emptyList(),
    val subtasks: List<Subtask> = emptyList(),
    val reminder: ReminderSettings? = null,
    val postponementCount: Int = 0,
    val scheduledStart: Instant? = null,
)

enum class TaskStatus { TODO, IN_PROGRESS, COMPLETED, ARCHIVED }
enum class Priority { LOW, MEDIUM, HIGH, URGENT }
enum class EnergyLevel { LOW, MEDIUM, HIGH }
enum class Importance { LOW, MEDIUM, HIGH }

data class RecurrenceRule(
    val frequency: RecurrenceFrequency,
    val interval: Int = 1,
    val daysOfWeek: Set<Int> = emptySet(),
    val endDate: LocalDate? = null,
)

enum class RecurrenceFrequency { DAILY, WEEKLY, MONTHLY, YEARLY }

data class ReminderSettings(
    val enabled: Boolean = true,
    val minutesBefore: Int = 15,
)
