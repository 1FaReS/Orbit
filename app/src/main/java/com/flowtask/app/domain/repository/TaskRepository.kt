package com.flowtask.app.domain.repository

import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskFilter
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(filter: TaskFilter = TaskFilter()): Flow<List<Task>>
    fun observeTask(id: Long): Flow<Task?>
    suspend fun saveTask(task: Task): Long
    suspend fun setCompleted(taskId: Long, completed: Boolean)
    suspend fun deleteTask(taskId: Long)
    suspend fun clearCompleted()
}
