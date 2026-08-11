package com.flowtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.flowtask.app.data.local.entity.RoutineEntity
import com.flowtask.app.data.local.entity.RoutineItemEntity
import com.flowtask.app.data.local.entity.RoutineWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Transaction
    @Query("SELECT * FROM routines ORDER BY name")
    fun observeAll(): Flow<List<RoutineWithItems>>

    @Upsert
    suspend fun upsertRoutine(routine: RoutineEntity): Long

    @Upsert
    suspend fun upsertItems(items: List<RoutineItemEntity>)

    @Query("DELETE FROM routine_items WHERE routineId = :routineId")
    suspend fun deleteItems(routineId: Long)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteById(id: Long)
}
