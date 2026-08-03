package com.flowtask.app.data.mapper

import com.flowtask.app.data.local.entity.FocusSessionEntity
import com.flowtask.app.domain.model.FocusSession

fun FocusSessionEntity.toDomain(): FocusSession = FocusSession(
    id, taskId, startedAt, endedAt, durationMinutes, status,
)

fun FocusSession.toEntity(): FocusSessionEntity = FocusSessionEntity(
    id, taskId, startedAt, endedAt, durationMinutes, status,
)
