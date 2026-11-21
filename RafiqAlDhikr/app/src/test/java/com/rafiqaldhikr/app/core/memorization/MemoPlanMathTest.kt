package com.rafiqaldhikr.app.core.memorization

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MemoPlanMathTest {
    // epoch day 4 is Monday 1970-01-05; weekday bits: Mon=0 … Sun=6
    private val monday = 4L
    private val all = MemoPlanMath.ALL_DAYS
    private val noFriday = all and (1 shl 4).inv()

    @Test fun splitsItemsOverSessions() {
        assertEquals(3, MemoPlanMath.perDayFor(42, 20))
        assertEquals(1, MemoPlanMath.perDayFor(42, 60))
        assertEquals(21, MemoPlanMath.daysFor(42, 2))
        assertEquals(1, MemoPlanMath.daysFor(0, 3))
    }

    @Test fun weekdaysAndStudyDates() {
        assertEquals(0, MemoPlanMath.weekdayIndex(monday))
        assertEquals(6, MemoPlanMath.weekdayIndex(monday + 6))
        assertEquals(3, MemoPlanMath.weekdayIndex(0)) // 1970-01-01 was a Thursday
        // Monday..Sunday without Friday: the 7th session lands on the second Monday after skipping Friday
        val dates = MemoPlanMath.studyDates(monday, 7, noFriday)
        assertEquals(listOf(0, 1, 2, 3, 5, 6, 0), dates.map { MemoPlanMath.weekdayIndex(it) })
        assertEquals(monday + 7, dates.last())
        assertEquals(monday + 7, MemoPlanMath.endDate(monday, 7, noFriday))
        assertEquals(7, MemoPlanMath.studyDaysBetween(monday, monday + 7, noFriday))
        // an empty mask behaves like every day
        assertEquals(MemoPlanMath.studyDates(monday, 5, all), MemoPlanMath.studyDates(monday, 5, 0))
    }

    @Test fun quotasAlwaysSumToTheCount() {
        for (ramp in listOf(false, true)) for (count in listOf(1, 5, 42, 50, 154)) for (sessions in listOf(1, 3, 20, 60)) {
            val q = MemoPlanMath.quotas(count, sessions, ramp)
            assertEquals(count, q.sum())
            assertEquals(sessions, q.size)
            assertTrue(q.all { it >= 0 })
        }
    }

    @Test fun plainQuotasAreBalanced() {
        val q = MemoPlanMath.quotas(42, 20, false)
        assertTrue(q.max() - q.filter { it > 0 }.min() <= 1)
        assertEquals(3, q[0])
    }

    @Test fun rampStartsLighterThanItEnds() {
        val q = MemoPlanMath.quotas(300, 30, true)
        assertTrue(q[0] < q[10])
        assertTrue(q[0] <= q[7])
        // too short a plan ignores the ramp
        assertEquals(MemoPlanMath.quotas(10, 3, false).toList(), MemoPlanMath.quotas(10, 3, true).toList())
    }

    @Test fun todayQuotaFollowsRestDaysAndCatchesUp() {
        // start Monday, no Friday, 20 sessions, 42 items
        assertEquals(3, MemoPlanMath.quotaToday(42, 20, false, monday, noFriday, monday))
        assertEquals(0, MemoPlanMath.quotaToday(42, 20, false, monday, noFriday, monday + 4)) // Friday: rest
        assertEquals(MemoPlanMath.REST_DAY, MemoPlanMath.state(monday, 20, noFriday, monday + 4))
        assertEquals(0, MemoPlanMath.quotaToday(42, 20, false, monday, noFriday, monday - 1)) // before the start
        // after 10 sessions with only 10 learned: 32 over 10 sessions
        val day = MemoPlanMath.studyDates(monday, 11, noFriday).last()
        assertEquals(4, MemoPlanMath.quotaToday(32, 20, false, monday, noFriday, day))
        // once all sessions have passed, everything left is due
        val after = MemoPlanMath.endDate(monday, 20, all)
        assertEquals(MemoPlanMath.OVER, MemoPlanMath.state(monday, 20, all, monday + 100))
        assertEquals(7, MemoPlanMath.quotaToday(7, 20, false, monday, all, after + 30))
        assertEquals(0, MemoPlanMath.quotaToday(0, 20, false, monday, all, monday))
    }

    @Test fun estimatesAndSuggestion() {
        assertEquals(0, MemoPlanMath.newMinutes(0))
        assertEquals(5, MemoPlanMath.newMinutes(20))
        assertEquals(1, MemoPlanMath.reviewMinutes(10))
        assertEquals(0, MemoPlanMath.peakReviewRounds(0))
        assertEquals(3, MemoPlanMath.peakReviewRounds(7))
        assertEquals(7, MemoPlanMath.peakReviewRounds(200))
        // 3600 words at 4 words/min and 30 min a day -> 30 sessions, never more than the item count
        assertEquals(30, MemoPlanMath.suggestedSessions(3600, 100))
        assertEquals(10, MemoPlanMath.suggestedSessions(3600, 10))
        assertEquals(3, MemoPlanMath.words("  ا ب  ج "))
    }

    @Test fun manualReviewTakesLastLearned() {
        assertEquals(listOf(7, 8, 9), MemoPlanMath.manualReview(listOf(9, 1, 8, 7, 2), 3))
        assertEquals(emptyList<Int>(), MemoPlanMath.manualReview(emptyList(), 5))
    }

    @Test fun pausedPlanAsksNothingAndKeepsRemainingSessions() {
        val start = 20000L
        assertEquals(MemoPlanMath.PAUSED, MemoPlanMath.state(start, 10, 127, start + 3, paused = true))
        assertEquals(0, MemoPlanMath.quotaToday(50, 10, false, start, 127, start + 3, paused = true))
        // 3 of 10 daily sessions gone -> 7 left; before the start all 10 are still ahead
        assertEquals(7, MemoPlanMath.sessionsLeft(start, 10, 127, start + 3))
        assertEquals(10, MemoPlanMath.sessionsLeft(start, 10, 127, start - 2))
        assertEquals(1, MemoPlanMath.sessionsLeft(start, 10, 127, start + 50))
    }
}
