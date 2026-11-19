package com.rafiqaldhikr.app.core.memorization

import org.junit.Assert.assertEquals
import org.junit.Test

class MemoPlanMathTest {
    @Test fun splitsItemsOverDays() {
        assertEquals(3, MemoPlanMath.perDayFor(42, 20)) // 42 on 20 days: at most 3 a day
        assertEquals(1, MemoPlanMath.perDayFor(42, 60))
        assertEquals(21, MemoPlanMath.daysFor(42, 2))
        assertEquals(14, MemoPlanMath.daysFor(42, 3))
        assertEquals(1, MemoPlanMath.daysFor(0, 3))
    }

    @Test fun quotaSpreadsWhatIsLeft() {
        assertEquals(3, MemoPlanMath.quotaToday(42, 20, 0)) // ceil(42/20)
        assertEquals(2, MemoPlanMath.quotaToday(36, 20, 2)) // 18 days left: ceil(36/18)
        assertEquals(0, MemoPlanMath.quotaToday(0, 20, 5))
    }

    @Test fun missedDaysRedistribute() {
        // after 10 days with only 10 learned of 42: 32 left over 10 days -> 4 a day
        assertEquals(4, MemoPlanMath.quotaToday(32, 20, 10))
        // after the end everything left is due today
        assertEquals(7, MemoPlanMath.quotaToday(7, 20, 25))
    }

    @Test fun dayIndexAndDaysLeft() {
        assertEquals(0, MemoPlanMath.dayIndex(100, 99))
        assertEquals(5, MemoPlanMath.dayIndex(100, 105))
        assertEquals(1, MemoPlanMath.daysLeft(20, 40))
    }

    @Test fun manualReviewTakesLastLearned() {
        assertEquals(listOf(7, 8, 9), MemoPlanMath.manualReview(listOf(9, 1, 8, 7, 2), 3))
        assertEquals(emptyList<Int>(), MemoPlanMath.manualReview(emptyList(), 5))
    }
}
