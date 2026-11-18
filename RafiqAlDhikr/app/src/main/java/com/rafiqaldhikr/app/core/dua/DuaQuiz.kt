package com.rafiqaldhikr.app.core.dua

import kotlin.random.Random

/** "Complete the dhikr": the first words are shown and the next words must be picked from four options. */
data class QuizQuestion(
    val prefix: String,
    val options: List<String>,
    val correctIndex: Int,
    val fullText: String,
    val source: String,
    val title: String
)

/** Builds quiz rounds from the app's own texts, so nothing is ever written ad hoc. */
object DuaQuiz {
    const val PREFIX_WORDS = 4
    const val ANSWER_WORDS = 4
    const val MIN_WORDS = PREFIX_WORDS + ANSWER_WORDS

    val DEFAULT_SECTIONS = listOf(
        DuaLibrary.LIBRARY, DuaLibrary.PROPHET, DuaLibrary.QURAN, DuaLibrary.PRAISE, DuaLibrary.ISTIGHFAR
    )

    private fun words(text: String) = text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }

    private class Candidate(val group: DuaGroup, val item: DuaItem, val words: List<String>) {
        val answer: String get() = words.subList(PREFIX_WORDS, MIN_WORDS).joinToString(" ")
        val prefix: String get() = words.subList(0, PREFIX_WORDS).joinToString(" ")
    }

    fun generate(
        sections: List<DuaSectionDef>,
        ids: List<String> = DEFAULT_SECTIONS,
        count: Int = 10,
        random: Random = Random.Default
    ): List<QuizQuestion> {
        val pool = sections.filter { it.id in ids }
            .flatMap { s -> s.groups.flatMap { g -> g.items.map { Candidate(g, it, words(it.text)) } } }
            .filter { it.words.size >= MIN_WORDS }
        if (pool.size < 4) return emptyList()
        val out = ArrayList<QuizQuestion>()
        for (c in pool.shuffled(random)) {
            if (out.size >= count) break
            val plainAnswer = DuaLibrary.plain(c.answer)
            val distractors = LinkedHashMap<String, String>()
            for (o in pool.shuffled(random)) {
                if (distractors.size == 3) break
                val p = DuaLibrary.plain(o.answer)
                if (o !== c && p != plainAnswer && p !in distractors) distractors[p] = o.answer
            }
            if (distractors.size < 3) continue
            val options = (distractors.values + c.answer).shuffled(random)
            out.add(
                QuizQuestion(
                    prefix = c.prefix + " …",
                    options = options,
                    correctIndex = options.indexOf(c.answer),
                    fullText = c.item.text,
                    source = c.item.source,
                    title = DuaLibrary.saveTitle(c.group, c.item)
                )
            )
        }
        return out
    }
}
