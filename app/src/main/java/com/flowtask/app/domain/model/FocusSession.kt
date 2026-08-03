package com.flowtask.app.domain.model

import java.time.Instant

data class FocusSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Instant,
    val endedAt: Instant? = null,
    val durationMinutes: Int,
    val status: FocusSessionStatus = FocusSessionStatus.ACTIVE,
)

enum class FocusSessionStatus { ACTIVE, PAUSED, COMPLETED, CANCELLED }
