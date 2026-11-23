package com.rafiqaldhikr.app.core.dua

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MemoTestTest {
    private fun words(prefix: String, n: Int) = (1..n).joinToString(" ") { "${prefix}كلمة${"ابجدهوزحطيكلمنسعفصقرشت"[it % 22]}${"ابجدهوزحطيكلمنسعفصقرشت"[(it * 3) % 22]}ات" }

    private fun section(id: String, title: String, texts: List<Pair<String, String>>) =
        DuaSectionDef(id, title, "", listOf(DuaGroup("باب", texts.map { (t, x) -> DuaItem(title = t, text = x) })))

    private val sections = listOf(
        section(DuaLibrary.NAWAWI, "الأربعون النووية", (1..8).map { "حديث $it" to words("ح$it", 25) }),
        section(DuaLibrary.M_USUL, "الأصول الثلاثة", (1..8).map { "فصل $it" to words("أ$it", 20) }),
        section(DuaLibrary.N_TUHFA, "تحفة الأطفال", (1..12).map { "البيت $it" to (words("ش$it", 5) + "\n" + words("ص$it", 5)) }),
        section(DuaLibrary.LIBRARY, "المكتبة", (1..8).map { "ذكر $it" to words("ذ$it", 9) })
    )
    private val universe = MemoTest.flatten(sections)

    @Test fun longTextHidesFiveOrSixWordsAndKeepsTheRestVisible() {
        val item = universe.first { it.sectionId == DuaLibrary.NAWAWI }
        repeat(20) { seed ->
            val q = MemoTest.complete(item, universe, Random(seed))
            assertNotNull(q)
            q!!
            assertEquals(4, q.options.size)
            val answer = q.options[q.correctIndex]
            assertTrue(answer.split(" ").size in 5..6)
            assertTrue(item.item.text.contains(answer))
            assertTrue(q.prompt.contains("…"))
            assertEquals(4, q.options.map { DuaLibrary.plain(it) }.toSet().size)
        }
    }

    @Test fun verseKeepsTwoOrThreeWordsAtBothEnds() {
        val item = universe.first { it.sectionId == DuaLibrary.N_TUHFA }
        repeat(20) { seed ->
            val q = MemoTest.complete(item, universe, Random(seed))!!
            val shown = q.prompt.split("[ ………… ]")
            assertEquals(2, shown.size)
            assertTrue(shown[0].trim().split(" ").size in 2..3)
            assertTrue(shown[1].trim().split(" ").size in 2..3)
            assertTrue(item.words.joinToString(" ").contains(q.options[q.correctIndex]))
        }
    }

    @Test fun shortAdhkarHideTheStartOrTheEnd() {
        val item = universe.first { it.sectionId == DuaLibrary.LIBRARY }
        val sides = HashSet<Boolean>()
        repeat(30) { seed ->
            val q = MemoTest.complete(item, universe, Random(seed))!!
            sides.add(q.prompt.startsWith("[ "))
            assertEquals(4, q.options.size)
        }
        assertEquals(setOf(true, false), sides)
    }

    @Test fun sectionQuestionsAreNotAskedForAdhkar() {
        val adhkar = universe.first { it.sectionId == DuaLibrary.LIBRARY }
        assertEquals(null, MemoTest.section(adhkar, sections, Random(1)))
        val matn = universe.first { it.sectionId == DuaLibrary.M_USUL }
        val q = MemoTest.section(matn, sections, Random(1))!!
        assertEquals("الأصول الثلاثة", q.options[q.correctIndex])
    }

    @Test fun nextNeedsALaterItemAndAnswersWithItsBeginning() {
        val last = universe.last { it.sectionId == DuaLibrary.M_USUL }
        assertEquals(null, MemoTest.next(last, universe, Random(1)))
        val first = universe.first { it.sectionId == DuaLibrary.M_USUL }
        val q = MemoTest.next(first, universe, Random(1))!!
        assertTrue(first.next!!.text.startsWith(q.options[q.correctIndex].removeSuffix("…").trim()))
    }

    @Test fun nextForAVerseShowsTheWholeVerseAndAsksForTheNextVersesFirstHemistich() {
        val verse = universe.first { it.sectionId == DuaLibrary.N_TUHFA }
        val q = MemoTest.next(verse, universe, Random(2))!!
        assertTrue(q.prompt.contains(verse.verse!![0]) && q.prompt.contains(verse.verse!![1]))
        assertEquals(verse.group.items[1].text.split("\n")[0].trim(), q.options[q.correctIndex])
        assertTrue(q.instruction.contains("البيت"))
    }

    @Test fun generateGivesOneQuestionPerChosenTextWithOneCorrectOption() {
        val scope = universe.filter { it.sectionId == DuaLibrary.NAWAWI }
        val qs = MemoTest.generate(scope, universe, sections, 5, Random(7))
        assertEquals(5, qs.size)
        qs.forEach { q ->
            assertEquals(4, q.options.size)
            assertTrue(q.correctIndex in 0..3)
            assertFalse(q.fullText.isBlank())
        }
        assertEquals(scope.size, MemoTest.generate(scope, universe, sections, -1, Random(7)).size)
    }

    @Test fun realLibraryTextsAlwaysMakeValidQuestions() {
        val dir = java.io.File("src/main/assets/dua")
        if (!dir.isDirectory) return
        val real = DuaLibrary.skeleton.map { def ->
            val f = java.io.File(dir, "${def.id}.json")
            def.copy(groups = if (f.exists()) DuaLibrary.parseGroups(f.readText()) else emptyList())
        }
        val all = MemoTest.flatten(real)
        val qs = MemoTest.generate(all.shuffled(Random(3)).take(150), all, real, -1, Random(5))
        assertTrue(qs.size > 100)
        qs.forEach { q ->
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.map { DuaLibrary.plain(it) }.toSet().size)
        }
    }
}
