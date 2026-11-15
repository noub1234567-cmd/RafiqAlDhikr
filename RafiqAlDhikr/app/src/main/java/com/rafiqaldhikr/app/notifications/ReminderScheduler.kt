package com.rafiqaldhikr.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rafiqaldhikr.app.core.special.SpecialReminder
import com.rafiqaldhikr.app.core.special.SpecialReminders
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
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
        const val KIND_PRAYER = "prayer"
        const val KIND_SPECIAL = "special"
        const val KIND_ADHKAR = "adhkar"
        const val KIND_QURAN = "quran"
        const val KIND_MEMORIZATION = "memorization"
        const val KIND_REVISION = "revision"
        const val KIND_FASTING = "fasting"
        const val KIND_SEASONAL = "seasonal"
        const val KIND_RESCHEDULED = "rescheduled"
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Delivers alarms that were armed for a moment already in the past but never reached the app
     * (the system dropped them while the app was closed). The normal receiver handles them.
     */
    fun catchUpMissedAlarms() {
        val now = System.currentTimeMillis()
        AlarmState.overdue(context, now).forEach { a ->
            AlarmDiagnostics.record(context, "تعويض منبّه فائت [${a.kind}#${a.targetId}] «${a.title}»")
            // Same code path as a real alarm; it also marks the alarm as fired.
            context.sendBroadcast(
                Intent(context, ReminderReceiver::class.java).apply {
                    putExtra(NotificationHelper.EXTRA_KIND, a.kind)
                    putExtra(NotificationHelper.EXTRA_TARGET_ID, a.targetId)
                    putExtra(NotificationHelper.EXTRA_PROFILE_ID, a.profileId)
                    putExtra(NotificationHelper.EXTRA_ACTIVITY_ID, a.activityId)
                    putExtra(NotificationHelper.EXTRA_NOTIFICATION_ID, a.requestCode)
                    putExtra(NotificationHelper.EXTRA_TITLE, a.title)
                    putExtra(NotificationHelper.EXTRA_BODY, a.body)
                    putExtra(NotificationHelper.EXTRA_SOUND_MODE, a.sound)
                }
            )
            // Prevent re-delivery by a second rescheduleAll() before the receiver runs.
            AlarmState.markFired(context, a.requestCode)
        }
    }

    suspend fun rescheduleAll() {
        catchUpMissedAlarms()
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

        // 7. Adhan (prayer-time) reminders
        schedulePrayerAlarms(now)

        // 8. Friday / Monday-Thursday / white-days reminders
        scheduleSpecialAlarms(now)
    }

    fun scheduleSpecialAlarms(nowMillis: Long = System.currentTimeMillis()) {
        SpecialReminder.entries.forEach { scheduleSpecialAlarm(it, nowMillis) }
    }

    fun scheduleSpecialAlarm(r: SpecialReminder, nowMillis: Long = System.currentTimeMillis()) {
        val requestCode = requestCodeFor(KIND_SPECIAL, r.ordinal.toLong())
        if (!SpecialReminders.isEnabled(context, r)) {
            cancelAlarm(requestCode)
            return
        }
        val next = SpecialReminders.nextTrigger(r, nowMillis) ?: run {
            cancelAlarm(requestCode)
            return
        }
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = next.triggerAtMillis,
            kind = KIND_SPECIAL,
            targetId = r.ordinal.toLong(),
            profileId = -1L,
            title = next.title,
            body = next.body,
            soundMode = NotificationSoundMode.GENTLE_SOUND
        )
    }

    /** Arms or cancels the adhan reminder of every prayer according to the saved settings. */
    fun schedulePrayerAlarms(nowMillis: Long = System.currentTimeMillis()) {
        PrayerSettings.fardPrayers.forEach { schedulePrayerAlarm(it, nowMillis) }
    }

    fun schedulePrayerAlarm(prayer: Prayer, nowMillis: Long = System.currentTimeMillis()) {
        val requestCode = requestCodeFor(KIND_PRAYER, prayer.ordinal.toLong())
        if (!PrayerSettings.adhanEnabled(context, prayer)) {
            cancelAlarm(requestCode)
            return
        }
        val trigger = PrayerSettings.nextAdhanTrigger(context, prayer, nowMillis) ?: run {
            cancelAlarm(requestCode)
            return
        }
        val lead = PrayerSettings.adhanLeadMinutes(context)
        val city = PrayerSettings.city(context)
        val prayerTime = java.time.Instant.ofEpochMilli(trigger + lead * 60_000L)
            .atZone(java.time.ZoneId.of(city.zoneId))
        val hhmm = "%02d:%02d".format(prayerTime.hour, prayerTime.minute)
        scheduleAlarm(
            requestCode = requestCode,
            triggerAtMillis = trigger,
            kind = KIND_PRAYER,
            targetId = prayer.ordinal.toLong(),
            profileId = -1L,
            title = if (lead == 0) "حان موعد أذان ${prayer.labelAr}" else "اقترب أذان ${prayer.labelAr}",
            body = if (lead == 0) "${city.nameAr} — $hhmm" else "بعد $lead دقيقة (${city.nameAr} — $hhmm)",
            soundMode = NotificationSoundMode.NORMAL
        )
    }

    /**
     * Re-arms ONLY the reminder that just fired.
     * Calling [rescheduleAll] from a receiver pushed every other alarm that was due at the same
     * moment (or in the same batch) to tomorrow, so only one notification was delivered.
     */
    suspend fun rescheduleOne(kind: String, targetId: Long, profileId: Long) {
        val now = System.currentTimeMillis()
        when (kind) {
            KIND_ADHKAR -> database.adhkarDao().getAllEnabledReminders()
                .firstOrNull { it.id == targetId }
                ?.let { scheduleAdhkarReminder(it, now) }
            KIND_QURAN -> database.quranDao().getGoal(profileId)
                ?.let { scheduleQuranReminder(it, now) }
            KIND_MEMORIZATION, KIND_REVISION -> database.memorizationDao().getPlanById(targetId)
                ?.let { scheduleMemorizationReminders(it, now, onlyKind = kind) }
            KIND_SEASONAL -> database.seasonalDao().getAll()
                .firstOrNull { it.id == targetId }
                ?.let { scheduleSeasonalReminder(it, now) }
            KIND_PRAYER -> Prayer.entries.getOrNull(targetId.toInt())?.let { schedulePrayerAlarm(it, now) }
            KIND_SPECIAL -> SpecialReminder.entries.getOrNull(targetId.toInt())?.let { scheduleSpecialAlarm(it, now) }
            else -> Unit // fasting reminders are one-shot: nothing to re-arm
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
            AlarmDiagnostics.record(context, "تذكير «${reminder.category.labelAr}» غير مفعّل — لم يُجدوَل")
            return
        }
        // A reminder linked to a prayer follows that prayer's time as it changes through the year.
        val prayerLink = com.rafiqaldhikr.app.core.prayer.PrayerSettings
            .link(context, reminder.profileId, reminder.category.name)
        val trigger = prayerLink
            ?.let { com.rafiqaldhikr.app.core.prayer.PrayerSettings.nextTrigger(context, it, nowMillis) }
            ?: nextDailyMillis(reminder.hour, reminder.minute, nowMillis)
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

    fun scheduleMemorizationReminders(
        plan: MemorizationPlanEntity,
        nowMillis: Long = System.currentTimeMillis(),
        onlyKind: String? = null
    ) {
        val memCode = requestCodeFor(KIND_MEMORIZATION, plan.id)
        val surahName = QuranSurahs.byNumber(plan.surahNumber).nameAr
        if (onlyKind == KIND_REVISION) {
            // leave the memorization alarm untouched
        } else if (plan.isActive && plan.memReminderEnabled) {
            val trigger = nextScheduledMillis(plan.memScheduleType, plan.memWeekdaysMask, plan.memReminderHour, plan.memReminderMinute, nowMillis)
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
        if (onlyKind == KIND_MEMORIZATION) {
            // leave the revision alarm untouched
        } else if (plan.isActive && plan.revReminderEnabled) {
            val trigger = nextScheduledMillis(plan.revScheduleType, plan.revWeekdaysMask, plan.revReminderHour, plan.revReminderMinute, nowMillis)
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
        val trigger = nextSeasonMillis(selection, nowMillis) ?: run {
            cancelAlarm(code)
            return
        }
        val seasonTitle = com.rafiqaldhikr.app.core.model.SeasonalCatalog.byId(selection.seasonId)?.title
        scheduleAlarm(
            requestCode = code,
            triggerAtMillis = trigger,
            kind = KIND_SEASONAL,
            targetId = selection.id,
            profileId = selection.profileId,
            title = seasonTitle ?: "تذكير موسمي بالعبادة",
            body = "تذكير بالعمل الصالح في موسم ${seasonTitle ?: "الخير"}.",
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

        val useAlarmClock = AlarmState.useAlarmClock(context)
        if (useAlarmClock) {
            // "Alarm clock" alarms are exempt from the exact-alarm permission and are the most
            // reliable kind on aggressive OEM power managers (they also show a status-bar alarm icon).
            val showIntent = PendingIntent.getActivity(
                context,
                0,
                Intent(context, com.rafiqaldhikr.app.MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent), pending)
        } else if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
        }
        AlarmState.markArmed(
            context, requestCode, triggerAtMillis, kind, targetId, profileId,
            extraActivityId, title, body, soundMode.name
        )
        AlarmDiagnostics.record(
            context,
            "جُدول [$kind#$targetId] «$title» عند " +
                java.text.SimpleDateFormat("MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date(triggerAtMillis)) +
                when { useAlarmClock -> " (منبّه ساعة)"; canExact -> " (دقيق)"; else -> " (غير دقيق!)" }
        )
    }

    fun cancelAlarm(requestCode: Int) {
        AlarmState.clearArmed(context, requestCode)
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

    /** Next trigger honouring the weekday mask (bit0=Monday..bit6=Sunday) for weekday-based schedules. */
    private fun nextScheduledMillis(
        type: com.rafiqaldhikr.app.core.model.ScheduleType,
        weekdaysMask: Int,
        hour: Int,
        minute: Int,
        nowMillis: Long
    ): Long {
        val usesMask = (type == com.rafiqaldhikr.app.core.model.ScheduleType.SPECIFIC_WEEKDAYS ||
            type == com.rafiqaldhikr.app.core.model.ScheduleType.WEEKLY) && (weekdaysMask and 0b1111111) != 0
        if (!usesMask) return nextDailyMillis(hour, minute, nowMillis)
        val zone = ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0L..7L) {
            val date = today.plusDays(offset)
            if ((weekdaysMask and (1 shl (date.dayOfWeek.value - 1))) == 0) continue
            val millis = date.atTime(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
                .atZone(zone).toInstant().toEpochMilli()
            if (millis > nowMillis) return millis
        }
        return nextDailyMillis(hour, minute, nowMillis)
    }

    /** Next trigger that falls inside the selected season's Hijri window, or null if none within ~13 months. */
    private fun nextSeasonMillis(selection: SeasonalSelectionEntity, nowMillis: Long): Long? {
        val zone = ZoneId.systemDefault()
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0L..400L) {
            val date = today.plusDays(offset)
            val hijri = com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper.toHijri(date)
            if (!com.rafiqaldhikr.app.core.model.SeasonalCatalog.isInSeason(selection.seasonId, hijri.month, hijri.day)) continue
            val millis = date.atTime(selection.reminderHour.coerceIn(0, 23), selection.reminderMinute.coerceIn(0, 59))
                .atZone(zone).toInstant().toEpochMilli()
            if (millis > nowMillis) return millis
        }
        return null
    }

    fun cancelRescheduledReminder(logId: Long) {
        cancelAlarm(requestCodeFor(KIND_RESCHEDULED, logId))
    }

    /** Cancels every alarm belonging to [profileId] (call BEFORE deleting its rows). */
    suspend fun cancelAllForProfile(profileId: Long) {
        database.worshipDao().getActivitiesByProfile(profileId).forEach {
            cancelAlarm(requestCodeFor(KIND_ACTIVITY, it.id))
        }
        database.worshipDao().getAllLogs().filter { it.profileId == profileId }.forEach {
            cancelAlarm(requestCodeFor(KIND_RESCHEDULED, it.id))
        }
        database.adhkarDao().getAllReminders().filter { it.profileId == profileId }.forEach {
            cancelAlarm(requestCodeFor(KIND_ADHKAR, it.profileId * 10 + it.category.ordinal))
        }
        cancelAlarm(requestCodeFor(KIND_QURAN, profileId))
        database.memorizationDao().getAllPlans().filter { it.profileId == profileId }.forEach {
            cancelAlarm(requestCodeFor(KIND_MEMORIZATION, it.id))
            cancelAlarm(requestCodeFor(KIND_REVISION, it.id))
        }
        database.fastingDao().getAll().filter { it.profileId == profileId }.forEach {
            cancelAlarm(requestCodeFor(KIND_FASTING, it.profileId * 100000 + (it.dateEpochDay % 100000)))
        }
        database.seasonalDao().getAll().filter { it.profileId == profileId }.forEach {
            cancelAlarm(requestCodeFor(KIND_SEASONAL, it.id))
        }
    }

    suspend fun cancelAllForAllProfiles() {
        database.profileDao().getAll().forEach { cancelAllForProfile(it.id) }
    }

    private fun requestCodeFor(kind: String, id: Long): Int {
        return (kind.hashCode() * 31 + id.hashCode()) and 0x7FFFFFFF
    }
}
