package com.rafiqaldhikr.app.core.dua

import com.rafiqaldhikr.app.core.prayer.Prayer
import java.time.DayOfWeek
import java.time.ZonedDateTime

/** A situation ("after the prayer", "before sleep"…) mapped to chapters of the library. */
data class NowContext(val key: String, val label: String, val chapters: List<Int>)

/** Suggests which adhkar fit the current time of day and prayer times. */
object DuaNow {
    val contexts: List<NowContext> = listOf(
        NowContext("adhan", "عند سماع الأذان", listOf(13)),
        NowContext("after_prayer", "بعد الصلاة", listOf(27)),
        NowContext("iftar", "عند الإفطار", listOf(68)),
        NowContext("morning", "أذكار الصباح", listOf(1)),
        NowContext("evening", "أذكار المساء", listOf(1)),
        NowContext("sleep", "قبل النوم", listOf(2, 29, 30, 31)),
        NowContext("waking", "عند الاستيقاظ", listOf(3)),
        NowContext("friday", "يوم الجمعة: الصلاة على النبي ﷺ", listOf(107)),
        NowContext("wudu", "الوضوء", listOf(6, 7)),
        NowContext("toilet", "دخول الخلاء والخروج منه", listOf(4, 5)),
        NowContext("home_out", "الخروج من البيت", listOf(8)),
        NowContext("home_in", "دخول البيت", listOf(9)),
        NowContext("mosque", "المسجد", listOf(10, 11, 12)),
        NowContext("prayer", "أذكار الصلاة", (18..26).toList()),
        NowContext("istikhara", "الاستخارة", listOf(28)),
        NowContext("distress", "الهم والحزن والكرب", listOf(34, 35, 40, 43, 46)),
        NowContext("debt", "قضاء الدين", listOf(41)),
        NowContext("sin", "الاستغفار والتوبة", listOf(44, 129)),
        NowContext("food", "الطعام والشراب", listOf(69, 70, 71)),
        NowContext("sick", "المرض وعيادة المريض", listOf(49, 51, 124)),
        NowContext("rain", "المطر والريح", listOf(61, 62, 64, 65, 66)),
        NowContext("travel", "السفر", (95..105).toList()),
        NowContext("salawat", "الصلاة على النبي ﷺ", listOf(107)),
        NowContext("tasbih", "التسبيح والتحميد", listOf(130, 131))
    )

    fun byKey(key: String?): NowContext? = contexts.firstOrNull { it.key == key }

    /** The leading chapter number of a group title such as "27. الأذكار بعد السلام"; null when unnumbered. */
    fun chapterNumber(title: String): Int? =
        Regex("^\\s*(\\d+)\\.").find(title)?.groupValues?.get(1)?.toIntOrNull()

    fun groupsFor(library: DuaSectionDef?, ctx: NowContext): List<DuaGroup> {
        if (library == null) return emptyList()
        return library.groups.filter { g -> chapterNumber(g.title)?.let { it in ctx.chapters } == true }
    }

    /**
     * Context keys that fit [now], most specific first. [times] are today's prayer times in the same zone
     * as [now]; when they are unknown the hour of the day decides.
     */
    fun suggest(now: ZonedDateTime, times: Map<Prayer, ZonedDateTime>?): List<String> {
        val out = LinkedHashSet<String>()
        val five = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
        fun window(p: Prayer, fromMin: Long, toMin: Long): Boolean {
            val t = times?.get(p) ?: return false
            return !now.isBefore(t.plusMinutes(fromMin)) && now.isBefore(t.plusMinutes(toMin))
        }
        if (times != null) {
            if (five.any { window(it, -2, 8) }) out.add("adhan")
            if (five.any { window(it, 8, 45) }) out.add("after_prayer")
            if (window(Prayer.MAGHRIB, -30, 5)) out.add("iftar")
            val fajr = times[Prayer.FAJR]
            val dhuhr = times[Prayer.DHUHR]
            val asr = times[Prayer.ASR]
            val isha = times[Prayer.ISHA]
            if (fajr != null && dhuhr != null && !now.isBefore(fajr) && now.isBefore(dhuhr)) out.add("morning")
            if (asr != null && isha != null && !now.isBefore(asr) && now.isBefore(isha.plusHours(2))) out.add("evening")
            if (window(Prayer.FAJR, -90, 30)) out.add("waking")
            if (isha != null && !now.isBefore(isha.plusMinutes(30))) out.add("sleep")
            if (fajr != null && now.isBefore(fajr.minusMinutes(90))) out.add("sleep")
        } else {
            val h = now.hour
            if (h in 5..10) out.add("morning")
            if (h in 15..19) out.add("evening")
            if (h >= 21 || h < 4) out.add("sleep")
            if (h in 4..5) out.add("waking")
        }
        if (now.dayOfWeek == DayOfWeek.FRIDAY) out.add("friday")
        if (out.isEmpty()) out.add("tasbih")
        return out.toList()
    }
}
