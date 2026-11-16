package com.rafiqaldhikr.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AfterPrayerAdhkarTest {
    @Test fun everyPrayerGetsTheCommonAdhkar() {
        for (p in listOf("FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA")) {
            val keys = AfterPrayerAdhkar.forPrayer(p).map { it.key }
            assertTrue(p, keys.containsAll(listOf("astaghfir", "subhan", "hamd", "takbir", "kursi", "muawwidhat")))
        }
    }

    @Test fun prayerSpecificAdhkar() {
        assertTrue(AfterPrayerAdhkar.forPrayer("FAJR").any { it.key == "ilman_nafia" })
        assertFalse(AfterPrayerAdhkar.forPrayer("DHUHR").any { it.key == "ilman_nafia" })
        assertTrue(AfterPrayerAdhkar.forPrayer("MAGHRIB").any { it.key == "tahlil_ten" })
        assertFalse(AfterPrayerAdhkar.forPrayer("ASR").any { it.key == "tahlil_ten" })
    }

    @Test fun muawwidhatAreThriceAfterFajrAndMaghribOnly() {
        val d = AfterPrayerAdhkar.all.first { it.key == "muawwidhat" }
        assertEquals(3, d.countFor("FAJR"))
        assertEquals(3, d.countFor("MAGHRIB"))
        assertEquals(1, d.countFor("ISHA"))
    }

    @Test fun tasbeehIsThirtyThreeEach() {
        for (k in listOf("subhan", "hamd", "takbir")) {
            assertEquals(33, AfterPrayerAdhkar.all.first { it.key == k }.count)
        }
    }

    @Test fun textsAreCleanAndCited() {
        for (d in AfterPrayerAdhkar.all) {
            assertTrue(d.key, d.text.isNotBlank() && d.source.startsWith("حصن المسلم"))
            assertFalse(d.key, d.text.contains('*') || d.text.contains('{') || d.text.contains('(') || d.text.contains("  "))
        }
    }
}
