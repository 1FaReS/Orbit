package com.flowtask.app.domain.model

data class Subtask(
    val id: Long = 0,
    val parentTaskId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val position: Int = 0,
)
