package com.rafiqaldhikr.app.core.memorization

import kotlin.math.ceil

/**
 * Arithmetic of a memorization plan. A plan has [sessions] study days (not calendar days): only the weekdays in
 * the study-days mask count, an optional gentle start ramps the load up, and what is still undone is always spread
 * over the sessions that remain, so a missed day redistributes instead of breaking the plan.
 */
object MemoPlanMath {
    const val REVIEW_AUTO = 0
    const val REVIEW_MANUAL = 1

    /** Bit i of the study-days mask is weekday i with Monday = 0 … Sunday = 6. */
    const val ALL_DAYS = 127

    /** Rough speeds used for the time estimates, in words per minute. */
    const val NEW_WORDS_PER_MINUTE = 4.0
    const val REVIEW_WORDS_PER_MINUTE = 30.0

    /** Daily time (minutes) above which the plan is flagged as too heavy. */
    const val HEAVY_MINUTES = 60

    /** Daily time (minutes) the suggested duration aims at for the new material. */
    const val TARGET_NEW_MINUTES = 30

    /** Review intervals of automatic review, in days (same as ReviewScheduler.INTERVALS). */
    private val REVIEW_INTERVALS = intArrayOf(1, 3, 7, 14, 30, 60, 120)

    private fun ceilDiv(a: Int, b: Int): Int = if (b <= 0) a else (a + b - 1) / b

    fun normalizeMask(mask: Int): Int = if (mask and ALL_DAYS == 0) ALL_DAYS else mask and ALL_DAYS

    /** Monday = 0 … Sunday = 6 (epoch day 0 was a Thursday). */
    fun weekdayIndex(epochDay: Long): Int = Math.floorMod(epochDay + 3, 7L).toInt()

    fun isStudyDay(mask: Int, epochDay: Long): Boolean = (normalizeMask(mask) shr weekdayIndex(epochDay)) and 1 == 1

    /** Items per session needed to finish [count] items in [sessions] sessions (at least 1 when there is something to learn). */
    fun perDayFor(count: Int, sessions: Int): Int = ceilDiv(count, sessions.coerceAtLeast(1)).coerceAtLeast(if (count > 0) 1 else 0)

    /** Sessions needed to finish [count] items at [perDay] items a session. */
    fun daysFor(count: Int, perDay: Int): Int = ceilDiv(count, perDay.coerceAtLeast(1)).coerceAtLeast(1)

    /** Calendar date of every session, first to last. */
    fun studyDates(startEpochDay: Long, sessions: Int, mask: Int): List<Long> {
        val out = ArrayList<Long>(sessions.coerceAtLeast(0))
        var d = startEpochDay
        while (out.size < sessions) {
            if (isStudyDay(mask, d)) out.add(d)
            d++
        }
        return out
    }

    fun endDate(startEpochDay: Long, sessions: Int, mask: Int): Long =
        studyDates(startEpochDay, sessions.coerceAtLeast(1), mask).last()

    /** Number of study days in [startEpochDay, endEpochDay] inclusive. */
    fun studyDaysBetween(startEpochDay: Long, endEpochDay: Long, mask: Int): Int {
        var n = 0
        var d = startEpochDay
        while (d <= endEpochDay) {
            if (isStudyDay(mask, d)) n++
            d++
        }
        return n
    }

    /** Relative load of each session: 1.0 everywhere, or a gentle first week rising from half to full load. */
    fun weights(sessions: Int, ramp: Boolean): DoubleArray {
        val w = DoubleArray(sessions.coerceAtLeast(0)) { 1.0 }
        if (!ramp || sessions < 4) return w
        val r = minOf(7, sessions / 3).coerceAtLeast(1)
        for (i in 0 until r) w[i] = 0.5 + 0.5 * i / r
        return w
    }

    private fun share(remaining: Int, weights: DoubleArray, index: Int): Int {
        if (remaining <= 0) return 0
        if (index >= weights.size) return remaining
        var sum = 0.0
        for (i in index until weights.size) sum += weights[i]
        return ceil(remaining * weights[index] / sum - 1e-9).toInt().coerceIn(1, remaining)
    }

    /** The planned amount of every session, simulated on the same rule that [quotaToday] applies each day. */
    fun quotas(count: Int, sessions: Int, ramp: Boolean): IntArray {
        val w = weights(sessions, ramp)
        val out = IntArray(w.size)
        var rem = count
        for (i in w.indices) {
            out[i] = share(rem, w, i)
            rem -= out[i]
        }
        return out
    }

    /** Sessions already past before [today] (study days in [start, today)). */
    fun sessionsElapsed(startEpochDay: Long, mask: Int, today: Long): Int =
        if (today <= startEpochDay) 0 else studyDaysBetween(startEpochDay, today - 1, mask)

    const val NOT_STARTED = 0
    const val REST_DAY = 1
    const val ACTIVE = 2
    const val OVER = 3

    /** Where [today] stands in the plan. */
    fun state(startEpochDay: Long, sessions: Int, mask: Int, today: Long): Int = when {
        today < startEpochDay -> NOT_STARTED
        !isStudyDay(mask, today) -> REST_DAY
        sessionsElapsed(startEpochDay, mask, today) >= sessions -> OVER
        else -> ACTIVE
    }

    /**
     * Items to learn today: what is still undone, spread by weight over the sessions that remain from today.
     * Zero before the start and on rest days; everything that is left once the plan has run out.
     */
    fun quotaToday(remaining: Int, sessions: Int, ramp: Boolean, startEpochDay: Long, mask: Int, today: Long): Int {
        if (remaining <= 0) return 0
        return when (state(startEpochDay, sessions, mask, today)) {
            NOT_STARTED, REST_DAY -> 0
            OVER -> remaining
            else -> share(remaining, weights(sessions, ramp), sessionsElapsed(startEpochDay, mask, today))
        }
    }

    /** Words in a text, for the time estimates. */
    fun words(text: String): Int = text.split(Regex("\\s+")).count { it.isNotBlank() }

    fun newMinutes(words: Int): Int = if (words <= 0) 0 else ceil(words / NEW_WORDS_PER_MINUTE).toInt()

    fun reviewMinutes(words: Int): Int = if (words <= 0) 0 else ceil(words / REVIEW_WORDS_PER_MINUTE).toInt()

    /** How many review rounds are due on a single day once the plan has run for [spanDays] calendar days. */
    fun peakReviewRounds(spanDays: Int): Int = REVIEW_INTERVALS.count { it <= spanDays }

    /** Sessions that bring the daily new material down to about [TARGET_NEW_MINUTES] minutes. */
    fun suggestedSessions(totalWords: Int, itemCount: Int): Int =
        ceil(totalWords / (NEW_WORDS_PER_MINUTE * TARGET_NEW_MINUTES)).toInt().coerceIn(1, itemCount.coerceAtLeast(1))

    /** Manual review: the last [count] learned positions, oldest first. Automatic review uses spaced repetition instead. */
    fun manualReview(donePositions: List<Int>, count: Int): List<Int> =
        donePositions.sorted().takeLast(count.coerceAtLeast(0))
}
