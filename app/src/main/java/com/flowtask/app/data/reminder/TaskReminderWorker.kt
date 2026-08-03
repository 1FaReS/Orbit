package com.flowtask.app.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.flowtask.app.MainActivity
import com.flowtask.app.R

class TaskReminderWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        if (!notificationsAllowed()) return Result.success()

        createChannel()
        val taskId = inputData.getLong(TaskIdKey, 0)
        val taskTitle = inputData.getString(TaskTitleKey).orEmpty()
        val projectName = inputData.getString(ProjectNameKey)
        val launchIntent = Intent(applicationContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            applicationContext,
            taskId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, ChannelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(taskTitle.ifBlank { "Task due now" })
            .setContentText(projectName?.let { "Due now · $it" } ?: "Due now")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext).notify(taskId.hashCode(), notification)
        } catch (_: SecurityException) {
            return Result.success()
        }
        return Result.success()
    }

    private fun notificationsAllowed(): Boolean {
        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    private fun createChannel() {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(ChannelId, "Task reminders", NotificationManager.IMPORTANCE_HIGH),
        )
    }

    companion object {
        const val TaskIdKey = "task_id"
        const val TaskTitleKey = "task_title"
        const val ProjectNameKey = "project_name"
        private const val ChannelId = "task_reminders"
    }
}
