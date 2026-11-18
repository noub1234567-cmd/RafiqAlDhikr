package com.rafiqaldhikr.app.core.dua

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DuaLibraryTest {
    private fun load(id: String): List<DuaGroup> {
        val f = File("src/main/assets/dua/$id.json")
        assertTrue("missing ${f.path}", f.exists())
        return DuaLibrary.parseGroups(f.readText(Charsets.UTF_8))
    }

    @Test fun everySectionHasContent() {
        DuaLibrary.skeleton.forEach { def ->
            val groups = load(def.id)
            assertTrue(def.id, groups.isNotEmpty())
            assertEquals("duplicate group titles in ${def.id}", groups.size, groups.map { it.title }.toSet().size)
            groups.forEach { g ->
                assertTrue("${def.id}/${g.title} empty", g.items.isNotEmpty())
                g.items.forEach { assertTrue("${def.id}/${g.title} blank text", it.text.isNotBlank()) }
            }
        }
    }

    @Test fun quranItemsAllCarryAReference() {
        val items = load(DuaLibrary.QURAN).flatMap { it.items }
        assertEquals(49, items.size)
        assertTrue(items.all { it.source.isNotBlank() })
    }

    @Test fun libraryKeepsAllChapters() {
        assertEquals(131, load(DuaLibrary.LIBRARY).size)
    }

    @Test fun noInvisibleOrStrayMarkers() {
        DuaLibrary.skeleton.forEach { def ->
            val raw = File("src/main/assets/dua/${def.id}.json").readText(Charsets.UTF_8)
            assertTrue(def.id, '﻿' !in raw && 'ـ' !in raw)
        }
    }

    @Test fun plainSearchIgnoresVowelsAndAlefForms() {
        assertEquals("انك انت", DuaLibrary.plain("إِنَّكَ أَنتَ"))
        assertTrue(DuaLibrary.plain("رَبَّنَا آتِنَا").contains(DuaLibrary.plain("ربنا اتنا")))
    }

    @Test fun sixSectionsWithUniqueIds() {
        val ids = DuaLibrary.skeleton.map { it.id }
        assertEquals(6, ids.size)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun lookupById() {
        assertNotNull(DuaLibrary.byId(DuaLibrary.skeleton, DuaLibrary.ISTIGHFAR))
        assertNull(DuaLibrary.byId(DuaLibrary.skeleton, "missing"))
        assertNull(DuaLibrary.byId(DuaLibrary.skeleton, null))
    }

    @Test fun itemCountSumsGroups() {
        val s = DuaSectionDef(
            "x", "t", "s",
            listOf(DuaGroup("a", listOf(DuaItem("1", "x"), DuaItem("2", "y"))), DuaGroup("b", listOf(DuaItem("3", "z"))))
        )
        assertEquals(3, s.itemCount)
    }

    @Test fun saveTitlePrefersChapterNameThenReferenceThenFirstWords() {
        val chapter = DuaGroup("11. دعاء دخول المسجد", emptyList())
        assertEquals("دعاء دخول المسجد", DuaLibrary.saveTitle(chapter, DuaItem(text = "x")))
        val plain = DuaGroup("أدعية قرآنية جامعة", emptyList())
        assertEquals("دعاء — البقرة 201", DuaLibrary.saveTitle(plain, DuaItem(text = "x", source = "البقرة 201")))
        assertEquals("ا ب ج د ه و…", DuaLibrary.saveTitle(plain, DuaItem(text = "((ا ب ج د ه و ز))")))
        assertEquals("عنوان", DuaLibrary.saveTitle(plain, DuaItem(title = "عنوان", text = "x")))
    }
}
