package com.rafiqaldhikr.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.core.model.QuranSurahs
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduler(
    private val context: Context,
    private val database: AppDatabase
) {
    companion object {
        const val KIND_ACTIVITY = "activity"
        const val KIND_ADHKAR = "adhkar"
        const val KIND_QURAN = "quran"
        const val KIND_MEMORIZATION = "memorization"
        const val KIND_REVISION = "revision"
        const val KIND_FASTING = "fasting"
        const val KIND_SEASONAL = "seasonal"
        const val KIND_RESCHEDULED = "rescheduled"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun rescheduleAll() {
        val now = System.currentTimeMillis()

        // 1. Worship activities
        database.worshipDao().getAllActiveReminderActivities().forEach { activity ->
            scheduleActivityReminder(activity, now)
        }

        // 2. Adhkar category reminders
        database.adhkarDao().getAllEnabledReminders().forEach { reminder ->
            scheduleAdhkarReminder(reminder, now)
        }

        // 3. Quran daily goal reminders
        database.quranDao().getAllEnabledGoalReminders().forEach { goal ->
            scheduleQuranReminder(goal, now)
        }

        // 4. Memorization & revision reminders
        database.memorizationDao().getAllReminderPlans().forEach { plan ->
            scheduleMemorizationReminders(plan, now)
        }

        // 5. Fasting reminders
        database.fastingDao().getAllActiveReminders().forEach { day ->
            scheduleFastingReminder(day, now)
        }

        // 6. Seasonal reminders
        database.seasonalDao().getAllEnabledReminders().forEach { sel ->
            scheduleSeasonalReminder(sel, now)
        }
    }

    fun scheduleActivityReminder(activity: WorshipActivityEntity, nowMillis: Long = System.currentTimeMillis()) {
        val requestCode = requestCodeFor(KIND_ACTIVITY, activity.id)
        if (!activity.isEnabled || !activity.reminderEnabled) {
            cancelAlarm(requestCode)
            return
        }
        val trigger = MissedActivityEvaluator.computeNextTriggerMillis(activity, nowMillis) ?: run {
            cancelAlarm(requestCode)
            return
        }
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = trigger,
            kind = KIND_ACTIVITY,
            targetId = activity.id,
            profileId = activity.profileId,
            title = "تذكير عبادة: ${activity.title}",
            body = activity.description.ifBlank { "حان وقت أداء ${activity.title} بتيسير وطمأنينة." },
            soundMode = activity.soundMode
        )
    }

    fun scheduleOneOffRescheduledReminder(
        logId: Long,
        activityId: Long,
        profileId: Long,
        title: String,
        body: String,
        triggerAtMillis: Long,
        soundMode: NotificationSoundMode = NotificationSoundMode.NORMAL
    ) {
        val requestCode = requestCodeFor(KIND_RESCHEDULED, logId)
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = triggerAtMillis,
            kind = KIND_RESCHEDULED,
            targetId = logId,
            profileId = profileId,
            title = title,
            body = body,
            soundMode = soundMode,
            extraActivityId = activityId
        )
    }

    fun scheduleAdhkarReminder(reminder: AdhkarReminderEntity, nowMillis: Long = System.currentTimeMillis()) {
        val requestCode = requestCodeFor(KIND_ADHKAR, reminder.profileId * 10 + reminder.category.ordinal)
        if (!reminder.enabled) {
            cancelAlarm(requestCode)
            return
        }
        val trigger = nextDailyMillis(reminder.hour, reminder.minute, nowMillis)
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = trigger,
            kind = KIND_ADHKAR,
            targetId = reminder.id,
            profileId = reminder.profileId,
            title = reminder.category.labelAr,
            body = "حان وقت ${reminder.category.labelAr}، حفظك الله وبارك في يومك.",
            soundMode = reminder.soundMode
        )
    }

    fun scheduleQuranReminder(goal: QuranGoalEntity, nowMillis: Long = System.currentTimeMillis()) {
        val requestCode = requestCodeFor(KIND_QURAN, goal.profileId)
        if (!goal.reminderEnabled) {
            cancelAlarm(requestCode)
            return
        }
        val trigger = nextDailyMillis(goal.reminderHour, goal.reminderMinute, nowMillis)
        val unit = if (goal.goalType.name == "CUSTOM") goal.customUnitLabel else goal.goalType.unitAr
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = trigger,
            kind = KIND_QURAN,
            targetId = goal.profileId,
            profileId = goal.profileId,
            title = "ورد القرآن الكريم",
            body = "تذكير بقراءة وردك اليومي (${goal.targetAmount.toInt()} $unit).",
            soundMode = goal.soundMode
        )
    }

    fun scheduleMemorizationReminders(plan: MemorizationPlanEntity, nowMillis: Long = System.currentTimeMillis()) {
        val memCode = requestCodeFor(KIND_MEMORIZATION, plan.id)
        val surahName = QuranSurahs.byNumber(plan.surahNumber).nameAr
        if (plan.isActive && plan.memReminderEnabled) {
            val trigger = nextDailyMillis(plan.memReminderHour, plan.memReminderMinute, nowMillis)
            scheduleAlarm(
                requestCode = memCode,
                triggerAtMillis = trigger,
                kind = KIND_MEMORIZATION,
                targetId = plan.id,
                profileId = plan.profileId,
                title = "حفظ القرآن: سورة $surahName",
                body = "تذكير بورد الحفظ في خطة ${plan.title}.",
                soundMode = NotificationSoundMode.NORMAL
            )
        } else {
            cancelAlarm(memCode)
        }

        val revCode = requestCodeFor(KIND_REVISION, plan.id)
        if (plan.isActive && plan.revReminderEnabled) {
            val trigger = nextDailyMillis(plan.revReminderHour, plan.revReminderMinute, nowMillis)
            scheduleAlarm(
                requestCode = revCode,
                triggerAtMillis = trigger,
                kind = KIND_REVISION,
                targetId = plan.id,
                profileId = plan.profileId,
                title = "مراجعة القرآن: سورة $surahName",
                body = "تذكير بمراجعة المحفوظ في خطة ${plan.title}.",
                soundMode = NotificationSoundMode.NORMAL
            )
        } else {
            cancelAlarm(revCode)
        }
    }

    fun scheduleFastingReminder(day: FastingDayEntity, nowMillis: Long = System.currentTimeMillis()) {
        val code = requestCodeFor(KIND_FASTING, day.profileId * 100000 + (day.dateEpochDay % 100000))
        if (!day.reminderEnabled || day.isCompleted) {
            cancelAlarm(code)
            return
        }
        // Remind the evening before fasting day if possible, or on the fasting morning
        val fastingDate = LocalDate.ofEpochDay(day.dateEpochDay)
        val reminderDate = fastingDate.minusDays(1)
        val zdt = ZonedDateTime.of(
            reminderDate,
            LocalTime.of(day.reminderHour.coerceIn(0, 23), day.reminderMinute.coerceIn(0, 59)),
            ZoneId.systemDefault()
        )
        val trigger = zdt.toInstant().toEpochMilli()
        if (trigger <= nowMillis) return

        scheduleAlarm(
            requestCode = code,
            triggerAtMillis = trigger,
            kind = KIND_FASTING,
            targetId = day.dateEpochDay,
            profileId = day.profileId,
            title = "تذكير بالصيام غداً",
            body = "غداً بإذن الله موعد صيام: ${day.occasion.labelAr}.",
            soundMode = NotificationSoundMode.GENTLE_SOUND
        )
    }

    fun scheduleSeasonalReminder(selection: SeasonalSelectionEntity, nowMillis: Long = System.currentTimeMillis()) {
        val code = requestCodeFor(KIND_SEASONAL, selection.id)
        if (!selection.isSelected || !selection.reminderEnabled) {
            cancelAlarm(code)
            return
        }
        val trigger = nextDailyMillis(selection.reminderHour, selection.reminderMinute, nowMillis)
        scheduleAlarm(
            requestCode = code,
            triggerAtMillis = trigger,
            kind = KIND_SEASONAL,
            targetId = selection.id,
            profileId = selection.profileId,
            title = "تذكير موسمي بالعبادة",
            body = "تذكير بالعمل الصالح ضمن مواسم الخير المختارة.",
            soundMode = NotificationSoundMode.GENTLE_SOUND
        )
    }

    private fun scheduleAlarm(
        requestCode: Int,
        triggerAtMillis: Long,
        kind: String,
        targetId: Long,
        profileId: Long,
        title: String,
        body: String,
        soundMode: NotificationSoundMode,
        extraActivityId: Long = if (kind == KIND_ACTIVITY) targetId else -1L
    ) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(NotificationHelper.EXTRA_KIND, kind)
            putExtra(NotificationHelper.EXTRA_TARGET_ID, targetId)
            putExtra(NotificationHelper.EXTRA_PROFILE_ID, profileId)
            putExtra(NotificationHelper.EXTRA_ACTIVITY_ID, extraActivityId)
            putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(NotificationHelper.EXTRA_TITLE, title)
            putExtra(NotificationHelper.EXTRA_BODY, body)
            putExtra(NotificationHelper.EXTRA_SOUND_MODE, soundMode.name)
        }

        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
    }

    fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    private fun nextDailyMillis(hour: Int, minute: Int, nowMillis: Long): Long {
        val zone = ZoneId.systemDefault()
        val nowZdt = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone)
        var target = nowZdt.toLocalDate()
            .atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            .atZone(zone)
        if (!target.toInstant().toEpochMilli().let { it > nowMillis }) {
            target = target.plusDays(1)
        }
        return target.toInstant().toEpochMilli()
    }

    private fun requestCodeFor(kind: String, id: Long): Int {
        return (kind.hashCode() * 31 + id.hashCode()) and 0x7FFFFFFF
    }
}
