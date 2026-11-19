package com.rafiqaldhikr.app.core.memorization

/** Arithmetic of a memorization plan: how many items to learn today, derived rather than stored. */
object MemoPlanMath {
    const val REVIEW_AUTO = 0
    const val REVIEW_MANUAL = 1

    private fun ceilDiv(a: Int, b: Int): Int = if (b <= 0) a else (a + b - 1) / b

    /** Items per day needed to finish [count] items in [days] days. */
    fun perDayFor(count: Int, days: Int): Int = ceilDiv(count, days.coerceAtLeast(1)).coerceAtLeast(if (count > 0) 1 else 0)

    /** Days needed to finish [count] items at [perDay] items a day. */
    fun daysFor(count: Int, perDay: Int): Int = ceilDiv(count, perDay.coerceAtLeast(1)).coerceAtLeast(1)

    /** 0-based day of the plan on [today]. */
    fun dayIndex(startEpochDay: Long, today: Long): Int = (today - startEpochDay).coerceAtLeast(0).toInt()

    /** Days left including today; never below 1, so a late plan keeps pushing the rest onto today. */
    fun daysLeft(totalDays: Int, dayIndex: Int): Int = (totalDays - dayIndex).coerceAtLeast(1)

    /**
     * Items to learn today: what is still undone spread over the remaining days. Missing a day therefore
     * redistributes the rest instead of breaking the plan.
     */
    fun quotaToday(remaining: Int, totalDays: Int, dayIndex: Int): Int =
        if (remaining <= 0) 0 else ceilDiv(remaining, daysLeft(totalDays, dayIndex))

    /** Manual review: the last [count] learned positions, oldest first. Automatic review uses spaced repetition instead. */
    fun manualReview(donePositions: List<Int>, count: Int): List<Int> =
        donePositions.sorted().takeLast(count.coerceAtLeast(0))
}
