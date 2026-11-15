package com.rafiqaldhikr.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.data.backup.AutoBackup
import java.util.concurrent.TimeUnit

class AutoBackupWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        if (!AutoBackup.isEnabled(applicationContext)) return Result.success()
        return try {
            val app = applicationContext as RafiqApplication
            AutoBackup.save(applicationContext, app.container.backupManager.buildBackupJson())
            AlarmDiagnostics.record(applicationContext, "نُسخت نسخة احتياطية تلقائية")
            Result.success()
        } catch (t: Throwable) {
            AlarmDiagnostics.record(applicationContext, "فشلت النسخة التلقائية: ${t.message}")
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "rafiq_auto_backup"

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
