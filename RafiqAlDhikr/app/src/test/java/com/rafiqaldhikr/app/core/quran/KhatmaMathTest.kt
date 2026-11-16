package com.rafiqaldhikr.app.core.quran

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class KhatmaMathTest {
    @Test fun totalIsMushafSize() = assertEquals(6236, KhatmaMath.TOTAL_AYAHS)

    @Test fun positionRoundTrip() {
        assertEquals(7, KhatmaMath.readAyahsFor(1, 7))
        assertEquals(7 + 5, KhatmaMath.readAyahsFor(2, 5))
        assertEquals(1 to 7, KhatmaMath.positionOf(7))
        assertEquals(2 to 1, KhatmaMath.positionOf(8))
        assertEquals(114 to 6, KhatmaMath.positionOf(KhatmaMath.TOTAL_AYAHS))
        assertNull(KhatmaMath.positionOf(0))
    }

    @Test fun ayahIsClampedToSurah() = assertEquals(7, KhatmaMath.readAyahsFor(1, 99))

    @Test fun dailyWirdRoundsUp() {
        // 6236 ayahs over 30 days = 207.87 -> 208
        assertEquals(208, KhatmaMath.dailyWirdAyahs(0, 100, 129))
        // last day: everything left must be read today
        assertEquals(50, KhatmaMath.dailyWirdAyahs(6186, 129, 129))
        // past the deadline still counts as one day
        assertEquals(6236, KhatmaMath.dailyWirdAyahs(0, 200, 129))
    }

    @Test fun aheadBehind() {
        // first day of 2: expected 3118
        assertEquals(0, KhatmaMath.aheadBy(3118, 10, 11, 10))
        assertEquals(-3118, KhatmaMath.aheadBy(0, 10, 11, 10))
    }
}
