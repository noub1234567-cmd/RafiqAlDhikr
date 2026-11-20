package com.rafiqaldhikr.app.core.dua

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One supplication, dhikr or piece of guidance in a library section. */
@Serializable
data class DuaItem(
    val title: String = "",
    val text: String,
    val source: String = "",
    val note: String = ""
)

/** A titled run of items inside a section (a chapter). */
@Serializable
data class DuaGroup(val title: String, val items: List<DuaItem>)

data class DuaSectionDef(
    val id: String,
    val title: String,
    val subtitle: String,
    val groups: List<DuaGroup> = emptyList()
) {
    val itemCount: Int get() = groups.sumOf { it.items.size }
}

/**
 * The content of the "الذكر والعلم" tab. Each section's texts live in `assets/dua/<id>.json`
 * (a list of groups), so adding content means editing one JSON file and nothing else.
 */
object DuaLibrary {
    const val LIBRARY = "library"
    const val PRAISE = "praise"
    const val ISTIGHFAR = "istighfar"
    const val PROPHET = "prophet"
    const val QURAN = "quran"
    const val CONDITIONS = "conditions"
    const val VIRTUE = "virtue"
    const val SUNAN = "sunan"
    const val NAWAWI = "nawawi"
    const val M_QAWAID = "m_qawaid"
    const val M_NAWAQID = "m_nawaqid"
    const val M_USUL = "m_usul"
    const val M_SHURUT = "m_shurut"
    const val M_KASHF = "m_kashf"
    const val M_ASHMAWIYYA = "m_ashmawiyya"
    const val M_AKHDARI = "m_akhdari"
    const val M_AJRUMIYYA = "m_ajrumiyya"
    const val N_TUHFA = "n_tuhfa"
    const val N_AJRUMIYYA = "n_ajrumiyya"
    const val N_BAYQUNIYYA = "n_bayquniyya"

    /** The sections without their content, for showing the buttons immediately. */
    val skeleton: List<DuaSectionDef> = listOf(
        DuaSectionDef(LIBRARY, "المكتبة", "أدعية وأذكار المناسبات: الصلاة والبيت والسفر والأحوال"),
        DuaSectionDef(PRAISE, "حمد الله عز وجل والثناء عليه", "حمد الله وتمجيده والثناء عليه"),
        DuaSectionDef(ISTIGHFAR, "الاستغفار والتوبة", "صيغ الاستغفار وفضله"),
        DuaSectionDef(PROPHET, "جوامع أدعية النبي ﷺ", "ما ثبت من دعائه عليه الصلاة والسلام"),
        DuaSectionDef(QURAN, "أدعية من القرآن", "الأدعية الواردة في القرآن الكريم"),
        DuaSectionDef(CONDITIONS, "هدي السلف في عبادة الدعاء", "آداب الدعاء وأسباب الإجابة وموانعها"),
        DuaSectionDef(VIRTUE, "فضل الدعاء", "أحاديث في فضل الذكر والدعاء مع بيان فضلها وتخريجها"),
        DuaSectionDef(SUNAN, "السنن المهجورة", "سنن نبوية ثابتة قلّ من يعمل بها، مع تخريجها وكيفية العمل بها وآثار الصحابة والتابعين"),
        DuaSectionDef(NAWAWI, "الأربعون النووية", "الأربعون النووية للإمام النووي مع تتمة الخمسين (٥٠ حديثاً)"),
        DuaSectionDef(M_QAWAID, "القواعد الأربع", "للشيخ محمد بن عبد الوهاب رحمه الله"),
        DuaSectionDef(M_NAWAQID, "نواقض الإسلام", "للشيخ محمد بن عبد الوهاب رحمه الله"),
        DuaSectionDef(M_USUL, "الأصول الثلاثة وأدلتها", "للشيخ محمد بن عبد الوهاب رحمه الله"),
        DuaSectionDef(M_SHURUT, "شروط الصلاة وأركانها وواجباتها", "للشيخ محمد بن عبد الوهاب رحمه الله"),
        DuaSectionDef(M_KASHF, "كشف الشبهات", "للشيخ محمد بن عبد الوهاب رحمه الله (ستة عشر فصلاً)"),
        DuaSectionDef(M_ASHMAWIYYA, "متن العشماوية", "في الفقه المالكي لعبد الباري العشماوي"),
        DuaSectionDef(M_AKHDARI, "متن الأخضري في العبادات", "في الفقه المالكي للأخضري"),
        DuaSectionDef(M_AJRUMIYYA, "متن الآجرومية", "في النحو لابن آجروم (غير مشكّل)"),
        DuaSectionDef(N_TUHFA, "تحفة الأطفال", "نظم في أحكام التجويد للجمزوري (٦١ بيتاً) مع شرح مختصر"),
        DuaSectionDef(N_AJRUMIYYA, "نظم الآجرومية", "نظم متن الآجرومية في النحو (١٥٤ بيتاً)"),
        DuaSectionDef(N_BAYQUNIYYA, "المنظومة البيقونية", "نظم في مصطلح الحديث للإمام عمر البيقوني (٣٤ بيتاً)")
    )

    /** A box on the hub that opens the list of sections it holds. */
    data class Category(val id: String, val title: String, val subtitle: String, val sectionIds: List<String>)

    val categories: List<Category> = listOf(
        Category("adhkar", "الأدعية والأذكار", "أذكار المناسبات والأدعية والاستغفار وفضل الدعاء",
            listOf(LIBRARY, PRAISE, ISTIGHFAR, PROPHET, QURAN, VIRTUE, CONDITIONS)),
        Category("hadith", "الأحاديث", "الأربعون النووية والسنن المهجورة", listOf(NAWAWI, SUNAN)),
        Category("matn", "المتون", "متون طالب العلم", listOf(M_QAWAID, M_NAWAQID, M_USUL, M_SHURUT, M_KASHF, M_ASHMAWIYYA, M_AKHDARI, M_AJRUMIYYA)),
        Category("nazm", "النظم", "منظومات للحفظ في التجويد والنحو والحديث", listOf(N_TUHFA, N_AJRUMIYYA, N_BAYQUNIYYA))
    )

    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cache: List<DuaSectionDef>? = null

    fun parseGroups(text: String): List<DuaGroup> = json.decodeFromString(text)

    /** Reads every section's content from the app's assets (cached after the first call). */
    fun sections(context: Context): List<DuaSectionDef> {
        cache?.let { return it }
        return synchronized(this) {
            cache ?: skeleton.map { def ->
                val groups = runCatching {
                    context.applicationContext.assets.open("dua/${def.id}.json").use {
                        parseGroups(it.readBytes().toString(Charsets.UTF_8))
                    }
                }.getOrDefault(emptyList())
                def.copy(groups = groups)
            }.also { cache = it }
        }
    }

    /** A readable title for an item saved to the personal notebook. */
    fun saveTitle(group: DuaGroup, item: DuaItem): String {
        if (item.title.isNotBlank()) return item.title
        val numbered = Regex("^\\d+\\.\\s*(.+)$").find(group.title)
        if (numbered != null) return numbered.groupValues[1]
        if (item.source.isNotBlank()) return "دعاء — ${item.source}"
        val words = item.text.replace(Regex("[()\\[\\]]"), " ").trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return words.take(6).joinToString(" ") + if (words.size > 6) "…" else ""
    }

    /** A stable key for an item, from its text without tashkeel, used to recognise favorites and memorized items. */
    fun itemKey(text: String): String {
        val t = plain(text).trim()
        return "${t.hashCode()}_${t.length}"
    }

    /** The first hemistich of a nazm verse (title "البيت N", two lines) so a review can hide the second; null for other texts. */
    fun verseFirstHalf(title: String, text: String): String? {
        if (!title.startsWith("البيت ")) return null
        val parts = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        return if (parts.size == 2) parts[0] else null
    }

    fun byId(sections: List<DuaSectionDef>, id: String?): DuaSectionDef? = sections.firstOrNull { it.id == id }

    /** Removes tashkeel/tatweel and unifies alef forms so a plain search matches fully vowelled text. */
    fun plain(s: String): String = buildString(s.length) {
        for (ch in s) {
            val c = ch.code
            val skip = c in 0x064B..0x065F || c == 0x0670 || c in 0x06D6..0x06ED || c == 0x0640
            if (skip) continue
            append(
                when (ch) {
                    'أ', 'إ', 'آ', 'ٱ' -> 'ا'
                    'ى' -> 'ي'
                    else -> ch
                }
            )
        }
    }
}
