package com.rafiqaldhikr.app.core.prayer

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Direction of the Kaaba from a point on Earth. */
object Qibla {
    const val KAABA_LAT = 21.422487
    const val KAABA_LON = 39.826206

    /** Initial great-circle bearing to the Kaaba in degrees clockwise from TRUE north, in 0..360. */
    fun bearingDegrees(latitude: Double, longitude: Double): Double {
        val phi1 = Math.toRadians(latitude)
        val phi2 = Math.toRadians(KAABA_LAT)
        val dLon = Math.toRadians(KAABA_LON - longitude)
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        return (Math.toDegrees(atan2(y, x)) + 360.0) % 360.0
    }

    /** Smallest signed angle (-180..180) turning from [from] to [to]. */
    fun shortestDelta(from: Double, to: Double): Double {
        var d = (to - from) % 360.0
        if (d > 180.0) d -= 360.0
        if (d < -180.0) d += 360.0
        return d
    }
}
