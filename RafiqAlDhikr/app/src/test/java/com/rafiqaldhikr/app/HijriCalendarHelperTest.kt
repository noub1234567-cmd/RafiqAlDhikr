package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.model.FastingOccasion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

class HijriCalendarHelperTest {

    @Test
    fun `hijri offset shifts day accurately`() {
        val date = LocalDate.of(2026, 3, 20)
        val base = HijriCalendarHelper.toHijri(date, 0)
        val plusOne = HijriCalendarHelper.toHijri(date, 1)
        val minusOne = HijriCalendarHelper.toHijri(date, -1)

        val expectedPlus = HijrahDate.from(date.plusDays(1)).get(ChronoField.DAY_OF_MONTH)
        val expectedMinus = HijrahDate.from(date.minusDays(1)).get(ChronoField.DAY_OF_MONTH)

        assertEquals(expectedPlus, plusOne.day)
        assertEquals(expectedMinus, minusOne.day)
        assertTrue(base.formattedArabic().endsWith("هـ"))
    }

    @Test
    fun `detects monday and thursday fasting`() {
        // 2026-11-16 is a Monday
        val monday = LocalDate.of(2026, 11, 16)
        assertEquals(DayOfWeek.MONDAY, monday.dayOfWeek)
        val occasions = HijriCalendarHelper.getRecommendedFastingOccasions(monday)
        assertTrue(occasions.contains(FastingOccasion.MONDAY_THURSDAY))

        // 2026-11-19 is a Thursday
        val thursday = LocalDate.of(2026, 11, 19)
        assertEquals(DayOfWeek.THURSDAY, thursday.dayOfWeek)
        assertTrue(
            HijriCalendarHelper.getRecommendedFastingOccasions(thursday)
                .contains(FastingOccasion.MONDAY_THURSDAY)
        )
    }

    @Test
    fun `detects white days, arafah, ashura, and eid prohibitions`() {
        // Construct exact Hijri dates via HijrahDate
        val arafahHijri = HijrahDate.of(1447, 12, 9)
        val arafahGregorian = LocalDate.from(arafahHijri)
        assertTrue(
            HijriCalendarHelper.getRecommendedFastingOccasions(arafahGregorian)
                .contains(FastingOccasion.ARAFAH)
        )
        assertFalse(HijriCalendarHelper.isEidProhibitedFastingDay(arafahGregorian))

        val ashuraHijri = HijrahDate.of(1448, 1, 10)
        val ashuraGregorian = LocalDate.from(ashuraHijri)
        assertTrue(
            HijriCalendarHelper.getRecommendedFastingOccasions(ashuraGregorian)
                .contains(FastingOccasion.ASHURA)
        )

        val whiteDayHijri = HijrahDate.of(1448, 3, 14)
        val whiteDayGregorian = LocalDate.from(whiteDayHijri)
        assertTrue(
            HijriCalendarHelper.getRecommendedFastingOccasions(whiteDayGregorian)
                .contains(FastingOccasion.WHITE_DAYS)
        )

        // 13 Dhul-Hijjah is Tashreeq -> prohibited and NOT a white day
        val tashreeq13 = LocalDate.from(HijrahDate.of(1447, 12, 13))
        assertTrue(HijriCalendarHelper.isEidProhibitedFastingDay(tashreeq13))
        assertFalse(
            HijriCalendarHelper.getRecommendedFastingOccasions(tashreeq13)
                .contains(FastingOccasion.WHITE_DAYS)
        )
    }

    @Test
    fun `arabic formatters convert digits and format time cleanly`() {
        assertEquals("١٢٣٠", ArabicFormatters.toArabicDigits(1230))
        assertEquals("٩:٠٥ ص", ArabicFormatters.formatTime12Ar(9, 5))
        assertEquals("٤:٣٠ م", ArabicFormatters.formatTime12Ar(16, 30))
    }
}
