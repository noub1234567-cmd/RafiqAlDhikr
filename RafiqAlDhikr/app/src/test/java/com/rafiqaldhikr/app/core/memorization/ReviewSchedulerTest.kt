package com.rafiqaldhikr.app.core.memorization

import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReviewSchedulerTest {
    private val plan = MemorizationPlanEntity(
        id = 1, profileId = 1, title = "t", surahNumber = 2,
        startAyah = 1, endAyah = 20, currentAyah = 10, versesPerSession = 5
    )

    private fun s(rev: Boolean, from: Int, to: Int, day: Long, notes: String = "", at: Long = day) =
        MemorizationSessionEntity(
            profileId = 1, planId = 1, isRevision = rev, fromAyah = from, toAyah = to,
            dateEpochDay = day, completedAtMillis = at, notes = notes
        )

    @Test fun chunksCoverMemorizedRangeOnly() {
        assertEquals(listOf(1 to 5, 6 to 10), ReviewScheduler.chunksOf(plan))
        assertTrue(ReviewScheduler.chunksOf(plan.copy(currentAyah = 0)).isEmpty())
    }

    @Test fun neverRecordedIsDueToday() {
        assertEquals(2, ReviewScheduler.dueChunks(plan, emptyList(), 100).size)
    }

    @Test fun freshlyMemorizedWaitsOneDay() {
        val sessions = listOf(s(false, 1, 10, 100))
        assertTrue(ReviewScheduler.dueChunks(plan, sessions, 100).isEmpty())
        assertEquals(2, ReviewScheduler.dueChunks(plan, sessions, 101).size)
    }

    @Test fun intervalsGrowAfterEachReview() {
        val sessions = listOf(s(false, 1, 5, 100), s(true, 1, 5, 101), s(true, 1, 5, 104))
        // stage 2 after two reviews -> 7 days after day 104
        assertTrue(ReviewScheduler.dueChunks(plan.copy(currentAyah = 5), sessions, 110).isEmpty())
        val due = ReviewScheduler.dueChunks(plan.copy(currentAyah = 5), sessions, 111)
        assertEquals(1, due.size)
        assertEquals(2, due[0].stage)
    }

    @Test fun hardResetsToFirstInterval() {
        val sessions = listOf(s(false, 1, 5, 100), s(true, 1, 5, 101), s(true, 1, 5, 104), s(true, 1, 5, 120, "صعب"))
        val p = plan.copy(currentAyah = 5)
        assertTrue(ReviewScheduler.dueChunks(p, sessions, 120).isEmpty())
        val due = ReviewScheduler.dueChunks(p, sessions, 121)
        assertEquals(1, due.size)
        assertEquals(0, due[0].stage)
    }

    @Test fun sameDayRepeatDoesNotAdvance() {
        val sessions = listOf(s(false, 1, 5, 100), s(true, 1, 5, 101, at = 1), s(true, 1, 5, 101, at = 2))
        val p = plan.copy(currentAyah = 5)
        // one review -> stage 1 -> 3 days after day 101
        assertTrue(ReviewScheduler.dueChunks(p, sessions, 103).isEmpty())
        assertEquals(1, ReviewScheduler.dueChunks(p, sessions, 104).size)
    }

    @Test fun mostOverdueFirst() {
        val sessions = listOf(s(false, 1, 5, 90), s(false, 6, 10, 99))
        val due = ReviewScheduler.dueChunks(plan, sessions, 105)
        assertEquals(1, due[0].fromAyah)
    }
}
