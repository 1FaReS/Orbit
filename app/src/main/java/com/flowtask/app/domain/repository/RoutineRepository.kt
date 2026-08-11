package com.flowtask.app.domain.repository

import com.flowtask.app.domain.model.Routine
import kotlinx.coroutines.flow.Flow

interface RoutineRepository {
    fun observeRoutines(): Flow<List<Routine>>
    suspend fun saveRoutine(routine: Routine): Long
    suspend fun deleteRoutine(id: Long)
}
