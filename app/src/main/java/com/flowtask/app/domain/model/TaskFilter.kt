package com.flowtask.app.domain.model

import java.time.LocalDate

data class TaskFilter(
    val query: String = "",
    val statuses: Set<TaskStatus> = emptySet(),
    val priorities: Set<Priority> = emptySet(),
    val dueDate: LocalDate? = null,
    val category: String? = null,
    val tagIds: Set<Long> = emptySet(),
    val maxDurationMinutes: Int? = null,
    val overdueOnly: Boolean = false,
)
