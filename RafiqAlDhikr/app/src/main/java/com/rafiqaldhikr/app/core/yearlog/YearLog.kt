package com.rafiqaldhikr.app.core.yearlog

import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.repository.PrayerItem
import java.time.LocalDate
import kotlin.math.ceil

/** What the year grid can show. [monthlyTarget] is the "qualifying days" goal of the monthly challenge. */
enum class LogKind(val labelAr: String, val monthlyTarget: Int) {
    PRAYER("الصلاة", 20),
    ADHKAR("الأذكار", 20),
    ACTIVITIES("العبادات", 20),
    QURAN("القرآن", 15),
    FASTING("الصيام", 8),
    TASBEEH("التسبيح", 15)
}

class YearLogInputs(
    val prayerLogs: List<PrayerLogEntity> = emptyList(),
    val adhkarEntries: List<AdhkarEntryEntity> = emptyList(),
    val adhkarProgress: List<AdhkarProgressEntity> = emptyList(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val quranLogs: List<QuranReadingLogEntity> = emptyList(),
    val fastingDays: List<FastingDayEntity> = emptyList(),
    val tasbeehLogs: List<TasbeehLogEntity> = emptyList()
)

data class Badge(val kind: LogKind, val days: Int, val titleAr: String, val earned: Boolean)

data class Challenge(val kind: LogKind, val done: Int, val target: Int) {
    val completed: Boolean get() = done >= target
}

object YearLog {
    /** Streak lengths that earn a badge. */
    val BADGE_DAYS = intArrayOf(7, 30, 100, 365)

    fun levelFromRatio(r: Double): Int = when {
        r <= 0.0 -> 0
        r <= 0.25 -> 1
        r <= 0.5 -> 2
        r <= 0.75 -> 3
        else -> 4
    }

    /** A day counts for streaks and challenges when it reaches this level or higher. */
    fun qualifies(kind: LogKind, level: Int): Boolean = when (kind) {
        LogKind.PRAYER, LogKind.ADHKAR -> level >= 4
        else -> level >= 1
    }

    /** Intensity 0..4 for every day that has any record. Days without a record are absent (= 0). */
    fun levels(kind: LogKind, input: YearLogInputs): Map<Long, Int> = when (kind) {
        LogKind.PRAYER -> {
            val fard = PrayerItem.entries.filter { it.isFard }.map { it.key }.toSet()
            input.prayerLogs.filter { it.itemKey in fard }
                .groupBy { it.dateEpochDay }
                .mapValues { (_, v) -> levelFromRatio(v.map { it.itemKey }.toSet().size / 5.0) }
        }
        LogKind.ADHKAR -> {
            val tracked = input.adhkarEntries
                .filter { it.category == AdhkarCategory.MORNING || it.category == AdhkarCategory.EVENING }
                .map { it.id }.toSet()
            if (tracked.isEmpty()) emptyMap()
            else input.adhkarProgress.filter { it.isCompleted && it.adhkarEntryId in tracked }
                .groupBy { it.dateEpochDay }
                .mapValues { (_, v) -> levelFromRatio(v.map { it.adhkarEntryId }.toSet().size.toDouble() / tracked.size) }
        }
        LogKind.ACTIVITIES -> input.activityLogs.groupBy { it.dateEpochDay }
            .mapValues { (_, v) -> levelFromRatio(v.count { it.status == ActivityStatus.COMPLETED }.toDouble() / v.size) }
            .filterValues { it > 0 }
        LogKind.QURAN -> {
            val perDay = input.quranLogs.groupBy { it.dateEpochDay }.mapValues { (_, v) -> v.sumOf { it.amountRead } }
                .filterValues { it > 0.0 }
            val max = perDay.values.maxOrNull() ?: 0.0
            perDay.mapValues { (_, a) -> ceil(4 * a / max).toInt().coerceIn(1, 4) }
        }
        LogKind.FASTING -> input.fastingDays.filter { it.isCompleted }.associate { it.dateEpochDay to 4 }
        LogKind.TASBEEH -> input.tasbeehLogs.groupBy { it.dateEpochDay }
            .mapValues { (_, v) -> v.sumOf { it.count } }
            .filterValues { it > 0 }
            .mapValues { (_, n) -> when { n >= 1000 -> 4; n >= 300 -> 3; n >= 100 -> 2; else -> 1 } }
    }

    fun qualifyingDays(kind: LogKind, levels: Map<Long, Int>): Set<Long> =
        levels.filterValues { qualifies(kind, it) }.keys

    /** Consecutive qualifying days ending today, or yesterday while today is still in progress. */
    fun currentStreak(days: Set<Long>, today: Long): Int {
        var cursor = if (today in days) today else today - 1
        var n = 0
        while (cursor in days) { n++; cursor-- }
        return n
    }

    fun longestStreak(days: Set<Long>): Int {
        var best = 0
        for (d in days) {
            if ((d - 1) in days) continue
            var n = 0
            var c = d
            while (c in days) { n++; c++ }
            if (n > best) best = n
        }
        return best
    }

    fun badges(kind: LogKind, longest: Int): List<Badge> = BADGE_DAYS.map { d ->
        Badge(kind, d, "${kind.labelAr} $d يوماً", longest >= d)
    }

    fun challenge(kind: LogKind, days: Set<Long>, today: LocalDate): Challenge {
        val from = today.withDayOfMonth(1).toEpochDay()
        return Challenge(kind, days.count { it in from..today.toEpochDay() }, kind.monthlyTarget)
    }

    /** Row of [date] in a Saturday-first week: Saturday 0 … Friday 6. */
    fun weekRow(date: LocalDate): Int = (date.dayOfWeek.value % 7 + 1) % 7
}
