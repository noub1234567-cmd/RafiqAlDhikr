package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.model.QuranSurahs
import com.rafiqaldhikr.app.core.model.VerifiedAdhkarCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdditionalDomainTests {

    @Test
    fun `quran surah catalog contains all 114 surahs with positive ayah counts`() {
        assertEquals(114, QuranSurahs.all.size)
        assertEquals("الفاتحة", QuranSurahs.byNumber(1).nameAr)
        assertEquals(286, QuranSurahs.byNumber(2).ayahCount)
        assertEquals("الناس", QuranSurahs.byNumber(114).nameAr)
        assertTrue(QuranSurahs.all.all { it.ayahCount > 0 })
    }

    @Test
    fun `verified adhkar catalog includes authentic sources for every item`() {
        assertTrue(VerifiedAdhkarCatalog.defaultEntries.isNotEmpty())
        assertTrue(
            VerifiedAdhkarCatalog.defaultEntries.all {
                it.sourceReference.isNotBlank() && it.arabicText.isNotBlank() && it.targetCount >= 1
            }
        )
    }
}
