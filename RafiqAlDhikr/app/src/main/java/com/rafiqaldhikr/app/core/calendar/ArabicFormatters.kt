package com.rafiqaldhikr.app.core.calendar

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ArabicFormatters {

    private val gregorianMonthsAr = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    private val weekdaysAr = mapOf(
        DayOfWeek.SATURDAY to "السبت",
        DayOfWeek.SUNDAY to "الأحد",
        DayOfWeek.MONDAY to "الإثنين",
        DayOfWeek.TUESDAY to "الثلاثاء",
        DayOfWeek.WEDNESDAY to "الأربعاء",
        DayOfWeek.THURSDAY to "الخميس",
        DayOfWeek.FRIDAY to "الجمعة"
    )

    fun toArabicDigits(value: Int): String = toArabicDigits(value.toString())

    fun toArabicDigits(value: Long): String = toArabicDigits(value.toString())

    fun toArabicDigits(input: String): String {
        val arabicZero = '٠'
        val sb = StringBuilder(input.length)
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append((arabicZero.code + (ch - '0')).toChar())
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatGregorianDate(date: LocalDate): String {
        val dayName = weekdaysAr[date.dayOfWeek] ?: ""
        val monthName = gregorianMonthsAr.getOrElse(date.monthValue - 1) { "" }
        return "$dayName، ${toArabicDigits(date.dayOfMonth)} $monthName ${toArabicDigits(date.year)} م"
    }

    fun formatGregorianShort(date: LocalDate): String {
        val monthName = gregorianMonthsAr.getOrElse(date.monthValue - 1) { "" }
        return "${toArabicDigits(date.dayOfMonth)} $monthName ${toArabicDigits(date.year)}"
    }

    fun formatTime12Ar(hour24: Int, minute: Int): String {
        val period = if (hour24 < 12) "ص" else "م"
        val h12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val minStr = minute.toString().padStart(2, '0')
        return "${toArabicDigits(h12)}:${toArabicDigits(minStr)} $period"
    }

    fun formatInstantDateTime(epochMillis: Long): String {
        val dt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDateTime()
        return "${formatGregorianShort(dt.toLocalDate())} • ${formatTime12Ar(dt.hour, dt.minute)}"
    }

    fun weekdayNameAr(dayOfWeek: DayOfWeek): String = weekdaysAr[dayOfWeek] ?: ""

    fun greetingForHour(hour: Int = LocalTime.now().hour): String {
        return when (hour) {
            in 4..11 -> "صباح الخير والبركة"
            in 12..16 -> "طاب يومك بذكر الله"
            in 17..20 -> "مساء الطمأنينة والذكر"
            else -> "ليلة هادئة ومباركة"
        }
    }
}
