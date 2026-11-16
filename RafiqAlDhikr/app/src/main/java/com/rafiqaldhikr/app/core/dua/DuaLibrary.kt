package com.rafiqaldhikr.app.core.dua

/** One supplication, dhikr or piece of guidance in a library section. */
data class DuaItem(
    val title: String,
    val text: String,
    val source: String = "",
    val note: String = ""
)

/** A titled run of items inside a section (a chapter). */
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
 * The content of the "الأدعية" tab. Each section is filled from its own list below, so adding content later
 * means appending [DuaGroup]s to one section and nothing else.
 */
object DuaLibrary {
    const val LIBRARY = "library"
    const val PRAISE = "praise"
    const val ISTIGHFAR = "istighfar"
    const val PROPHET = "prophet"
    const val QURAN = "quran"
    const val CONDITIONS = "conditions"

    val sections: List<DuaSectionDef> = listOf(
        DuaSectionDef(LIBRARY, "المكتبة", "أدعية وأذكار المناسبات: الصلاة والبيت والسفر والأحوال"),
        DuaSectionDef(PRAISE, "الحمد والثناء", "حمد الله وتمجيده والثناء عليه"),
        DuaSectionDef(ISTIGHFAR, "الاستغفار", "صيغ الاستغفار وفضله"),
        DuaSectionDef(PROPHET, "أدعية الرسول ﷺ", "ما ثبت من دعائه عليه الصلاة والسلام"),
        DuaSectionDef(QURAN, "أدعية من القرآن", "الأدعية الواردة في القرآن الكريم"),
        DuaSectionDef(CONDITIONS, "شروط الدعاء وكيفيته", "آداب الدعاء وأسباب الإجابة وموانعها")
    )

    fun byId(id: String?): DuaSectionDef? = sections.firstOrNull { it.id == id }
}
