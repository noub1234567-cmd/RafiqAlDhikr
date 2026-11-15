package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.special.SpecialReminder
import com.rafiqaldhikr.app.core.special.SpecialReminders
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class SpecialRemindersTest {
    private val zone: ZoneId = ZoneOffset.UTC

    private fun millis(y: Int, m: Int, d: Int, h: Int, min: Int) =
        LocalDateTime.of(y, m, d, h, min).atZone(zone).toInstant().toEpochMilli()

    private fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    @Test
    fun fridayReminderFiresOnTheNextFridayMorning() {
        // 2026-11-15 is a Sunday.
        val t = SpecialReminders.nextTrigger(SpecialReminder.FRIDAY_KAHF, millis(2026, 11, 15, 21, 0), zone)
        assertNotNull(t)
        assertEquals(millis(2026, 11, 20, 9, 0), t!!.triggerAtMillis)
    }

    @Test
    fun mondayThursdayReminderFiresTheEveningBefore() {
        // Sunday evening already passed, so the next one is Wednesday evening (before Thursday).
        val t = SpecialReminders.nextTrigger(SpecialReminder.MON_THU_FAST, millis(2026, 11, 15, 21, 0), zone)
        assertEquals(millis(2026, 11, 18, 20, 0), t!!.triggerAtMillis)
        assertTrue(t.body.contains("الخميس"))
    }

    @Test
    fun whiteDaysReminderFiresTheEveningBeforeAWhiteDay() {
        val t = SpecialReminders.nextTrigger(SpecialReminder.WHITE_DAYS, millis(2026, 11, 15, 12, 0), zone)
        assertNotNull(t)
        val tomorrow = dateOf(t!!.triggerAtMillis).plusDays(1)
        val hijriDay = HijriCalendarHelper.toHijri(tomorrow).day
        assertTrue("tomorrow should be Hijri 13-15 but was $hijriDay", hijriDay in 13..15)
    }
}
