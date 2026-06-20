package com.example.leveluplife.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object TaskReminderScheduler {

    private const val WORK_NAME = "task_reminders"
    private const val IMMEDIATE_WORK_NAME = "task_reminders_now"
    private const val INTERVAL_HOURS = 8L

    fun schedule(context: Context) {
        TaskNotifier.ensureChannel(context)

        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun runNow(context: Context) {
        TaskNotifier.ensureChannel(context)

        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setConstraints(networkConstraints())
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private fun networkConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
}
