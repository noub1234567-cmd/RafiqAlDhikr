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
 * The content of the "الأدعية" tab. Each section's texts live in `assets/dua/<id>.json`
 * (a list of groups), so adding content means editing one JSON file and nothing else.
 */
object DuaLibrary {
    const val LIBRARY = "library"
    const val PRAISE = "praise"
    const val ISTIGHFAR = "istighfar"
    const val PROPHET = "prophet"
    const val QURAN = "quran"
    const val CONDITIONS = "conditions"

    /** The six sections without their content, for showing the buttons immediately. */
    val skeleton: List<DuaSectionDef> = listOf(
        DuaSectionDef(LIBRARY, "المكتبة", "أدعية وأذكار المناسبات: الصلاة والبيت والسفر والأحوال"),
        DuaSectionDef(PRAISE, "حمد الله عز وجل والثناء عليه", "حمد الله وتمجيده والثناء عليه"),
        DuaSectionDef(ISTIGHFAR, "الاستغفار والتوبة", "صيغ الاستغفار وفضله"),
        DuaSectionDef(PROPHET, "جوامع أدعية النبي ﷺ", "ما ثبت من دعائه عليه الصلاة والسلام"),
        DuaSectionDef(QURAN, "أدعية من القرآن", "الأدعية الواردة في القرآن الكريم"),
        DuaSectionDef(CONDITIONS, "هدي السلف في عبادة الدعاء", "آداب الدعاء وأسباب الإجابة وموانعها")
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
