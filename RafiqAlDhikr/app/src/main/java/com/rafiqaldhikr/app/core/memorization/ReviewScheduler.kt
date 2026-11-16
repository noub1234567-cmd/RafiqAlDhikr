package com.rafiqaldhikr.app.core.memorization

import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity

/** A block of consecutive memorized ayahs that is due for review. */
data class ReviewChunk(
    val fromAyah: Int,
    val toAyah: Int,
    /** Epoch day on which it became (or becomes) due. */
    val dueDay: Long,
    /** Number of successful reviews in a row (0 = just memorized or marked hard). */
    val stage: Int
) {
    fun overdueDays(today: Long): Long = today - dueDay
}

/**
 * Spaced repetition without any stored state: everything is derived from the plan's sessions.
 * A revision session whose notes are [HARD_NOTE] sends the blocks it covers back to the first interval.
 */
object ReviewScheduler {
    const val HARD_NOTE = "صعب"

    /** Days to wait after a session at each stage (stage = successful reviews so far). */
    val INTERVALS: IntArray = intArrayOf(1, 3, 7, 14, 30, 60, 120)

    fun chunksOf(plan: MemorizationPlanEntity): List<Pair<Int, Int>> {
        val last = plan.currentAyah.coerceAtMost(plan.endAyah)
        if (last < plan.startAyah) return emptyList()
        val size = plan.versesPerSession.coerceAtLeast(1)
        val out = ArrayList<Pair<Int, Int>>()
        var from = plan.startAyah
        while (from <= last) {
            val to = minOf(from + size - 1, last)
            out.add(from to to)
            from = to + 1
        }
        return out
    }

    private fun stateOf(
        from: Int,
        to: Int,
        sessions: List<MemorizationSessionEntity>
    ): Pair<Long?, Int> {
        var last: Long? = null
        var stage = 0
        sessions
            .filter { it.fromAyah <= to && it.toAyah >= from }
            .sortedWith(compareBy({ it.dateEpochDay }, { it.completedAtMillis }))
            .forEach { s ->
                when {
                    !s.isRevision -> {
                        last = s.dateEpochDay
                        stage = 0
                    }
                    s.notes.trim() == HARD_NOTE -> {
                        last = s.dateEpochDay
                        stage = 0
                    }
                    else -> {
                        // A second review on the same day does not advance the interval.
                        if (last != s.dateEpochDay) stage = minOf(stage + 1, INTERVALS.size - 1)
                        last = s.dateEpochDay
                    }
                }
            }
        return last to stage
    }

    /** Blocks due on or before [today], most overdue first. Blocks never recorded are due today. */
    fun dueChunks(
        plan: MemorizationPlanEntity,
        sessions: List<MemorizationSessionEntity>,
        today: Long
    ): List<ReviewChunk> {
        val mine = sessions.filter { it.planId == plan.id }
        return chunksOf(plan).mapNotNull { (from, to) ->
            val (last, stage) = stateOf(from, to, mine)
            val due = if (last == null) today else last + INTERVALS[stage]
            if (due <= today) ReviewChunk(from, to, due, stage) else null
        }.sortedWith(compareByDescending<ReviewChunk> { it.overdueDays(today) }.thenBy { it.fromAyah })
    }
}
