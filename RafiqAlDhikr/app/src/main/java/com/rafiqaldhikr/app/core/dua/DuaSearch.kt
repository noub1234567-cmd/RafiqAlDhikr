package com.rafiqaldhikr.app.core.dua

/** One search result: an item with the section and chapter it lives in. */
data class DuaHit(
    val sectionId: String,
    val sectionTitle: String,
    val group: DuaGroup,
    val item: DuaItem
)

/** Search across every section of the du'a library, ignoring tashkeel and alef forms. */
object DuaSearch {
    class Entry(val hit: DuaHit, val head: String, val body: String)

    /** Normalised text of every item, built once and reused for each keystroke. */
    fun buildIndex(sections: List<DuaSectionDef>): List<Entry> = buildList {
        for (s in sections) for (g in s.groups) for (i in g.items) {
            add(
                Entry(
                    DuaHit(s.id, s.title, g, i),
                    head = DuaLibrary.plain(g.title + " " + i.title),
                    body = DuaLibrary.plain(i.text + " " + i.note + " " + i.source)
                )
            )
        }
    }

    /** Items containing every word of [query]; those matching in a title come first. */
    fun search(index: List<Entry>, query: String, limit: Int = 80): List<DuaHit> {
        val terms = DuaLibrary.plain(query).trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return emptyList()
        val titled = ArrayList<DuaHit>()
        val other = ArrayList<DuaHit>()
        for (e in index) {
            if (!terms.all { e.head.contains(it) || e.body.contains(it) }) continue
            if (terms.all { e.head.contains(it) }) titled.add(e.hit) else other.add(e.hit)
        }
        return (titled + other).take(limit)
    }
}
