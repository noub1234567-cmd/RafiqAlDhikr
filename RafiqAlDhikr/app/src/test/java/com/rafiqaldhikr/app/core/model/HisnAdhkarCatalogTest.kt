package com.rafiqaldhikr.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HisnAdhkarCatalogTest {
    private val hisn = HisnAdhkarCatalog.entries

    @Test fun keysAreUniqueAcrossAllEntries() {
        val all = VerifiedAdhkarCatalog.defaultEntries.map { it.key }
        assertEquals(all.size, all.toSet().size)
    }

    @Test fun coreSetIsUntouched() {
        assertEquals(23, VerifiedAdhkarCatalog.coreEntries.size)
        assertTrue(VerifiedAdhkarCatalog.coreKeys.containsAll(listOf("morning_kursi", "evening_nas", "sleep_bismika", "wakeup_hamd")))
        assertTrue(hisn.none { it.key in VerifiedAdhkarCatalog.coreKeys })
    }

    @Test fun everyCategoryIsCovered() {
        for (c in listOf(AdhkarCategory.MORNING, AdhkarCategory.EVENING, AdhkarCategory.SLEEP, AdhkarCategory.WAKEUP)) {
            assertTrue(c.name, hisn.any { it.category == c })
        }
    }

    @Test fun textsAreCleanAndCited() {
        for (e in hisn) {
            assertTrue(e.key, e.arabicText.isNotBlank() && e.title.isNotBlank())
            assertTrue(e.key, e.targetCount >= 1)
            assertTrue(e.key, e.sourceReference.startsWith("حصن المسلم"))
            assertFalse(e.key, e.arabicText.contains('*') || e.arabicText.contains('{') || e.arabicText.contains("  "))
            assertFalse(e.key, e.arabicText.contains('('))
        }
    }

    @Test fun eveningWordingIsSwapped() {
        val m = hisn.first { it.key == "morning_ashhaduka" }.arabicText
        val e = hisn.first { it.key == "evening_ashhaduka" }.arabicText
        assertTrue(m.contains("أصبحت") && !m.contains("أمسيت"))
        assertTrue(e.contains("أمسيت") && !e.contains("أصبحت"))
        val ev = hisn.first { it.key == "evening_mulk_lillah" }.arabicText
        assertTrue(ev.contains("هذه الليلة") && !ev.contains("هذا اليوم"))
    }

    @Test fun morningOnlyAndEveningOnlyItems() {
        assertTrue(hisn.any { it.key == "morning_ilman_nafia" })
        assertTrue(hisn.none { it.key == "evening_ilman_nafia" })
        assertTrue(hisn.any { it.key == "evening_kalimat_tammat" })
        assertTrue(hisn.none { it.key == "morning_kalimat_tammat" })
    }
}
