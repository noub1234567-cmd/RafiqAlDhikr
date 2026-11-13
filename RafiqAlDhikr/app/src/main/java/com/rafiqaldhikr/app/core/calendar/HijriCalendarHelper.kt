package com.rafiqaldhikr.app.core.calendar

import com.rafiqaldhikr.app.core.model.FastingOccasion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class HijriDateParts(
    val year: Int,
    val month: Int,
    val day: Int,
    val monthNameAr: String
) {
    fun formattedArabic(): String = "${ArabicFormatters.toArabicDigits(day)} $monthNameAr ${ArabicFormatters.toArabicDigits(year)} هـ"
}

object HijriCalendarHelper {

    val hijriMonthsAr: List<String> = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    /**
     * Converts a Gregorian [LocalDate] to a calculated Umm al-Qura [HijriDateParts]
     * shifted by [offsetDays] (typically -2..+2) to allow manual adjustment according
     * to local moon sighting.
     */
    fun toHijri(gregorianDate: LocalDate, offsetDays: Int = 0): HijriDateParts {
        val adjusted = gregorianDate.plusDays(offsetDays.toLong())
        val hijrah = HijrahDate.from(adjusted)
        val year = hijrah.get(ChronoField.YEAR)
        val month = hijrah.get(ChronoField.MONTH_OF_YEAR)
        val day = hijrah.get(ChronoField.DAY_OF_MONTH)
        val monthName = hijriMonthsAr.getOrElse(month - 1) { "" }
        return HijriDateParts(
            year = year,
            month = month,
            day = day,
            monthNameAr = monthName
        )
    }

    fun getRecommendedFastingOccasions(
        gregorianDate: LocalDate,
        hijriOffsetDays: Int = 0
    ): List<FastingOccasion> {
        val occasions = mutableListOf<FastingOccasion>()
        val hijri = toHijri(gregorianDate, hijriOffsetDays)

        if (gregorianDate.dayOfWeek == DayOfWeek.MONDAY || gregorianDate.dayOfWeek == DayOfWeek.THURSDAY) {
            occasions.add(FastingOccasion.MONDAY_THURSDAY)
        }

        // White days (13, 14, 15) excluding 13 Dhul-Hijjah (days of Tashreeq)
        if (hijri.day in listOf(13, 14, 15) && !(hijri.month == 12 && hijri.day == 13)) {
            occasions.add(FastingOccasion.WHITE_DAYS)
        }

        // Day of Arafah: 9 Dhul-Hijjah
        if (hijri.month == 12 && hijri.day == 9) {
            occasions.add(FastingOccasion.ARAFAH)
        }

        // Ashura: 10 Muharram (and 9 Muharram noted via UI/optional)
        if (hijri.month == 1 && hijri.day == 10) {
            occasions.add(FastingOccasion.ASHURA)
        }

        // Six days of Shawwal: 2..30 Shawwal (1 Shawwal is Eid al-Fitr)
        if (hijri.month == 10 && hijri.day >= 2) {
            occasions.add(FastingOccasion.SHAWWAL_SIX)
        }

        return occasions
    }

    fun isEidProhibitedFastingDay(gregorianDate: LocalDate, hijriOffsetDays: Int = 0): Boolean {
        val hijri = toHijri(gregorianDate, hijriOffsetDays)
        val eidAlFitr = hijri.month == 10 && hijri.day == 1
        val eidAlAdhaAndTashreeq = hijri.month == 12 && hijri.day in 10..13
        return eidAlFitr || eidAlAdhaAndTashreeq
    }
}
