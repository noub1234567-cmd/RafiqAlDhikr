package com.rafiqaldhikr.app.core.prayer

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

enum class Prayer(val labelAr: String) {
    FAJR("الفجر"),
    SUNRISE("الشروق"),
    DHUHR("الظهر"),
    ASR("العصر"),
    MAGHRIB("المغرب"),
    ISHA("العشاء")
}

enum class CalcMethod(
    val labelAr: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    /** When > 0, Isha is this many minutes after Maghrib instead of using [ishaAngle]. */
    val ishaIntervalMin: Int = 0
) {
    ALGERIA("وزارة الشؤون الدينية (الجزائر)", 18.0, 17.0),
    MWL("رابطة العالم الإسلامي", 18.0, 17.0),
    EGYPT("الهيئة المصرية العامة للمساحة", 19.5, 17.5),
    UMM_AL_QURA("أم القرى (مكة)", 18.5, 0.0, 90),
    ISNA("أمريكا الشمالية", 15.0, 15.0)
}

/**
 * Offline astronomical prayer-time calculation (same equations as the well-known praytimes.org
 * library). No network or location permission is needed: the user picks a city.
 */
object PrayerCalculator {

    /** Returns null when a time cannot be computed (e.g. polar latitudes). */
    fun compute(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        method: CalcMethod = CalcMethod.ALGERIA,
        asrFactor: Int = 1
    ): Map<Prayer, ZonedDateTime>? {
        val offsetHours = zone.rules
            .getOffset(date.atTime(12, 0).atZone(zone).toInstant()).totalSeconds / 3600.0
        val jd = julian(date.year, date.monthValue, date.dayOfMonth) - longitude / (15.0 * 24.0)

        var guess = mapOf(
            Prayer.FAJR to 5.0, Prayer.SUNRISE to 6.0, Prayer.DHUHR to 12.0,
            Prayer.ASR to 13.0, Prayer.MAGHRIB to 18.0, Prayer.ISHA to 18.0
        )
        repeat(2) { guess = step(guess, jd, latitude, method, asrFactor) }

        val shift = offsetHours - longitude / 15.0
        val start = date.atStartOfDay(zone)
        val out = HashMap<Prayer, ZonedDateTime>()
        for ((p, h) in guess) {
            if (h.isNaN()) return null
            val local = fixHour(h + shift)
            out[p] = start.plusSeconds((local * 3600.0).toLong())
        }
        return out
    }

    private fun step(
        hours: Map<Prayer, Double>, jd: Double, lat: Double, m: CalcMethod, asrFactor: Int
    ): Map<Prayer, Double> {
        fun t(p: Prayer) = hours.getValue(p) / 24.0
        val fajr = angleTime(jd, lat, m.fajrAngle, t(Prayer.FAJR), ccw = true)
        val sunrise = angleTime(jd, lat, RISE_SET_ANGLE, t(Prayer.SUNRISE), ccw = true)
        val dhuhr = midDay(jd, t(Prayer.DHUHR))
        val asr = angleTime(jd, lat, asrAngle(jd, lat, asrFactor, t(Prayer.ASR)), t(Prayer.ASR), ccw = false)
        val maghrib = angleTime(jd, lat, RISE_SET_ANGLE, t(Prayer.MAGHRIB), ccw = false)
        val isha = if (m.ishaIntervalMin > 0) maghrib + m.ishaIntervalMin / 60.0
        else angleTime(jd, lat, m.ishaAngle, t(Prayer.ISHA), ccw = false)
        return mapOf(
            Prayer.FAJR to fajr, Prayer.SUNRISE to sunrise, Prayer.DHUHR to dhuhr,
            Prayer.ASR to asr, Prayer.MAGHRIB to maghrib, Prayer.ISHA to isha
        )
    }

    private const val RISE_SET_ANGLE = 0.833

    private fun asrAngle(jd: Double, lat: Double, factor: Int, t: Double): Double {
        val decl = sunPosition(jd + t).second
        return -Math.toDegrees(atan(1.0 / (factor + tan(Math.toRadians(abs(lat - decl))))))
    }

    private fun angleTime(jd: Double, lat: Double, angle: Double, t: Double, ccw: Boolean): Double {
        val decl = sunPosition(jd + t).second
        val noon = midDay(jd, t)
        val latR = Math.toRadians(lat)
        val declR = Math.toRadians(decl)
        val x = (-sin(Math.toRadians(angle)) - sin(declR) * sin(latR)) / (cos(declR) * cos(latR))
        if (x < -1.0 || x > 1.0) return Double.NaN
        val delta = Math.toDegrees(acos(x)) / 15.0
        return noon + if (ccw) -delta else delta
    }

    private fun midDay(jd: Double, t: Double): Double =
        fixHour(12.0 - sunPosition(jd + t).first)

    /** Returns (equation of time in hours, declination in degrees). */
    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = fixHour(
            Math.toDegrees(atan2(cos(Math.toRadians(e)) * sin(Math.toRadians(l)), cos(Math.toRadians(l)))) / 15.0
        )
        val eqt = q / 15.0 - ra
        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        return eqt to decl
    }

    private fun julian(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) { y -= 1; m += 12 }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixAngle(a: Double) = fix(a, 360.0)
    private fun fixHour(a: Double) = fix(a, 24.0)
    private fun fix(a: Double, mode: Double): Double {
        val r = a - mode * floor(a / mode)
        return if (r < 0) r + mode else r
    }
}
