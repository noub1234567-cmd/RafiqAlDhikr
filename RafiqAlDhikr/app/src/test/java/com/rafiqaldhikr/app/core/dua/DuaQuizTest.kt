package com.rafiqaldhikr.app.core.dua

import java.time.LocalDate
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import com.rafiqaldhikr.app.core.jumua.JumuaChecklist

class DuaQuizTest {
    private fun section(n: Int) = DuaSectionDef(
        "library", "م", "",
        listOf(DuaGroup("g", (1..n).map { DuaItem(text = "كلمة$it ثانية$it ثالثة$it رابعة$it خامسة$it سادسة$it سابعة$it ثامنة$it تاسعة$it") }))
    )

    @Test
    fun questionsHaveFourDistinctOptionsAndCorrectOneIsTheContinuation() {
        val qs = DuaQuiz.generate(listOf(section(12)), count = 5, random = Random(1))
        assertEquals(5, qs.size)
        for (q in qs) {
            assertEquals(4, q.options.size)
            assertEquals(4, q.options.toSet().size)
            val words = q.fullText.split(" ")
            assertEquals(words.subList(4, 8).joinToString(" "), q.options[q.correctIndex])
            assertTrue(q.prefix.startsWith(words.subList(0, 4).joinToString(" ")))
        }
    }

    @Test
    fun tooFewItemsGivesNoQuestions() {
        assertTrue(DuaQuiz.generate(listOf(section(3))).isEmpty())
        assertTrue(DuaQuiz.generate(listOf(section(12)), ids = listOf("none")).isEmpty())
    }

    @Test
    fun fridayLogic() {
        val friday = LocalDate.of(2026, 11, 20)
        assertTrue(JumuaChecklist.isFriday(friday))
        assertEquals(friday, JumuaChecklist.fridayFor(friday))
        assertEquals(friday, JumuaChecklist.fridayFor(friday.plusDays(3)))
        assertEquals(friday, JumuaChecklist.fridayFor(friday.plusDays(6)))
    }
}
