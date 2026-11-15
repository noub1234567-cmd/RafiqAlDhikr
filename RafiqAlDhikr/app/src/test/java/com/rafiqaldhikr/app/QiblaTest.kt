package com.rafiqaldhikr.app

import com.rafiqaldhikr.app.core.prayer.Qibla
import org.junit.Assert.assertEquals
import org.junit.Test

class QiblaTest {
    @Test
    fun bearingsMatchKnownValues() {
        // Well-known approximate great-circle qibla bearings from true north.
        assertEquals(119.0, Qibla.bearingDegrees(48.8566, 2.3522), 2.0)   // Paris
        assertEquals(136.0, Qibla.bearingDegrees(30.0444, 31.2357), 2.0)  // Cairo
        assertEquals(151.6, Qibla.bearingDegrees(41.0082, 28.9784), 1.5) // Istanbul
        assertEquals(105.4, Qibla.bearingDegrees(36.7538, 3.0588), 1.0)   // Algiers
    }

    @Test
    fun shortestDeltaWrapsAround() {
        assertEquals(20.0, Qibla.shortestDelta(350.0, 10.0), 1e-9)
        assertEquals(-20.0, Qibla.shortestDelta(10.0, 350.0), 1e-9)
    }
}
