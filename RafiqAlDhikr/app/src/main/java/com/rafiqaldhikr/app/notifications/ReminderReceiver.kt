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

        AlarmDiagnostics.record(
            context,
            "وصل منبّه [$kind#$targetId] «$title» — الإشعارات مفعّلة: " +
                androidx.core.app.NotificationManagerCompat.from(context).areNotificationsEnabled()
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = app.container.database
                var resolvedLogId = -1L
                var resolvedActivityId = activityId

                // Stale alarms (deleted profile/item, restored backup, disabled reminder) must stay silent.
                if (profileId > 0L && db.profileDao().getById(profileId) == null) {
                    AlarmDiagnostics.record(context, "تُجوهل [$kind#$targetId]: الملف الشخصي غير موجود")
                    return@launch
                }
                if (!isTargetStillActive(app, kind, targetId, profileId, activityId)) {
                    AlarmDiagnostics.record(context, "تُجوهل [$kind#$targetId]: العنصر محذوف أو التذكير معطّل")
                    return@launch
                }

                if (kind == ReminderScheduler.KIND_ACTIVITY && activityId > 0L) {
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val activity = db.worshipDao().getActivityById(activityId)
                    if (activity != null) {
                        val existingLog = db.worshipDao().getLogForActivityDay(profileId, activityId, todayEpoch)
                        if (existingLog != null) {
                            resolvedLogId = existingLog.id
                            // Do not spam if user already completed today's occurrence
                            if (existingLog.status == ActivityStatus.COMPLETED) {
                                AlarmDiagnostics.record(context, "تُجوهل [$kind#$targetId]: العبادة مكتملة اليوم")
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
                    // Snoozes of non-activity reminders (adhkar, quran, ...) have no log; only activity snoozes do.
                    if (activityId > 0L && (log == null || log.status == ActivityStatus.COMPLETED)) {
                        AlarmDiagnostics.record(context, "تُجوهلت غفوة [$kind#$targetId]: السجل محذوف أو مكتمل")
                        return@launch
                    }
                    if (activityId <= 0L && log?.status == ActivityStatus.COMPLETED) {
                        AlarmDiagnostics.record(context, "تُجوهلت غفوة [$kind#$targetId]: مكتملة")
                        return@launch
                    }
                } else {
                    // Record this reminder in the history (completed / missed list)
                    val sysLog = runCatching { logSystemReminder(db, kind, targetId, profileId, title) }.getOrNull()
                    if (sysLog != null) {
                        resolvedActivityId = sysLog.first
                        resolvedLogId = sysLog.second
                    }
                    // Re-arm only this reminder; rescheduleAll() would move other alarms due now to tomorrow
                    app.container.reminderScheduler.rescheduleOne(kind, targetId, profileId)
                }

                NotificationHelper.showReminderNotification(
                    context = context,
                    notificationId = notificationId,
                    title = title,
                    body = body,
                    soundMode = soundMode,
                    activityId = resolvedActivityId,
                    logId = resolvedLogId,
                    profileId = profileId
                )
                AlarmDiagnostics.record(context, "نُشر الإشعار [$kind#$targetId] «$title»")
            } catch (t: Throwable) {
                AlarmDiagnostics.record(context, "خطأ في [$kind#$targetId]: ${t.javaClass.simpleName} ${t.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Adhkar / quran / memorization / revision reminders have no WorshipActivity of their own.
     * A hidden, disabled activity ("sys-<kind>-<id>") carries today's log so it shows in the history.
     * Returns (activityId, logId) or null for kinds that are not recorded.
     */
    private suspend fun logSystemReminder(
        db: com.rafiqaldhikr.app.data.local.db.AppDatabase,
        kind: String,
        targetId: Long,
        profileId: Long,
        title: String
    ): Pair<Long, Long>? {
        val category = when (kind) {
            ReminderScheduler.KIND_ADHKAR -> com.rafiqaldhikr.app.core.model.ActivityCategory.ADHKAR
            ReminderScheduler.KIND_QURAN -> com.rafiqaldhikr.app.core.model.ActivityCategory.QURAN
            ReminderScheduler.KIND_MEMORIZATION,
            ReminderScheduler.KIND_REVISION -> com.rafiqaldhikr.app.core.model.ActivityCategory.MEMORIZATION
            else -> return null
        }
        if (profileId <= 0L) return null
        val dao = db.worshipDao()
        val key = "sys-$kind-$targetId"
        val activity = dao.getActivityByTemplateKey(profileId, key) ?: run {
            val newId = dao.insertActivity(
                com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity(
                    profileId = profileId,
                    templateKey = key,
                    title = title,
                    category = category,
                    isEnabled = false,
                    reminderEnabled = false
                )
            )
            dao.getActivityById(newId) ?: return null
        }
        val todayEpoch = LocalDate.now().toEpochDay()
        val existing = dao.getLogForActivityDay(profileId, activity.id, todayEpoch)
        if (existing != null) return activity.id to existing.id
        val logId = dao.insertLog(
            ActivityLogEntity(
                profileId = profileId,
                activityId = activity.id,
                activityTitleSnapshot = title,
                categorySnapshot = category,
                dateEpochDay = todayEpoch,
                scheduledAtMillis = System.currentTimeMillis(),
                status = ActivityStatus.PENDING
            )
        )
        return activity.id to logId
    }

    private suspend fun isTargetStillActive(
        app: RafiqApplication,
        kind: String,
        targetId: Long,
        profileId: Long,
        activityId: Long
    ): Boolean {
        val db = app.container.database
        return when (kind) {
            ReminderScheduler.KIND_ACTIVITY -> {
                val a = db.worshipDao().getActivityById(activityId)
                a != null && a.isEnabled && a.reminderEnabled
            }
            ReminderScheduler.KIND_ADHKAR ->
                db.adhkarDao().getAllEnabledReminders().any { it.id == targetId }
            ReminderScheduler.KIND_QURAN ->
                db.quranDao().getGoal(profileId)?.reminderEnabled == true
            ReminderScheduler.KIND_MEMORIZATION ->
                db.memorizationDao().getPlanById(targetId)?.let { it.isActive && it.memReminderEnabled } == true
            ReminderScheduler.KIND_REVISION ->
                db.memorizationDao().getPlanById(targetId)?.let { it.isActive && it.revReminderEnabled } == true
            ReminderScheduler.KIND_FASTING ->
                db.fastingDao().getByDate(profileId, targetId)?.let { it.reminderEnabled && !it.isCompleted } == true
            ReminderScheduler.KIND_SEASONAL ->
                db.seasonalDao().getAll().any { it.id == targetId && it.isSelected && it.reminderEnabled }
            else -> true
        }
    }
}
