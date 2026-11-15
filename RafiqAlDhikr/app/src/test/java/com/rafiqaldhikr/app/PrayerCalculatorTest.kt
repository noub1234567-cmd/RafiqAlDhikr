package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.prayer.CalcMethod
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerCalculator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PrayerCalculatorTest {

    private fun minutes(t: java.time.ZonedDateTime) = t.hour * 60 + t.minute

    @Test
    fun algiersNovemberTimesAreInRealisticRange() {
        val t = PrayerCalculator.compute(
            LocalDate.of(2026, 11, 15), 36.7538, 3.0588, ZoneId.of("Africa/Algiers"), CalcMethod.ALGERIA
        )
        assertNotNull(t)
        t!!
        // Reference values of published Algiers timetables for mid-November, within a few minutes.
        assertTrue(minutes(t.getValue(Prayer.DHUHR)) in (12 * 60 + 28)..(12 * 60 + 38))
        assertTrue(minutes(t.getValue(Prayer.MAGHRIB)) in (17 * 60 + 33)..(17 * 60 + 43))
        assertTrue(minutes(t.getValue(Prayer.FAJR)) in (5 * 60 + 50)..(6 * 60 + 5))
    }

    @Test
    fun timesAreInChronologicalOrder() {
        val t = PrayerCalculator.compute(
            LocalDate.of(2026, 6, 21), 36.7538, 3.0588, ZoneId.of("Africa/Algiers"), CalcMethod.ALGERIA
        )!!
        val order = listOf(Prayer.FAJR, Prayer.SUNRISE, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        for (i in 0 until order.size - 1) {
            assertTrue(t.getValue(order[i]).isBefore(t.getValue(order[i + 1])))
        }
    }
}
