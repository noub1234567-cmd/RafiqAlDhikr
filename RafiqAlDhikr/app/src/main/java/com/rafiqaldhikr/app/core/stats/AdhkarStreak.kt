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

    /**
     * The entries that count toward the daily streak of [category]: the original curated set when present, so
     * adding the long Hisn al-Muslim lists does not make a complete day unreachable. Falls back to every entry of
     * the category when none of the original ones remain (all deleted or replaced by custom ones).
     */
    fun trackedEntries(entries: List<AdhkarEntryEntity>, category: AdhkarCategory): List<AdhkarEntryEntity> {
        val inCategory = entries.filter { it.category == category }
        val core = inCategory.filter { it.templateKey != null && it.templateKey in coreKeys }
        return core.ifEmpty { inCategory }
    }

    private val coreKeys: Set<String> get() = com.rafiqaldhikr.app.core.model.VerifiedAdhkarCatalog.coreKeys

    fun compute(
        entries: List<AdhkarEntryEntity>,
        progress: List<AdhkarProgressEntity>,
        today: LocalDate
    ): Int {
        val required = tracked
            .map { cat -> cat to trackedEntries(entries, cat).map { it.id }.toSet() }
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
