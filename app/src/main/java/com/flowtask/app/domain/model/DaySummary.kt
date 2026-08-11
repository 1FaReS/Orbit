package com.flowtask.app.domain.model

import java.time.LocalDate

data class DaySummary(
    val date: LocalDate,
    val completedTasks: Int,
    val totalTasks: Int,
    val focusedMinutes: Int,
    val reflection: String = "",
)
