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

    /** First date in 2027 whose Hijri month/day match (self-consistent with the app's own Hijri helper). */
    private fun firstDate(month: Int, day: Int): LocalDate =
        generateSequence(LocalDate.of(2027, 1, 1)) { it.plusDays(1) }
            .first { val h = HijriCalendarHelper.toHijri(it); h.month == month && h.day == day }

    @Test
    fun arafahReminderFiresTheEveningOf8DhulHijjah() {
        val eve = firstDate(12, 8)
        assertTrue(SpecialReminders.firesOn(SpecialReminder.ARAFAH_FAST, eve))
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.ARAFAH_FAST, eve.minusDays(1)))
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.ARAFAH_FAST, eve.plusDays(1)))
    }

    @Test
    fun ashuraReminderFiresBeforeThe9thAnd10thOfMuharram() {
        val eight = firstDate(1, 8)
        assertTrue(SpecialReminders.firesOn(SpecialReminder.ASHURA_FAST, eight))
        assertTrue(SpecialReminders.firesOn(SpecialReminder.ASHURA_FAST, eight.plusDays(1)))
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.ASHURA_FAST, eight.plusDays(2)))
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.ASHURA_FAST, eight.minusDays(1)))
    }

    @Test
    fun shawwalReminderSkipsEidAndRepeatsWeekly() {
        val firstShawwal = firstDate(10, 1)
        // The evening before Eid is not a reminder day; the 1st evening is (fast on the 2nd).
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.SHAWWAL_SIX, firstShawwal.minusDays(1)))
        assertTrue(SpecialReminders.firesOn(SpecialReminder.SHAWWAL_SIX, firstShawwal))
        assertTrue(SpecialReminders.firesOn(SpecialReminder.SHAWWAL_SIX, firstShawwal.plusDays(7)))
        assertTrue(!SpecialReminders.firesOn(SpecialReminder.SHAWWAL_SIX, firstShawwal.plusDays(3)))
    }

    @Test
    fun existingReminderOrdinalsAreUnchanged() {
        assertEquals(
            listOf("FRIDAY_KAHF", "MON_THU_FAST", "WHITE_DAYS", "ARAFAH_FAST", "ASHURA_FAST", "SHAWWAL_SIX"),
            SpecialReminder.entries.map { it.name }
        )
    }
}
