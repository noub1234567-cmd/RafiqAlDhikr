package com.rafiqaldhikr.app.core.dua

import com.rafiqaldhikr.app.core.prayer.Prayer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DuaToolsTest {
    private val zone = ZoneId.of("Africa/Algiers")
    private fun at(h: Int, m: Int = 0, date: LocalDate = LocalDate.of(2026, 11, 18)): ZonedDateTime =
        LocalDateTime.of(date, java.time.LocalTime.of(h, m)).atZone(zone)

    private val times = mapOf(
        Prayer.FAJR to at(5, 30), Prayer.SUNRISE to at(7, 0), Prayer.DHUHR to at(12, 30),
        Prayer.ASR to at(15, 15), Prayer.MAGHRIB to at(17, 45), Prayer.ISHA to at(19, 10)
    )

    private val sections = listOf(
        DuaSectionDef(
            "a", "أ", "",
            listOf(
                DuaGroup("1. أذكار", listOf(DuaItem(text = "اللَّهُمَّ بِكَ أَصْبَحْنَا", source = "مسلم"))),
                DuaGroup("27. بعد السلام", listOf(DuaItem(title = "الاستغفار", text = "أَسْتَغْفِرُ اللَّهَ")))
            )
        )
    )

    @Test
    fun searchIgnoresTashkeelAndAlefForms() {
        val idx = DuaSearch.buildIndex(sections)
        assertEquals(1, DuaSearch.search(idx, "اصبحنا").size)
        assertEquals(1, DuaSearch.search(idx, "استغفر الله").size)
        assertEquals(1, DuaSearch.search(idx, "مسلم").size)
        assertTrue(DuaSearch.search(idx, "").isEmpty())
        assertTrue(DuaSearch.search(idx, "غير موجود").isEmpty())
    }

    @Test
    fun titleMatchesComeFirst() {
        val s = listOf(
            DuaSectionDef(
                "a", "أ", "",
                listOf(
                    DuaGroup("g", listOf(DuaItem(title = "x", text = "نص فيه رحمة"), DuaItem(title = "رحمة", text = "y")))
                )
            )
        )
        val hits = DuaSearch.search(DuaSearch.buildIndex(s), "رحمه")
        assertEquals("رحمة", hits.first().item.title)
    }

    @Test
    fun chapterNumbers() {
        assertEquals(27, DuaNow.chapterNumber("27. الأذكار بعد السلام"))
        assertNull(DuaNow.chapterNumber("أذكار"))
        val groups = DuaNow.groupsFor(sections[0], DuaNow.byKey("after_prayer")!!)
        assertEquals(1, groups.size)
    }

    @Test
    fun suggestionsFollowPrayerTimes() {
        assertEquals("adhan", DuaNow.suggest(at(12, 31), times).first())
        assertEquals("after_prayer", DuaNow.suggest(at(13, 0), times).first())
        assertTrue("morning" in DuaNow.suggest(at(8, 0), times))
        assertTrue("evening" in DuaNow.suggest(at(16, 0), times))
        assertTrue("sleep" in DuaNow.suggest(at(22, 0), times))
        assertTrue("iftar" in DuaNow.suggest(at(17, 30), times))
        assertTrue("waking" in DuaNow.suggest(at(5, 0), times))
        assertTrue("friday" in DuaNow.suggest(at(10, 0, LocalDate.of(2026, 11, 20)), times))
    }

    @Test
    fun suggestionsFallBackToHour() {
        assertTrue("morning" in DuaNow.suggest(at(8), null))
        assertTrue("sleep" in DuaNow.suggest(at(23), null))
        assertEquals(listOf("tasbih"), DuaNow.suggest(at(13), null))
    }

    @Test
    fun dailyPickIsStableAndCycles() {
        val a = DuaDaily.pick(sections, listOf("a"), 10)
        assertEquals(a, DuaDaily.pick(sections, listOf("a"), 10))
        assertEquals(DuaDaily.pick(sections, listOf("a"), 0), DuaDaily.pick(sections, listOf("a"), 2))
        assertNull(DuaDaily.pick(sections, listOf("zzz"), 0))
    }
}
