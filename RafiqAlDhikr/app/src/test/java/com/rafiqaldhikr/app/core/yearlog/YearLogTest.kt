package com.rafiqaldhikr.app.core.yearlog

import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class YearLogTest {
    @Test fun ratioLevels() {
        assertEquals(0, YearLog.levelFromRatio(0.0))
        assertEquals(1, YearLog.levelFromRatio(0.2))
        assertEquals(2, YearLog.levelFromRatio(0.4))
        assertEquals(3, YearLog.levelFromRatio(0.6))
        assertEquals(4, YearLog.levelFromRatio(1.0))
    }

    @Test fun prayerLevelsUseFardOnly() {
        fun log(day: Long, key: String) = PrayerLogEntity(profileId = 1, dateEpochDay = day, itemKey = key, completedAtMillis = 0)
        val logs = listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA").map { log(10, it) } +
            listOf(log(11, "FAJR"), log(11, "WITR"), log(11, "DUHA"))
        val lv = YearLog.levels(LogKind.PRAYER, YearLogInputs(prayerLogs = logs))
        assertEquals(4, lv[10L])
        assertEquals(1, lv[11L]) // one fard of five
        assertTrue(YearLog.qualifies(LogKind.PRAYER, lv[10L]!!))
        assertFalse(YearLog.qualifies(LogKind.PRAYER, lv[11L]!!))
    }

    @Test fun tasbeehThresholds() {
        fun t(day: Long, n: Int) = TasbeehLogEntity(profileId = 1, tasbeehTypeId = 1, dateEpochDay = day, count = n)
        val lv = YearLog.levels(LogKind.TASBEEH, YearLogInputs(tasbeehLogs = listOf(t(1, 5), t(2, 150), t(3, 400), t(4, 1200), t(5, 0))))
        assertEquals(mapOf(1L to 1, 2L to 2, 3L to 3, 4L to 4), lv)
    }

    @Test fun streaks() {
        val days = setOf(1L, 2L, 3L, 7L, 8L, 9L, 10L)
        assertEquals(4, YearLog.longestStreak(days))
        assertEquals(4, YearLog.currentStreak(days, 10))
        assertEquals(4, YearLog.currentStreak(days, 11)) // today still in progress
        assertEquals(0, YearLog.currentStreak(days, 13))
        assertEquals(0, YearLog.longestStreak(emptySet()))
    }

    @Test fun badgesFollowLongestStreak() {
        val b = YearLog.badges(LogKind.QURAN, 35)
        assertEquals(listOf(true, true, false, false), b.map { it.earned })
    }

    @Test fun monthlyChallengeCountsOnlyThisMonth() {
        val today = LocalDate.of(2026, 11, 16)
        val days = (0..3).map { today.minusDays(it.toLong()).toEpochDay() }.toSet() + today.minusDays(40).toEpochDay()
        val c = YearLog.challenge(LogKind.FASTING, days, today)
        assertEquals(4, c.done)
        assertFalse(c.completed)
    }

    @Test fun weekRowSaturdayFirst() {
        assertEquals(0, YearLog.weekRow(LocalDate.of(2026, 11, 14))) // Saturday
        assertEquals(1, YearLog.weekRow(LocalDate.of(2026, 11, 15))) // Sunday
        assertEquals(6, YearLog.weekRow(LocalDate.of(2026, 11, 20))) // Friday
    }
}
