package com.flowtask.app.domain.repository

import com.flowtask.app.domain.model.FocusSession
import kotlinx.coroutines.flow.Flow

interface FocusSessionRepository {
    fun observeSessions(): Flow<List<FocusSession>>
    fun observeActiveSession(): Flow<FocusSession?>
    suspend fun saveSession(session: FocusSession): Long
    suspend fun deleteSession(id: Long)
}
