package com.flowtask.app.data.repository

import androidx.room.withTransaction
import com.flowtask.app.data.local.FlowTaskDatabase
import com.flowtask.app.data.local.entity.RoutineEntity
import com.flowtask.app.data.local.entity.RoutineItemEntity
import com.flowtask.app.domain.model.Routine
import com.flowtask.app.domain.model.RoutineItem
import com.flowtask.app.domain.repository.RoutineRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class OfflineRoutineRepository @Inject constructor(
    private val database: FlowTaskDatabase,
) : RoutineRepository {
    override fun observeRoutines(): Flow<List<Routine>> = database.routineDao().observeAll().map { rows ->
        rows.map { row ->
            Routine(
                id = row.routine.id,
                name = row.routine.name,
                repeatDays = row.routine.repeatDays,
                reminderTime = row.routine.reminderTime,
                enabled = row.routine.enabled,
                items = row.items.sortedBy { it.position }.map {
                    RoutineItem(it.id, it.title, it.position)
                },
            )
        }
    }

    override suspend fun saveRoutine(routine: Routine): Long = database.withTransaction {
        require(routine.name.isNotBlank()) { "Routine name cannot be blank" }
        val generatedId = database.routineDao().upsertRoutine(
            RoutineEntity(
                id = routine.id,
                name = routine.name.trim(),
                repeatDays = routine.repeatDays,
                reminderTime = routine.reminderTime,
                enabled = routine.enabled,
            ),
        )
        val routineId = routine.id.takeIf { it != 0L } ?: generatedId
        database.routineDao().deleteItems(routineId)
        database.routineDao().upsertItems(
            routine.items.mapIndexed { index, item ->
                RoutineItemEntity(item.id, routineId, item.title.trim(), index)
            },
        )
        routineId
    }

    override suspend fun deleteRoutine(id: Long) = database.withTransaction {
        database.routineDao().deleteItems(id)
        database.routineDao().deleteById(id)
    }
}
