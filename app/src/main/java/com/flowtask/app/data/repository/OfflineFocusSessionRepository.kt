package com.flowtask.app.data.repository

import com.flowtask.app.data.local.dao.FocusSessionDao
import com.flowtask.app.data.mapper.toDomain
import com.flowtask.app.data.mapper.toEntity
import com.flowtask.app.domain.model.FocusSession
import com.flowtask.app.domain.repository.FocusSessionRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineFocusSessionRepository @Inject constructor(
    private val dao: FocusSessionDao,
) : FocusSessionRepository {
    override fun observeSessions(): Flow<List<FocusSession>> = dao.observeAll()
        .map { sessions -> sessions.map { it.toDomain() } }

    override fun observeActiveSession(): Flow<FocusSession?> = dao.observeActive().map { it?.toDomain() }
    override suspend fun saveSession(session: FocusSession): Long = dao.upsert(session.toEntity())
    override suspend fun deleteSession(id: Long) = dao.deleteById(id)
}
