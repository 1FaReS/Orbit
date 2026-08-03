package com.flowtask.app.data.repository

import androidx.room.withTransaction
import com.flowtask.app.data.local.FlowTaskDatabase
import com.flowtask.app.data.local.entity.TaskTagCrossRef
import com.flowtask.app.data.mapper.toDomain
import com.flowtask.app.data.mapper.toEntity
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskFilter
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.domain.repository.TaskRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@Singleton
class OfflineTaskRepository @Inject constructor(
    private val database: FlowTaskDatabase,
) : TaskRepository {
    private val taskDao = database.taskDao()
    private val tagDao = database.tagDao()

    override fun observeTasks(filter: TaskFilter): Flow<List<Task>> = taskDao.observeAll()
        .map { relations -> relations.map { it.toDomain() }.filter { it.matches(filter) } }
        .distinctUntilChanged()

    override fun observeTask(id: Long): Flow<Task?> = taskDao.observeById(id)
        .map { it?.toDomain() }
        .distinctUntilChanged()

    override suspend fun saveTask(task: Task): Long {
        require(task.title.isNotBlank()) { "Task title cannot be blank" }
        require(task.estimatedDurationMinutes > 0) { "Estimated duration must be positive" }
        val taskToSave = task.withConsistentCompletionTime()

        return database.withTransaction {
            val generatedId = taskDao.upsertTask(taskToSave.toEntity())
            val taskId = taskToSave.id.takeIf { it != 0L } ?: generatedId

            taskDao.deleteSubtasks(taskId)
            taskDao.deleteTaskTags(taskId)
            taskDao.insertSubtasks(taskToSave.subtasks.mapIndexed { index, subtask ->
                subtask.copy(position = index).toEntity(taskId)
            })
            val tagIds = taskToSave.tags.map { tag ->
                tag.id.takeIf { it != 0L } ?: tagDao.upsert(tag.toEntity())
            }
            taskDao.insertTaskTags(tagIds.map { TaskTagCrossRef(taskId, it) })
            taskId
        }
    }

    override suspend fun setCompleted(taskId: Long, completed: Boolean) {
        taskDao.setStatus(
            taskId = taskId,
            status = if (completed) TaskStatus.COMPLETED else TaskStatus.TODO,
            completedAt = if (completed) Instant.now() else null,
        )
    }

    override suspend fun deleteTask(taskId: Long) = taskDao.deleteById(taskId)
    override suspend fun clearCompleted() = taskDao.clearCompleted()
}

internal fun Task.withConsistentCompletionTime(now: Instant = Instant.now()): Task = copy(
    completedAt = when (status) {
        TaskStatus.COMPLETED -> completedAt ?: now
        else -> null
    },
)

private fun Task.matches(filter: TaskFilter): Boolean {
    val queryMatches = filter.query.isBlank() || title.contains(filter.query, ignoreCase = true) ||
        description.contains(filter.query, ignoreCase = true)
    val statusMatches = filter.statuses.isEmpty() || status in filter.statuses
    val priorityMatches = filter.priorities.isEmpty() || priority in filter.priorities
    val dateMatches = filter.dueDate == null || dueDate == filter.dueDate
    val categoryMatches = filter.category == null || category == filter.category
    val tagMatches = filter.tagIds.isEmpty() || tags.any { it.id in filter.tagIds }
    val durationMatches = filter.maxDurationMinutes == null ||
        estimatedDurationMinutes <= filter.maxDurationMinutes
    val overdueMatches = !filter.overdueOnly || (
        status != TaskStatus.COMPLETED && dueDate?.isBefore(LocalDate.now()) == true
    )
    return queryMatches && statusMatches && priorityMatches && dateMatches && categoryMatches &&
        tagMatches && durationMatches && overdueMatches
}
