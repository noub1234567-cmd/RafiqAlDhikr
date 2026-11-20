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

    @Test fun sixteenSectionsWithUniqueIds() {
        val ids = DuaLibrary.skeleton.map { it.id }
        assertEquals(16, ids.size)
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

    @Test fun virtueHasFiftyHadithsEachWithGradingAndVirtue() {
        val items = load(DuaLibrary.VIRTUE).flatMap { it.items }
        assertEquals(50, items.size)
        assertTrue(items.all { it.source.startsWith("التخريج:") && it.note.startsWith("فضل") })
    }

    @Test fun sunanHasFiftySunnahsAndThreeAthar() {
        val groups = load(DuaLibrary.SUNAN)
        assertEquals(7, groups.size)
        val sunnahs = groups.dropLast(1).flatMap { it.items }
        assertEquals(50, sunnahs.size)
        assertTrue(sunnahs.all { it.source.contains("المصدر:") && it.source.contains("الحكم:") && it.note.startsWith("كيفية العمل:") })
        val athar = groups.last().items.filter { it.title.startsWith("الأثر") }
        assertEquals(3, athar.size)
        assertTrue(athar.all { it.source.contains("المصدر:") && it.note.startsWith("الفائدة:") })
    }

    @Test fun nawawiHasFiftyHadithsWithoutLeftovers() {
        val items = load(DuaLibrary.NAWAWI).flatMap { it.items }
        assertEquals(50, items.size)
        assertTrue(items.all { it.title.startsWith("الحديث ") && it.text.isNotBlank() })
        assertTrue(items.none { it.text.contains("جامع العلوم") || it.text.contains("<br>") })
    }

    @Test fun matnsAndNazmAreLoadedWithExpectedSizes() {
        fun n(id: String) = load(id).sumOf { it.items.size }
        assertEquals(61, n(DuaLibrary.N_TUHFA) - 1) // 61 verses plus one commentary item
        assertEquals(154, n(DuaLibrary.N_AJRUMIYYA))
        assertEquals(34, n(DuaLibrary.N_BAYQUNIYYA))
        assertEquals(12, load(DuaLibrary.M_NAWAQID).size)
        assertEquals(5, load(DuaLibrary.M_QAWAID).size)
        for (id in listOf(DuaLibrary.M_QAWAID, DuaLibrary.M_NAWAQID, DuaLibrary.M_USUL, DuaLibrary.M_SHURUT)) {
            assertTrue(id, load(id).all { g -> g.items.isNotEmpty() && g.items.all { it.text.isNotBlank() } })
        }
        for (id in listOf(DuaLibrary.N_AJRUMIYYA, DuaLibrary.N_BAYQUNIYYA)) {
            assertTrue(id, load(id).flatMap { it.items }.all { it.text.contains("\n") })
        }
    }

    @Test fun everyCategorySectionExistsAndNoneIsListedTwice() {
        val all = DuaLibrary.categories.flatMap { it.sectionIds }
        assertEquals(all.size, all.toSet().size)
        assertEquals(DuaLibrary.skeleton.map { it.id }.toSet(), all.toSet())
        assertEquals(4, DuaLibrary.categories.size)
    }
}
