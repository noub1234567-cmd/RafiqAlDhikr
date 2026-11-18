package com.rafiqaldhikr.app.core.jumua

import android.content.Context
import java.time.DayOfWeek
import java.time.LocalDate

data class JumuaItem(val key: String, val label: String)

/** The Friday checklist: tick what you did today. Stored on the device, one set per profile and Friday. */
object JumuaChecklist {
    val items: List<JumuaItem> = listOf(
        JumuaItem("ghusl", "الاغتسال"),
        JumuaItem("tayyib", "التطيّب ولبس أحسن الثياب"),
        JumuaItem("bakkir", "التبكير إلى صلاة الجمعة"),
        JumuaItem("kahf", "قراءة سورة الكهف"),
        JumuaItem("salawat", "الإكثار من الصلاة على النبي ﷺ"),
        JumuaItem("dua", "الدعاء في آخر ساعة قبل المغرب"),
        JumuaItem("sadaqa", "الصدقة")
    )

    fun isFriday(date: LocalDate): Boolean = date.dayOfWeek == DayOfWeek.FRIDAY

    /** The Friday the checklist refers to: today when it is Friday, otherwise the latest one. */
    fun fridayFor(date: LocalDate): LocalDate {
        var d = date
        while (!isFriday(d)) d = d.minusDays(1)
        return d
    }

    private const val FILE = "jumua_checklist"
    private fun key(profileId: Long, friday: LocalDate) = "p${profileId}_${friday.toEpochDay()}"

    fun checked(context: Context, profileId: Long, friday: LocalDate): Set<String> =
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
            .getStringSet(key(profileId, friday), emptySet()).orEmpty().toSet()

    fun toggle(context: Context, profileId: Long, friday: LocalDate, itemKey: String): Set<String> {
        val now = checked(context, profileId, friday).let { if (itemKey in it) it - itemKey else it + itemKey }
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putStringSet(key(profileId, friday), now).apply()
        return now
    }
}
