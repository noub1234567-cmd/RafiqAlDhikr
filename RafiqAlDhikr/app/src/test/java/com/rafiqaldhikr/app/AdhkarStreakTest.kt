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
}
