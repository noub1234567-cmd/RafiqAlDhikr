package com.rafiqaldhikr.app.core.dua

import kotlinx.serialization.Serializable
import kotlin.random.Random

/** One question of the memorization test; every text comes from the app's own library. */
data class TestQuestion(
    val kind: Int,
    val instruction: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val fullText: String,
    val title: String,
    val source: String,
    /** [MemoTest.Item.ref] of the text the question is about, to find weak spots later. */
    val ref: String
)

/** A wrong answer kept in the saved report. */
@Serializable
data class WrongAnswer(
    val ref: String,
    val title: String,
    val instruction: String,
    val prompt: String,
    val chosen: String,
    val correct: String
)

/**
 * The test behind "اختبر حفظك": questions are built from the texts the person chose (memorized plan items,
 * picked sections or earlier mistakes), and the wrong options come from the whole library.
 * Three kinds: complete the missing part (placed at random), which section a text is from, and what comes next.
 */
object MemoTest {
    const val KIND_COMPLETE = 0
    const val KIND_SECTION = 1
    const val KIND_NEXT = 2

    /** Text with at least this many words is "long": a run of five or six words is hidden somewhere inside it. */
    const val LONG_WORDS = 14
    private const val MIN_CLOZE_WORDS = 5
    private const val MARK = "[ ………… ]"

    class Item(
        val sectionId: String,
        val sectionTitle: String,
        val group: DuaGroup,
        val index: Int,
        val item: DuaItem
    ) {
        val words: List<String> = item.text.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        val ref: String = DuaLibrary.itemKey(item.text)
        val verse: List<String>? = run {
            if (!item.title.startsWith("البيت ")) null
            else item.text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.takeIf { it.size == 2 }
        }
        val displayTitle: String get() = DuaLibrary.saveTitle(group, item)
        val next: DuaItem? get() = group.items.getOrNull(index + 1)
    }

    fun flatten(sections: List<DuaSectionDef>): List<Item> = sections.flatMap { s ->
        s.groups.flatMap { g -> g.items.mapIndexed { i, d -> Item(s.id, s.title, g, i, d) } }
    }

    private val STOP = setOf(
        "في", "من", "على", "الى", "عن", "مع", "ثم", "او", "ان", "انه", "انها", "ما", "لا", "لم", "لن", "قد", "كل",
        "هو", "هي", "هذا", "هذه", "ذلك", "التي", "الذي", "الذين", "بن", "بعد", "قبل", "حتى", "اذا", "كان", "كانت",
        "عند", "فيه", "فيها", "منه", "منها", "عليه", "عليها", "له", "لها", "به", "بها", "ولا", "وما", "وهو", "وهي"
    )

    internal fun isContent(word: String): Boolean {
        val p = DuaLibrary.plain(word).filter { it.isLetter() }
        return p.length >= 4 && p !in STOP
    }

    /** A start index for a run of [n] words that begins and ends on meaningful words when possible. */
    internal fun pickStart(words: List<String>, n: Int, random: Random): Int? {
        if (words.size < n) return null
        val all = (0..words.size - n).toList()
        val good = all.filter { isContent(words[it]) && isContent(words[it + n - 1]) }
        return (good.ifEmpty { all }).random(random)
    }

    private fun span(words: List<String>, from: Int, n: Int) = words.subList(from, from + n).joinToString(" ")

    private fun candidates(universe: List<Item>, item: Item, random: Random): List<Item> =
        universe.filter { it !== item && it.ref != item.ref }.shuffled(random)
            .sortedBy { if (it.sectionId == item.sectionId) 0 else 1 }

    private fun optionsOf(correct: String, wrong: List<String>, random: Random): Pair<List<String>, Int>? {
        val seen = HashSet<String>().apply { add(DuaLibrary.plain(correct)) }
        val picked = ArrayList<String>()
        for (w in wrong) {
            if (picked.size == 3) break
            if (seen.add(DuaLibrary.plain(w))) picked.add(w)
        }
        if (picked.size < 3) return null
        val all = (picked + correct).shuffled(random)
        return all to all.indexOf(correct)
    }

    private fun question(kind: Int, instruction: String, prompt: String, opts: Pair<List<String>, Int>, full: String, item: Item) =
        TestQuestion(kind, instruction, prompt, opts.first, opts.second, full, item.displayTitle, item.item.source, item.ref)

    /** Hides a part of the text, and asks for it from four options. */
    fun complete(item: Item, universe: List<Item>, random: Random): TestQuestion? {
        val w = item.words
        if (w.size < MIN_CLOZE_WORDS) return null
        val cands = candidates(universe, item, random)
        val verse = item.verse
        val (prompt, correct, wrong) = when {
            verse != null -> {
                val a = 2 + random.nextInt(2)
                val b = 2 + random.nextInt(2)
                val hidden = w.size - a - b
                if (hidden < 3) return null
                val correct = span(w, a, hidden)
                val wrong = cands.mapNotNull { o ->
                    val ow = o.words
                    val h = ow.size - a - b
                    if (o.verse != null && h >= 3 && kotlin.math.abs(h - hidden) <= 2) span(ow, a, h) else null
                }
                Triple(w.take(a).joinToString(" ") + " $MARK " + w.takeLast(b).joinToString(" "), correct, wrong)
            }
            w.size >= LONG_WORDS -> {
                val n = 5 + random.nextInt(2)
                val s = pickStart(w, n, random) ?: return null
                val before = w.take(s).joinToString(" ")
                val after = w.drop(s + n).joinToString(" ")
                val wrong = cands.mapNotNull { o -> pickStart(o.words, n, random)?.let { span(o.words, it, n) } }
                Triple(listOf(before, MARK, after).filter { it.isNotEmpty() }.joinToString(" "), span(w, s, n), wrong)
            }
            else -> {
                val h = ((w.size * 2 + 4) / 5).coerceIn(2, w.size - 2)
                val atStart = random.nextBoolean()
                val correct = if (atStart) span(w, 0, h) else span(w, w.size - h, h)
                val rest = if (atStart) w.drop(h).joinToString(" ") else w.take(w.size - h).joinToString(" ")
                val wrong = cands.filter { it.words.size >= h + 2 }.map { o ->
                    if (atStart) span(o.words, 0, h) else span(o.words, o.words.size - h, h)
                }
                Triple(if (atStart) "$MARK $rest" else "$rest $MARK", correct, wrong)
            }
        }
        val opts = optionsOf(correct, wrong, random) ?: return null
        return question(KIND_COMPLETE, "أكمل الناقص:", prompt, opts, item.item.text, item)
    }

    private fun startOf(item: Item): String = item.verse?.get(0) ?: item.words.take(8).joinToString(" ")

    private fun snippet(item: Item, max: Int): String =
        item.verse?.get(0) ?: if (item.words.size > max) item.words.take(max).joinToString(" ") + " …" else item.item.text.trim()

    /** "What comes next?": the beginning of the text that follows in its chapter. */
    fun next(item: Item, universe: List<Item>, random: Random): TestQuestion? {
        val nxt = item.next ?: return null
        val nextItem = Item(item.sectionId, item.sectionTitle, item.group, item.index + 1, nxt)
        val correct = startOf(nextItem)
        val sameGroup = item.group.items.indices.filter { it != item.index && it != item.index + 1 }
            .map { startOf(Item(item.sectionId, item.sectionTitle, item.group, it, item.group.items[it])) }.shuffled(random)
        val others = candidates(universe, item, random).map { startOf(it) }
        val opts = optionsOf(correct, sameGroup + others, random) ?: return null
        return question(KIND_NEXT, "ما الذي يلي هذا النص؟", snippet(item, 20), opts, item.item.text + "\n\n— ثم —\n" + nxt.text, item)
    }

    private fun categoryOf(sectionId: String) = DuaLibrary.categories.firstOrNull { sectionId in it.sectionIds }?.id

    /** "Which section is this from?" — only for matns, nazm and hadith, where a text belongs to a single place. */
    fun section(item: Item, sections: List<DuaSectionDef>, random: Random): TestQuestion? {
        val cat = categoryOf(item.sectionId)
        if (cat == null || cat == "adhkar") return null
        val others = sections.filter { it.id != item.sectionId && it.itemCount > 0 }.shuffled(random)
            .sortedBy { if (categoryOf(it.id) == cat) 0 else 1 }.map { it.title }
        val opts = optionsOf(item.sectionTitle, others, random) ?: return null
        return question(KIND_SECTION, "من أي قسم هذا النص؟", snippet(item, 14), opts, item.item.text, item)
    }

    /**
     * Builds the questions: one per chosen text ([count] of them, or all when [count] is -1),
     * mixing the three kinds at random and falling back to a kind that is possible for that text.
     */
    fun generate(
        scope: List<Item>,
        universe: List<Item>,
        sections: List<DuaSectionDef>,
        count: Int,
        random: Random = Random.Default
    ): List<TestQuestion> {
        val chosen = scope.shuffled(random).let { if (count > 0) it.take(count) else it }
        return chosen.mapNotNull { item ->
            val r = random.nextInt(10)
            val order = when {
                r < 6 -> listOf(KIND_COMPLETE, KIND_NEXT, KIND_SECTION)
                r < 8 -> listOf(KIND_NEXT, KIND_COMPLETE, KIND_SECTION)
                else -> listOf(KIND_SECTION, KIND_COMPLETE, KIND_NEXT)
            }
            order.firstNotNullOfOrNull { kind ->
                when (kind) {
                    KIND_COMPLETE -> complete(item, universe, random)
                    KIND_NEXT -> next(item, universe, random)
                    else -> section(item, sections, random)
                }
            }
        }
    }
}
