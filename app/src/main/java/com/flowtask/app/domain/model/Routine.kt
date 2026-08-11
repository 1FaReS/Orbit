package com.flowtask.app.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

data class Routine(
    val id: Long = 0,
    val name: String,
    val items: List<RoutineItem> = emptyList(),
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val reminderTime: LocalTime? = null,
    val enabled: Boolean = true,
)

data class RoutineItem(
    val id: Long = 0,
    val title: String,
    val position: Int = 0,
)
