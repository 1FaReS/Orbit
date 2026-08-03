package com.flowtask.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.flowtask.app.domain.model.EnergyLevel
import com.flowtask.app.domain.model.Importance
import com.flowtask.app.domain.model.Priority
import com.flowtask.app.domain.model.RecurrenceFrequency
import com.flowtask.app.domain.model.TaskStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "tasks",
    indices = [
        Index("status"),
        Index("dueDate"),
        Index("scheduledStart"),
        Index("category"),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: Priority,
    val dueDate: LocalDate?,
    val dueTime: LocalTime?,
    val estimatedDurationMinutes: Int,
    val actualDurationMinutes: Int,
    val energyLevel: EnergyLevel,
    val importance: Importance,
    val createdAt: Instant,
    val completedAt: Instant?,
    val recurrenceFrequency: RecurrenceFrequency?,
    val recurrenceInterval: Int?,
    val recurrenceDaysOfWeek: Set<Int>,
    val recurrenceEndDate: LocalDate?,
    val category: String?,
    val reminderEnabled: Boolean,
    val reminderMinutesBefore: Int?,
    val postponementCount: Int,
    val scheduledStart: Instant?,
)
