package com.rafiqaldhikr.app.core.special

import android.content.Context
import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.model.FastingOccasion
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/** Weekly / monthly Sunnah reminders that depend on the calendar rather than on the user's own plan. */
enum class SpecialReminder(val labelAr: String, val descriptionAr: String, val hour: Int, val minute: Int) {
    FRIDAY_KAHF("ورد الجمعة", "صباح كل جمعة: تذكير بسورة الكهف والإكثار من الصلاة على النبي ﷺ (٩:٠٠ صباحاً)", 9, 0),
    MON_THU_FAST("صيام الاثنين والخميس", "مساء الأحد والأربعاء: تذكير بنية صيام الغد (٨:٠٠ مساءً)", 20, 0),
    WHITE_DAYS("الأيام البيض", "مساء ١٢ و١٣ و١٤ هجرياً: تذكير بصيام الأيام البيض (٨:٠٠ مساءً)", 20, 0),

    // New entries are appended: the ordinal is part of the alarm request code.
    ARAFAH_FAST("صيام يوم عرفة", "مساء ٨ ذي الحجة: تذكير بصيام يوم عرفة لغير الحاج (٨:٠٠ مساءً)", 20, 0),
    ASHURA_FAST("صيام عاشوراء", "مساء ٨ و٩ محرم: تذكير بصيام التاسع والعاشر (٨:٠٠ مساءً)", 20, 0),
    SHAWWAL_SIX("ست من شوال", "مساء أول شوال ثم كل أسبوع حتى آخره: تذكير بصيام الست (٨:٠٠ مساءً)", 20, 0)
}

data class SpecialTrigger(val triggerAtMillis: Long, val title: String, val body: String)

object SpecialReminders {
    private const val PREFS = "special_reminders"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context, r: SpecialReminder): Boolean =
        prefs(context).getBoolean(r.name, false)

    fun setEnabled(context: Context, r: SpecialReminder, value: Boolean) {
        prefs(context).edit().putBoolean(r.name, value).apply()
    }

    /** Whether [r] fires on [date] (the reminder day itself, not the day it is about). */
    internal fun firesOn(r: SpecialReminder, date: LocalDate): Boolean = when (r) {
        SpecialReminder.FRIDAY_KAHF -> date.dayOfWeek == DayOfWeek.FRIDAY
        SpecialReminder.MON_THU_FAST ->
            HijriCalendarHelper.getRecommendedFastingOccasions(date.plusDays(1))
                .contains(FastingOccasion.MONDAY_THURSDAY)
        SpecialReminder.WHITE_DAYS ->
            HijriCalendarHelper.getRecommendedFastingOccasions(date.plusDays(1))
                .contains(FastingOccasion.WHITE_DAYS)
        SpecialReminder.ARAFAH_FAST ->
            HijriCalendarHelper.getRecommendedFastingOccasions(date.plusDays(1))
                .contains(FastingOccasion.ARAFAH)
        SpecialReminder.ASHURA_FAST -> {
            val h = HijriCalendarHelper.toHijri(date.plusDays(1), 0)
            h.month == 1 && (h.day == 9 || h.day == 10)
        }
        SpecialReminder.SHAWWAL_SIX -> {
            // The evenings before the 2nd, 9th, 16th and 23rd of Shawwal (the 1st is Eid, when fasting is forbidden).
            val h = HijriCalendarHelper.toHijri(date.plusDays(1), 0)
            h.month == 10 && h.day in listOf(2, 9, 16, 23)
        }
    }

    /** Next firing of [r] strictly after [nowMillis], searching up to ~7 weeks ahead. */
    fun nextTrigger(r: SpecialReminder, nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): SpecialTrigger? {
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0L..50L) {
            val date = today.plusDays(offset)
            if (!firesOn(r, date)) continue
            val at = date.atTime(r.hour, r.minute).atZone(zone).toInstant().toEpochMilli()
            if (at <= nowMillis) continue
            return SpecialTrigger(at, r.labelAr, bodyFor(r, date))
        }
        return null
    }

    private fun bodyFor(r: SpecialReminder, date: LocalDate): String = when (r) {
        SpecialReminder.FRIDAY_KAHF ->
            "يوم الجمعة: يُستحب قراءة سورة الكهف والإكثار من الصلاة على النبي ﷺ."
        SpecialReminder.MON_THU_FAST -> {
            val tomorrow = date.plusDays(1).dayOfWeek
            val name = if (tomorrow == DayOfWeek.MONDAY) "الاثنين" else "الخميس"
            "غداً يوم $name — يُستحب صيامه. انوِ الصيام من الليل إن أردت."
        }
        SpecialReminder.WHITE_DAYS ->
            "غداً من الأيام البيض (١٣ و١٤ و١٥ من الشهر الهجري) — يُستحب صيامها."
        SpecialReminder.ARAFAH_FAST ->
            "غداً يوم عرفة — صيامه لغير الحاج يكفّر سنتين: الماضية والباقية (رواه مسلم)."
        SpecialReminder.ASHURA_FAST ->
            if (HijriCalendarHelper.toHijri(date.plusDays(1), 0).day == 9)
                "غداً التاسع من محرم — يُستحب صيامه مع العاشر."
            else
                "غداً يوم عاشوراء — صيامه يكفّر السنة الماضية (رواه مسلم)."
        SpecialReminder.SHAWWAL_SIX ->
            "من صام رمضان ثم أتبعه ستاً من شوال كان كصيام الدهر (رواه مسلم). انوِ صيام الغد إن أردت."
    }
}
