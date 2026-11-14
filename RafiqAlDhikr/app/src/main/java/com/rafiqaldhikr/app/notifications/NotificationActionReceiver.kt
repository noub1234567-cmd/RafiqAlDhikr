package com.rafiqaldhikr.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as RafiqApplication
        val action = intent.action ?: return
        val pendingResult = goAsync()

        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, -1)
        val logId = intent.getLongExtra(NotificationHelper.EXTRA_LOG_ID, -1L)
        val activityId = intent.getLongExtra(NotificationHelper.EXTRA_ACTIVITY_ID, -1L)
        val profileId = intent.getLongExtra(NotificationHelper.EXTRA_PROFILE_ID, -1L)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "تذكير عبادة"
        val body = intent.getStringExtra(NotificationHelper.EXTRA_BODY) ?: "حان وقت العبادة"
        val soundStr = intent.getStringExtra(NotificationHelper.EXTRA_SOUND_MODE) ?: NotificationSoundMode.NORMAL.name
        val soundMode = runCatching { NotificationSoundMode.valueOf(soundStr) }
            .getOrDefault(NotificationSoundMode.NORMAL)

        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = app.container.database.worshipDao()
                when (action) {
                    NotificationHelper.ACTION_COMPLETE -> {
                        if (logId > 0L) {
                            val existing = dao.getLogById(logId)
                            if (existing != null) {
                                dao.updateLog(
                                    MissedActivityEvaluator.markExplicitlyCompleted(
                                        existing,
                                        System.currentTimeMillis()
                                    )
                                )
                            }
                        }
                    }

                    NotificationHelper.ACTION_SNOOZE_15,
                    NotificationHelper.ACTION_REMIND_60 -> {
                        val delayMinutes = if (action == NotificationHelper.ACTION_SNOOZE_15) 15L else 60L
                        val newTrigger = System.currentTimeMillis() + delayMinutes * 60_000L

                        if (logId > 0L) {
                            val existing = dao.getLogById(logId)
                            if (existing != null && existing.status != ActivityStatus.COMPLETED) {
                                // Strictly reschedule WITHOUT completing!
                                dao.updateLog(
                                    MissedActivityEvaluator.rescheduleWithoutCompleting(existing, newTrigger)
                                )
                            }
                        }

                        app.container.reminderScheduler.scheduleOneOffRescheduledReminder(
                            logId = if (logId > 0L) logId else notificationId.toLong(),
                            activityId = activityId,
                            profileId = profileId,
                            title = title,
                            body = body,
                            triggerAtMillis = newTrigger,
                            soundMode = soundMode
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
