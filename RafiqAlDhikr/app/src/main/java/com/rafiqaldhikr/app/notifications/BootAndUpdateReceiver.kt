package com.rafiqaldhikr.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rafiqaldhikr.app.RafiqApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootAndUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val validActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
        if (intent.action !in validActions) return

        val app = context.applicationContext as RafiqApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                app.container.worshipRepository.evaluateMissedActivitiesForAllProfiles()
                app.container.reminderScheduler.rescheduleAll()
                MissedActivityWorker.enqueuePeriodic(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
