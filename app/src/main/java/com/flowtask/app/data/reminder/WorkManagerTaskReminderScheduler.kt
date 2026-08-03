package com.flowtask.app.data.reminder

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.flowtask.app.domain.model.Task
import com.flowtask.app.domain.model.TaskStatus
import com.flowtask.app.domain.reminder.TaskReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerTaskReminderScheduler @Inject constructor(
    @ApplicationContext context: Context,
) : TaskReminderScheduler {
    private val workManager = WorkManager.getInstance(context)

    override fun schedule(task: Task) {
        cancel(task.id)
        val delay = task.reminderDelayMillis() ?: return
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong(TaskReminderWorker.TaskIdKey, task.id)
                    .putString(TaskReminderWorker.TaskTitleKey, task.title)
                    .putString(TaskReminderWorker.ProjectNameKey, task.category)
                    .build(),
            )
            .addTag(TaskRemindersTag)
            .build()

        workManager.enqueueUniqueWork(workName(task.id), ExistingWorkPolicy.REPLACE, request)
    }

    override fun cancel(taskId: Long) {
        if (taskId > 0) workManager.cancelUniqueWork(workName(taskId))
    }

    override fun replaceAll(tasks: List<Task>) {
        tasks.forEach(::schedule)
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag(TaskRemindersTag)
    }

    private fun workName(taskId: Long) = "task-reminder-$taskId"

    private companion object {
        const val TaskRemindersTag = "task-reminders"
    }
}

internal fun Task.reminderDelayMillis(
    now: Instant = Instant.now(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): Long? {
    if (id <= 0 || status == TaskStatus.COMPLETED) return null
    val date = dueDate ?: return null
    val time = dueTime ?: return null
    val scheduledAt = date.atTime(time).atZone(zoneId).toInstant()
    return Duration.between(now, scheduledAt).toMillis().takeIf { it > 0 }
}
