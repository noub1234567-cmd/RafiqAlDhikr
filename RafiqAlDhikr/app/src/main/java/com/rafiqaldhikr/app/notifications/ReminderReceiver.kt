package com.rafiqaldhikr.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as RafiqApplication
        val pendingResult = goAsync()

        val kind = intent.getStringExtra(NotificationHelper.EXTRA_KIND) ?: ReminderScheduler.KIND_ACTIVITY
        val targetId = intent.getLongExtra(NotificationHelper.EXTRA_TARGET_ID, -1L)
        val profileId = intent.getLongExtra(NotificationHelper.EXTRA_PROFILE_ID, -1L)
        val activityId = intent.getLongExtra(NotificationHelper.EXTRA_ACTIVITY_ID, -1L)
        val notificationId = intent.getIntExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, 1001)
        val title = intent.getStringExtra(NotificationHelper.EXTRA_TITLE) ?: "رفيقك في الذكر"
        val body = intent.getStringExtra(NotificationHelper.EXTRA_BODY) ?: "تذكير بوقت الذكر والعبادة"
        val soundStr = intent.getStringExtra(NotificationHelper.EXTRA_SOUND_MODE) ?: NotificationSoundMode.NORMAL.name
        val soundMode = runCatching { NotificationSoundMode.valueOf(soundStr) }
            .getOrDefault(NotificationSoundMode.NORMAL)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = app.container.database
                var resolvedLogId = -1L

                if (kind == ReminderScheduler.KIND_ACTIVITY && activityId > 0L) {
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val activity = db.worshipDao().getActivityById(activityId)
                    if (activity != null) {
                        val existingLog = db.worshipDao().getLogForActivityDay(profileId, activityId, todayEpoch)
                        if (existingLog != null) {
                            resolvedLogId = existingLog.id
                            // Do not spam if user already completed today's occurrence
                            if (existingLog.status == ActivityStatus.COMPLETED) {
                                app.container.reminderScheduler.scheduleActivityReminder(activity)
                                return@launch
                            }
                        } else {
                            resolvedLogId = db.worshipDao().insertLog(
                                ActivityLogEntity(
                                    profileId = profileId,
                                    activityId = activityId,
                                    activityTitleSnapshot = activity.title,
                                    categorySnapshot = activity.category,
                                    dateEpochDay = todayEpoch,
                                    scheduledAtMillis = System.currentTimeMillis(),
                                    status = ActivityStatus.PENDING
                                )
                            )
                        }
                        // Schedule the next recurring occurrence
                        app.container.reminderScheduler.scheduleActivityReminder(activity)
                    }
                } else if (kind == ReminderScheduler.KIND_RESCHEDULED && targetId > 0L) {
                    resolvedLogId = targetId
                    val log = db.worshipDao().getLogById(targetId)
                    if (log?.status == ActivityStatus.COMPLETED) {
                        return@launch
                    }
                } else {
                    // Reschedule recurring non-activity reminders for next day
                    app.container.reminderScheduler.rescheduleAll()
                }

                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    body = body,
                    soundMode = soundMode,
                    activityId = activityId,
                    logId = resolvedLogId,
                    profileId = profileId
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}
