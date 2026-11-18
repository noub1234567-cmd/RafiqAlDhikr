package com.rafiqaldhikr.app.core.dua

/** The hadith and the du'a of the day: the same pick all day, changing at midnight. */
object DuaDaily {
    data class Pick(val group: DuaGroup, val item: DuaItem)

    /** Deterministic pick from the given sections' items by [epochDay]. */
    fun pick(sections: List<DuaSectionDef>, ids: List<String>, epochDay: Long): Pick? {
        val all = sections.filter { it.id in ids }.flatMap { s -> s.groups.flatMap { g -> g.items.map { Pick(g, it) } } }
        if (all.isEmpty()) return null
        return all[Math.floorMod(epochDay, all.size.toLong()).toInt()]
    }

    val HADITH_IDS = listOf(DuaLibrary.VIRTUE)
    val DUA_IDS = listOf(DuaLibrary.PROPHET, DuaLibrary.QURAN)
}
