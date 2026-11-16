package com.rafiqaldhikr.app.core.ramadan

import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.prayer.Prayer
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoUnit

/** Where [today] stands relative to Ramadan. */
sealed interface RamadanStatus {
    val start: LocalDate
    val length: Int

    /** [daysUntil] days before 1 Ramadan, which falls on [start]. */
    data class Before(val daysUntil: Long, override val start: LocalDate, override val length: Int) : RamadanStatus

    /** Day [dayNumber] (1-based) of a Ramadan that began on [start]. */
    data class During(val dayNumber: Int, override val start: LocalDate, override val length: Int) : RamadanStatus
}

enum class FastMarker { IMSAK, IFTAR }

data class NextMarker(val kind: FastMarker, val at: ZonedDateTime)

object RamadanCalendar {
    /** Minutes before Fajr at which the "imsak" precaution is shown. */
    const val IMSAK_MINUTES = 10L

    /** Gregorian date of 1 Ramadan of Hijri [year] (Umm al-Qura), shifted by the user's sighting [offset]. */
    fun startOf(year: Int, offset: Int): LocalDate =
        LocalDate.from(HijrahDate.of(year, 9, 1)).minusDays(offset.toLong())

    fun lengthOf(year: Int): Int = HijrahDate.of(year, 9, 1).lengthOfMonth()

    fun statusFor(today: LocalDate, offset: Int): RamadanStatus {
        val h = HijriCalendarHelper.toHijri(today, offset)
        if (h.month == 9) {
            val start = startOf(h.year, offset)
            return RamadanStatus.During(
                dayNumber = (ChronoUnit.DAYS.between(start, today) + 1).toInt(),
                start = start,
                length = lengthOf(h.year)
            )
        }
        val year = if (h.month < 9) h.year else h.year + 1
        val start = startOf(year, offset)
        return RamadanStatus.Before(ChronoUnit.DAYS.between(today, start), start, lengthOf(year))
    }

    fun imsakOf(times: Map<Prayer, ZonedDateTime>): ZonedDateTime? = times[Prayer.FAJR]?.minusMinutes(IMSAK_MINUTES)

    /**
     * The next fasting boundary: today's imsak, then today's iftar (Maghrib), then tomorrow's imsak.
     * Null when the times are unavailable.
     */
    fun nextMarker(
        now: ZonedDateTime,
        today: Map<Prayer, ZonedDateTime>,
        tomorrow: Map<Prayer, ZonedDateTime>
    ): NextMarker? {
        val imsak = imsakOf(today)
        val iftar = today[Prayer.MAGHRIB]
        if (imsak != null && now.isBefore(imsak)) return NextMarker(FastMarker.IMSAK, imsak)
        if (iftar != null && now.isBefore(iftar)) return NextMarker(FastMarker.IFTAR, iftar)
        return imsakOf(tomorrow)?.let { NextMarker(FastMarker.IMSAK, it) }
    }
}
