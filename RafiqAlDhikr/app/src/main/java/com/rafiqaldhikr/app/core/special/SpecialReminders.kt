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
    WHITE_DAYS("الأيام البيض", "مساء ١٢ و١٣ و١٤ هجرياً: تذكير بصيام الأيام البيض (٨:٠٠ مساءً)", 20, 0)
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
    }
}
