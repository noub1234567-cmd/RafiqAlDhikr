package com.rafiqaldhikr.app.core.dua

import com.rafiqaldhikr.app.core.memorization.ReviewScheduler

/** Spaced repetition for memorized du'as, using the same intervals as Quran review. */
object DuaMemoSchedule {
    data class Next(val stage: Int, val dueEpochDay: Long)

    /**
     * New state after a review on [today]. "Hard" goes back to the first interval; a second success on the
     * same day does not advance the stage.
     */
    fun afterReview(stage: Int, lastEpochDay: Long, remembered: Boolean, today: Long): Next {
        val max = ReviewScheduler.INTERVALS.size - 1
        if (!remembered) return Next(0, today + ReviewScheduler.INTERVALS[0])
        val next = if (lastEpochDay == today) stage.coerceIn(0, max) else (stage + 1).coerceIn(0, max)
        return Next(next, today + ReviewScheduler.INTERVALS[next])
    }
}
