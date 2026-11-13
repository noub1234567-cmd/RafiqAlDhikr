package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.ScheduleType
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.notifications.MissedActivityEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class MissedActivityEvaluatorTest {

    @Test
    fun `specific weekdays bitmask matches only selected days`() {
        val fridayMask = 1 shl (DayOfWeek.FRIDAY.value - 1)
        val activity = WorshipActivityEntity(
            id = 1,
            profileId = 1,
            title = "سورة الكهف",
            scheduleType = ScheduleType.SPECIFIC_WEEKDAYS,
            weekdaysMask = fridayMask,
            isEnabled = true
        )
        // 2026-11-13 is Friday
        val friday = LocalDate.of(2026, 11, 13)
        val saturday = friday.plusDays(1)
        assertTrue(MissedActivityEvaluator.isScheduledOnDate(activity, friday))
        assertFalse(MissedActivityEvaluator.isScheduledOnDate(activity, saturday))
    }

    @Test
    fun `custom interval matches every N days from anchor`() {
        val anchor = LocalDate.of(2026, 11, 1)
        val activity = WorshipActivityEntity(
            id = 2,
            profileId = 1,
            title = "مراجعة",
            scheduleType = ScheduleType.CUSTOM_INTERVAL,
            customIntervalDays = 3,
            anchorEpochDay = anchor.toEpochDay(),
            isEnabled = true
        )
        assertTrue(MissedActivityEvaluator.isScheduledOnDate(activity, anchor))
        assertFalse(MissedActivityEvaluator.isScheduledOnDate(activity, anchor.plusDays(1)))
        assertTrue(MissedActivityEvaluator.isScheduledOnDate(activity, anchor.plusDays(3)))
    }

    @Test
    fun `passing deadline marks pending log as missed`() {
        val scheduled = 1_000_000L
        val log = ActivityLogEntity(
            id = 10,
            profileId = 1,
            activityId = 1,
            activityTitleSnapshot = "صلاة الضحى",
            categorySnapshot = ActivityCategory.PRAYER_SUNNAH,
            dateEpochDay = 20000L,
            scheduledAtMillis = scheduled,
            status = ActivityStatus.PENDING
        )

        val beforeDeadline = MissedActivityEvaluator.evaluateStatus(log, scheduled - 1000L)
        assertEquals(ActivityStatus.PENDING, beforeDeadline.status)

        val afterDeadline = MissedActivityEvaluator.evaluateStatus(log, scheduled + 1000L)
        assertEquals(ActivityStatus.MISSED, afterDeadline.status)
        assertNull(afterDeadline.completedAtMillis)
    }

    @Test
    fun `rescheduling missed activity NEVER marks it completed`() {
        val missedLog = ActivityLogEntity(
            id = 11,
            profileId = 1,
            activityId = 1,
            activityTitleSnapshot = "ورد القرآن",
            categorySnapshot = ActivityCategory.QURAN,
            dateEpochDay = 20000L,
            scheduledAtMillis = 1_000_000L,
            status = ActivityStatus.MISSED,
            completedAtMillis = null
        )

        val newReminderTime = 2_000_000L
        val rescheduled = MissedActivityEvaluator.rescheduleWithoutCompleting(missedLog, newReminderTime)

        assertEquals(ActivityStatus.RESCHEDULED, rescheduled.status)
        assertEquals(newReminderTime, rescheduled.rescheduledToMillis)
        assertNull(rescheduled.completedAtMillis)

        // If rescheduled time also passes without user clicking complete, it becomes MISSED again
        val missedAgain = MissedActivityEvaluator.evaluateStatus(rescheduled, newReminderTime + 500L)
        assertEquals(ActivityStatus.MISSED, missedAgain.status)
        assertNull(missedAgain.completedAtMillis)

        // Only explicit completion sets COMPLETED
        val completed = MissedActivityEvaluator.markExplicitlyCompleted(missedAgain, newReminderTime + 600L)
        assertEquals(ActivityStatus.COMPLETED, completed.status)
        assertNotNull(completed.completedAtMillis)
    }

    @Test
    fun `computeNextTriggerMillis picks next future slot`() {
        val zone = ZoneId.of("UTC")
        val activity = WorshipActivityEntity(
            id = 5,
            profileId = 1,
            title = "أذكار الصباح",
            scheduleType = ScheduleType.DAILY,
            reminderEnabled = true,
            reminderHour = 7,
            reminderMinute = 0,
            isEnabled = true
        )
        val nowAt8am = ZonedDateTime.of(LocalDate.of(2026, 11, 13), LocalTime.of(8, 0), zone)
            .toInstant().toEpochMilli()
        val next = MissedActivityEvaluator.computeNextTriggerMillis(activity, nowAt8am, zone)
        val expectedTomorrow7am = ZonedDateTime.of(LocalDate.of(2026, 11, 14), LocalTime.of(7, 0), zone)
            .toInstant().toEpochMilli()
        assertEquals(expectedTomorrow7am, next)
    }
}
