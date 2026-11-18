package com.rafiqaldhikr.app.core.stats

import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.yearlog.LogKind
import com.rafiqaldhikr.app.core.yearlog.YearLog
import com.rafiqaldhikr.app.core.yearlog.YearLogInputs
import com.rafiqaldhikr.app.data.repository.PrayerItem

/** Everything worth knowing about one Saturday-to-Friday week, in one place. */
data class WeekSummary(
    val weekStartEpochDay: Long,
    /** Obligatory prayers ticked, out of [FARD_PER_WEEK]. */
    val fardPrayers: Int,
    /** Voluntary prayers ticked (sunnah, duha, witr…). */
    val sunnahPrayers: Int,
    /** Days with every morning and evening dhikr completed, out of 7. */
    val adhkarDays: Int,
    val quranDays: Int,
    val quranAmount: Double,
    val fastingDays: Int,
    val tasbeehTotal: Int,
    /** Activities (excluding the weekly planner) completed / missed. */
    val activitiesDone: Int,
    val activitiesMissed: Int,
    /** Weekly planner slots done / planned this week. */
    val plannerDone: Int,
    val plannerPlanned: Int
) {
    companion object {
        const val FARD_PER_WEEK = 35
    }
}

object WeeklySummary {
    /**
     * [plannerActivityIds] are the hidden activities the planner mirrors into the log; they are left out of
     * the activity counts because the planner is reported on its own line.
     */
    fun compute(
        weekStartEpochDay: Long,
        input: YearLogInputs,
        plannerActivityIds: Set<Long>,
        plannerDone: Int,
        plannerPlanned: Int
    ): WeekSummary {
        val range = weekStartEpochDay..(weekStartEpochDay + 6)
        val fard = PrayerItem.entries.filter { it.isFard }.map { it.key }.toSet()
        val prayers = input.prayerLogs.filter { it.dateEpochDay in range }
        val adhkarLevels = YearLog.levels(LogKind.ADHKAR, input)
        val quran = input.quranLogs.filter { it.dateEpochDay in range && it.amountRead > 0.0 }
        val logs = input.activityLogs.filter { it.dateEpochDay in range && it.activityId !in plannerActivityIds }
        return WeekSummary(
            weekStartEpochDay = weekStartEpochDay,
            fardPrayers = prayers.filter { it.itemKey in fard }.map { it.dateEpochDay to it.itemKey }.toSet().size,
            sunnahPrayers = prayers.filter { it.itemKey !in fard }.map { it.dateEpochDay to it.itemKey }.toSet().size,
            adhkarDays = range.count { (adhkarLevels[it] ?: 0) >= 4 },
            quranDays = quran.map { it.dateEpochDay }.toSet().size,
            quranAmount = quran.sumOf { it.amountRead },
            fastingDays = input.fastingDays.count { it.isCompleted && it.dateEpochDay in range },
            tasbeehTotal = input.tasbeehLogs.filter { it.dateEpochDay in range }.sumOf { it.count },
            activitiesDone = logs.count { it.status == ActivityStatus.COMPLETED },
            activitiesMissed = logs.count { it.status == ActivityStatus.MISSED },
            plannerDone = plannerDone,
            plannerPlanned = plannerPlanned
        )
    }
}
