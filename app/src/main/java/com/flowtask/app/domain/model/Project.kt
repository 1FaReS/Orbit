package com.flowtask.app.domain.model

import java.time.Instant
import java.time.LocalDate

data class Project(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val groupName: String = "Work",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val colorId: String = "violet",
    val icon: String = "work",
    val createdAt: Instant = Instant.now(),
)

data class ProjectSummary(
    val project: Project,
    val taskCount: Int,
    val completedCount: Int,
) {
    val progress: Float get() = if (taskCount == 0) 0f else completedCount.toFloat() / taskCount
}
