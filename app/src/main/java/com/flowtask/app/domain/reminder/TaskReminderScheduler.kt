package com.flowtask.app.domain.reminder

import com.flowtask.app.domain.model.Task

interface TaskReminderScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: Long)
    fun replaceAll(tasks: List<Task>)
    fun cancelAll()
}
