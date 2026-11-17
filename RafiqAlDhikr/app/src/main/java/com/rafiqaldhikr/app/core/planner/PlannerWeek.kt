package com.rafiqaldhikr.app.core.planner

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/** Fixed worship types so reports can group accurately; "أخرى" covers anything else. */
enum class PlannerType(val labelAr: String) {
    PRAYER("صلاة"),
    QURAN("قرآن"),
    DHIKR("ذكر"),
    DUA("دعاء"),
    CHARITY("صدقة"),
    FASTING("صيام"),
    KNOWLEDGE("علم"),
    KINSHIP("صلة رحم"),
    OTHER("أخرى");

    companion object {
        fun fromKey(key: String): PlannerType = entries.firstOrNull { it.name == key } ?: OTHER
    }
}

/** Week math. A week starts on Saturday; day index 0 = Saturday … 6 = Friday. */
object PlannerWeek {
    const val DAYS = 7
    const val MIN_ROWS = 1
    const val MAX_ROWS = 30
    const val DEFAULT_ROWS = 6
    const val NO_TIME = -1

    val dayNamesAr = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")

    fun weekStart(date: LocalDate): LocalDate {
        val back = (date.dayOfWeek.value - DayOfWeek.SATURDAY.value + 7) % 7
        return date.minusDays(back.toLong())
    }

    fun dayIndexOf(date: LocalDate, weekStart: LocalDate): Int =
        (date.toEpochDay() - weekStart.toEpochDay()).toInt()

    fun clampRows(rows: Int): Int = rows.coerceIn(MIN_ROWS, MAX_ROWS)

    /** Alarm moment of a slot, or null when it has no alarm. */
    fun triggerMillis(
        weekStartEpochDay: Long,
        dayIndex: Int,
        timeMinutes: Int,
        zone: ZoneId = ZoneId.systemDefault()
    ): Long? {
        if (timeMinutes < 0 || dayIndex !in 0 until DAYS) return null
        val date = LocalDate.ofEpochDay(weekStartEpochDay + dayIndex)
        val time = LocalTime.of((timeMinutes / 60).coerceIn(0, 23), (timeMinutes % 60).coerceIn(0, 59))
        return ZonedDateTime.of(date, time, zone).toInstant().toEpochMilli()
    }

    /** The next Saturday 00:01 strictly after [nowMillis]; when the weekly table turns over. */
    fun nextBoundaryMillis(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        var start = weekStart(today)
        var boundary = ZonedDateTime.of(start, LocalTime.of(0, 1), zone).toInstant().toEpochMilli()
        while (boundary <= nowMillis) {
            start = start.plusDays(7)
            boundary = ZonedDateTime.of(start, LocalTime.of(0, 1), zone).toInstant().toEpochMilli()
        }
        return boundary
    }

    fun formatTime(timeMinutes: Int): String =
        if (timeMinutes < 0) "" else "%02d:%02d".format(timeMinutes / 60, timeMinutes % 60)
}

/** One line of a weekly report: how many times a worship was planned and how many were done. */
@kotlinx.serialization.Serializable
data class ReportLine(val title: String, val type: String, val planned: Int, val done: Int)

object PlannerReports {
    private fun normalize(title: String) = title.trim().replace(Regex("\\s+"), " ")

    /** Groups slots by (title, type) as written in the table. Most frequent first. */
    fun summarize(slots: List<Triple<String, String, Boolean>>): List<ReportLine> =
        slots
            .filter { it.first.isNotBlank() }
            .groupBy { normalize(it.first) to it.second }
            .map { (key, items) -> ReportLine(key.first, key.second, items.size, items.count { it.third }) }
            .sortedWith(compareByDescending<ReportLine> { it.planned }.thenBy { it.title })
}
