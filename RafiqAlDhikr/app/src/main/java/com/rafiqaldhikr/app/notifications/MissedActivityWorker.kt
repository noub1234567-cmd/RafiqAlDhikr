package com.rafiqaldhikr.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.rafiqaldhikr.app.RafiqApplication
import java.util.concurrent.TimeUnit

class MissedActivityWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as RafiqApplication
        app.container.worshipRepository.evaluateMissedActivitiesForAllProfiles()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "rafiq_missed_activity_evaluator"

        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<MissedActivityWorker>(1, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
