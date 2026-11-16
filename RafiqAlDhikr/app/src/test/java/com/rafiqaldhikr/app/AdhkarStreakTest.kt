package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.stats.AdhkarStreak
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AdhkarStreakTest {
    private val today = LocalDate.of(2026, 11, 15)

    private fun entry(id: Long, cat: AdhkarCategory) =
        AdhkarEntryEntity(
            id = id, profileId = 1, category = cat, title = "t$id", arabicText = "x",
            targetCount = 1, sourceReference = "s"
        )

    private fun done(entryId: Long, daysAgo: Long) = AdhkarProgressEntity(
        profileId = 1, adhkarEntryId = entryId, dateEpochDay = today.toEpochDay() - daysAgo,
        currentCount = 1, isCompleted = true
    )

    private val entries = listOf(entry(1, AdhkarCategory.MORNING), entry(2, AdhkarCategory.EVENING))

    @Test
    fun noRequiredCategoriesGivesZero() {
        assertEquals(0, AdhkarStreak.compute(listOf(entry(9, AdhkarCategory.SLEEP)), emptyList(), today))
    }

    @Test
    fun todayInProgressDoesNotBreakStreak() {
        val progress = listOf(1L, 2L).flatMap { id -> listOf(done(id, 1), done(id, 2), done(id, 3)) }
        assertEquals(3, AdhkarStreak.compute(entries, progress, today))
    }

    @Test
    fun completedTodayCountsAndGapStopsTheStreak() {
        val progress = listOf(1L, 2L).flatMap { id -> listOf(done(id, 0), done(id, 1), done(id, 3)) }
        assertEquals(2, AdhkarStreak.compute(entries, progress, today))
    }

    @Test
    fun aDayWithOnlyMorningDoneIsNotComplete() {
        val progress = listOf(done(1, 1), done(1, 2), done(2, 2))
        assertEquals(0, AdhkarStreak.compute(entries, progress, today))
    }

    @Test
    fun streakIgnoresNonCoreTemplateEntriesWhenCoreExists() {
        val core = entry(1, AdhkarCategory.MORNING).copy(templateKey = "morning_kursi")
        val extra = entry(2, AdhkarCategory.MORNING).copy(templateKey = "morning_afini")
        val evening = entry(3, AdhkarCategory.EVENING).copy(templateKey = "evening_kursi")
        val all = listOf(core, extra, evening)
        val progress = listOf(done(1, 0), done(3, 0), done(1, 1), done(3, 1))
        // The Hisn entry (id 2) was never completed, yet the two days count.
        assertEquals(2, AdhkarStreak.compute(all, progress, today))
    }

    @Test
    fun streakFallsBackToAllEntriesWithoutCoreOnes() {
        val a = entry(1, AdhkarCategory.MORNING).copy(templateKey = "morning_afini")
        val b = entry(2, AdhkarCategory.MORNING).copy(templateKey = "morning_hasbiya")
        val progress = listOf(done(1, 0))
        assertEquals(0, AdhkarStreak.compute(listOf(a, b), progress, today))
    }
}
