package com.rafiqaldhikr.app.notifications

import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.ScheduleType
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

object MissedActivityEvaluator {

    fun isScheduledOnDate(activity: WorshipActivityEntity, date: LocalDate): Boolean {
        if (!activity.isEnabled) return false
        val epochDay = date.toEpochDay()
        return when (activity.scheduleType) {
            ScheduleType.DAILY -> true
            ScheduleType.WEEKLY -> {
                val anchor = LocalDate.ofEpochDay(
                    if (activity.anchorEpochDay > 0) activity.anchorEpochDay else date.toEpochDay()
                )
                date.dayOfWeek == anchor.dayOfWeek
            }
            ScheduleType.SPECIFIC_WEEKDAYS -> {
                val bitIndex = date.dayOfWeek.value - 1 // Monday=0 .. Sunday=6
                (activity.weekdaysMask and (1 shl bitIndex)) != 0
            }
            ScheduleType.ONE_TIME -> activity.oneTimeDateEpochDay == epochDay
            ScheduleType.CUSTOM_INTERVAL -> {
                val interval = activity.customIntervalDays.coerceAtLeast(1)
                val anchor = activity.anchorEpochDay
                val diff = epochDay - anchor
                diff >= 0 && (diff % interval == 0L)
            }
        }
    }

    fun scheduledMillisForDate(
        activity: WorshipActivityEntity,
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val time = LocalTime.of(
            activity.reminderHour.coerceIn(0, 23),
            activity.reminderMinute.coerceIn(0, 59)
        )
        return ZonedDateTime.of(date, time, zoneId).toInstant().toEpochMilli()
    }

    /**
     * Computes the next future trigger timestamp in epoch millis for [activity],
     * or null if reminders are disabled or no future occurrence exists (e.g. past ONE_TIME).
     */
    fun computeNextTriggerMillis(
        activity: WorshipActivityEntity,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (!activity.isEnabled || !activity.reminderEnabled) return null
        val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

        if (activity.scheduleType == ScheduleType.ONE_TIME) {
            val targetEpoch = activity.oneTimeDateEpochDay ?: return null
            val candidate = scheduledMillisForDate(activity, LocalDate.ofEpochDay(targetEpoch), zoneId)
            return if (candidate > nowMillis) candidate else null
        }

        for (dayOffset in 0L..366L) {
            val candidateDate = today.plusDays(dayOffset)
            if (isScheduledOnDate(activity, candidateDate)) {
                val candidateMillis = scheduledMillisForDate(activity, candidateDate, zoneId)
                if (candidateMillis > nowMillis) {
                    return candidateMillis
                }
            }
        }
        return null
    }

    /**
     * Evaluates an existing [ActivityLogEntity] against [nowMillis].
     * - If already COMPLETED, returns unchanged.
     * - If PENDING and scheduled time has passed, marks as MISSED.
     * - If RESCHEDULED and the rescheduled time has also passed without completion, marks as MISSED.
     */
    fun evaluateStatus(log: ActivityLogEntity, nowMillis: Long): ActivityLogEntity {
        if (log.status == ActivityStatus.COMPLETED) return log
        val effectiveDeadline = when (log.status) {
            ActivityStatus.RESCHEDULED -> log.rescheduledToMillis ?: log.scheduledAtMillis
            else -> log.scheduledAtMillis
        }
        return if (nowMillis > effectiveDeadline && log.status != ActivityStatus.MISSED) {
            log.copy(status = ActivityStatus.MISSED)
        } else {
            log
        }
    }

    /**
     * Reschedules an incomplete activity to [newReminderMillis].
     * Strictly guarantees [ActivityLogEntity.completedAtMillis] stays null and status is NEVER COMPLETED.
     */
    fun rescheduleWithoutCompleting(
        log: ActivityLogEntity,
        newReminderMillis: Long
    ): ActivityLogEntity {
        require(log.status != ActivityStatus.COMPLETED) {
            "لا يمكن إعادة جدولة نشاط مكتمل بالفعل."
        }
        return log.copy(
            status = ActivityStatus.RESCHEDULED,
            rescheduledToMillis = newReminderMillis,
            completedAtMillis = null
        )
    }

    /**
     * Explicit completion only when the user presses the complete button.
     */
    fun markExplicitlyCompleted(
        log: ActivityLogEntity,
        completedAtMillis: Long
    ): ActivityLogEntity {
        return log.copy(
            status = ActivityStatus.COMPLETED,
            completedAtMillis = completedAtMillis
        )
    }
}
