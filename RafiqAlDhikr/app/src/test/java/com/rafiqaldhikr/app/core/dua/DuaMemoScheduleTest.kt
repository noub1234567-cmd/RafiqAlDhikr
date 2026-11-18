package com.rafiqaldhikr.app.core.dua

import com.rafiqaldhikr.app.core.memorization.ReviewScheduler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DuaMemoScheduleTest {
    @Test
    fun successAdvancesAndSchedules() {
        val a = DuaMemoSchedule.afterReview(0, -1, true, 100)
        assertEquals(1, a.stage)
        assertEquals(100L + ReviewScheduler.INTERVALS[1], a.dueEpochDay)
    }

    @Test
    fun hardGoesBackToFirstInterval() {
        val a = DuaMemoSchedule.afterReview(4, 90, false, 100)
        assertEquals(0, a.stage)
        assertEquals(101L, a.dueEpochDay)
    }

    @Test
    fun sameDaySuccessDoesNotAdvance() {
        val a = DuaMemoSchedule.afterReview(2, 100, true, 100)
        assertEquals(2, a.stage)
    }

    @Test
    fun stageIsCapped() {
        val last = ReviewScheduler.INTERVALS.size - 1
        assertEquals(last, DuaMemoSchedule.afterReview(last, 50, true, 100).stage)
    }

    @Test
    fun itemKeyIgnoresTashkeel() {
        assertEquals(DuaLibrary.itemKey("اللَّهُمَّ ارْحَمْنِي"), DuaLibrary.itemKey("اللهم ارحمني"))
        assertNotEquals(DuaLibrary.itemKey("اللهم ارحمني"), DuaLibrary.itemKey("اللهم اغفر لي"))
    }
}
