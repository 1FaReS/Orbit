package com.flowtask.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.flowtask.app.data.local.entity.SubtaskEntity
import com.flowtask.app.data.local.entity.TaskEntity
import com.flowtask.app.data.local.entity.TaskTagCrossRef
import com.flowtask.app.data.local.entity.TaskWithRelations
import com.flowtask.app.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TaskDao {
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY completedAt IS NOT NULL, dueDate IS NULL, dueDate, dueTime, createdAt DESC")
    fun observeAll(): Flow<List<TaskWithRelations>>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskWithRelations?>

    @Upsert
    suspend fun upsertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<SubtaskEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTaskTags(crossRefs: List<TaskTagCrossRef>)

    @Query("DELETE FROM subtasks WHERE parentTaskId = :taskId")
    suspend fun deleteSubtasks(taskId: Long)

    @Query("DELETE FROM task_tags WHERE taskId = :taskId")
    suspend fun deleteTaskTags(taskId: Long)

    @Query("UPDATE tasks SET status = :status, completedAt = :completedAt WHERE id = :taskId")
    suspend fun setStatus(taskId: Long, status: TaskStatus, completedAt: Instant?)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("DELETE FROM tasks WHERE status = 'COMPLETED'")
    suspend fun clearCompleted()
}
