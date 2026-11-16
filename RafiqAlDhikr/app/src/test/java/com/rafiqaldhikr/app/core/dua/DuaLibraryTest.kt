package com.rafiqaldhikr.app.core.dua

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class DuaLibraryTest {
    @Test fun sixSectionsWithUniqueIds() {
        val ids = DuaLibrary.sections.map { it.id }
        assertEquals(6, ids.size)
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun lookupById() {
        assertNotNull(DuaLibrary.byId(DuaLibrary.ISTIGHFAR))
        assertNull(DuaLibrary.byId("missing"))
        assertNull(DuaLibrary.byId(null))
    }

    @Test fun itemCountSumsGroups() {
        val s = DuaSectionDef(
            "x", "t", "s",
            listOf(DuaGroup("a", listOf(DuaItem("1", "x"), DuaItem("2", "y"))), DuaGroup("b", listOf(DuaItem("3", "z"))))
        )
        assertEquals(3, s.itemCount)
    }
}
