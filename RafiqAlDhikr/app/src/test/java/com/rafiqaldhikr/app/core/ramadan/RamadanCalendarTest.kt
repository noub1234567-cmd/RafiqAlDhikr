package com.rafiqaldhikr.app.core.ramadan

import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.prayer.Prayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class RamadanCalendarTest {
    private val zone = ZoneId.of("Africa/Algiers")
    private fun at(d: LocalDate, h: Int, m: Int) = ZonedDateTime.of(d, java.time.LocalTime.of(h, m), zone)
    private fun times(d: LocalDate) = mapOf(Prayer.FAJR to at(d, 5, 30), Prayer.MAGHRIB to at(d, 18, 0))

    @Test fun startIsFirstOfRamadan() {
        for (offset in -1..1) {
            val s = RamadanCalendar.startOf(1448, offset)
            val h = HijriCalendarHelper.toHijri(s, offset)
            assertEquals(9, h.month); assertEquals(1, h.day); assertEquals(1448, h.year)
            assertEquals(8, HijriCalendarHelper.toHijri(s.minusDays(1), offset).month)
        }
    }

    @Test fun statusBeforeAndDuring() {
        val start = RamadanCalendar.startOf(1448, 0)
        val before = RamadanCalendar.statusFor(start.minusDays(10), 0) as RamadanStatus.Before
        assertEquals(10L, before.daysUntil)
        assertEquals(start, before.start)
        val first = RamadanCalendar.statusFor(start, 0) as RamadanStatus.During
        assertEquals(1, first.dayNumber)
        val d15 = RamadanCalendar.statusFor(start.plusDays(14), 0) as RamadanStatus.During
        assertEquals(15, d15.dayNumber)
        assertTrue(first.length == 29 || first.length == 30)
    }

    @Test fun afterRamadanPointsToNextYear() {
        val start = RamadanCalendar.startOf(1448, 0)
        val afterEnd = start.plusDays(RamadanCalendar.lengthOf(1448).toLong())
        val s = RamadanCalendar.statusFor(afterEnd, 0) as RamadanStatus.Before
        assertTrue(s.start.isAfter(afterEnd.plusDays(300)))
    }

    @Test fun markers() {
        val d = LocalDate.of(2027, 2, 10)
        val today = times(d); val tomorrow = times(d.plusDays(1))
        assertEquals(FastMarker.IMSAK, RamadanCalendar.nextMarker(at(d, 4, 0), today, tomorrow)!!.kind)
        assertEquals(at(d, 5, 20), RamadanCalendar.nextMarker(at(d, 4, 0), today, tomorrow)!!.at)
        assertEquals(FastMarker.IFTAR, RamadanCalendar.nextMarker(at(d, 12, 0), today, tomorrow)!!.kind)
        val night = RamadanCalendar.nextMarker(at(d, 20, 0), today, tomorrow)!!
        assertEquals(FastMarker.IMSAK, night.kind)
        assertEquals(at(d.plusDays(1), 5, 20), night.at)
    }
}
