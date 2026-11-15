package com.rafiqaldhikr.app.core.stats

import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import java.time.LocalDate

/**
 * Consecutive days on which every morning AND evening dhikr entry was completed.
 * Today does not break the streak while it is still in progress: counting starts from yesterday
 * if today is not complete yet.
 */
object AdhkarStreak {
    private val tracked = listOf(AdhkarCategory.MORNING, AdhkarCategory.EVENING)

    fun compute(
        entries: List<AdhkarEntryEntity>,
        progress: List<AdhkarProgressEntity>,
        today: LocalDate
    ): Int {
        val required = tracked
            .map { cat -> cat to entries.filter { it.category == cat }.map { it.id }.toSet() }
            .filter { it.second.isNotEmpty() }
        if (required.isEmpty()) return 0

        val completedByDay: Map<Long, Set<Long>> = progress
            .filter { it.isCompleted }
            .groupBy({ it.dateEpochDay }, { it.adhkarEntryId })
            .mapValues { it.value.toSet() }

        fun dayComplete(day: Long): Boolean {
            val done = completedByDay[day] ?: return false
            return required.all { (_, ids) -> done.containsAll(ids) }
        }

        val todayEpoch = today.toEpochDay()
        var cursor = if (dayComplete(todayEpoch)) todayEpoch else todayEpoch - 1
        var streak = 0
        while (dayComplete(cursor)) {
            streak++
            cursor--
        }
        return streak
    }
}
