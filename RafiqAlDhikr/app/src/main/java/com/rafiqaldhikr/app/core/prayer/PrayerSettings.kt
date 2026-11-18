package com.rafiqaldhikr.app.core.prayer

import android.content.Context
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** A reminder placed [offsetMinutes] after a prayer (or sunrise). */
data class PrayerLink(val anchor: Prayer, val offsetMinutes: Int)

/** City / method choice and per-reminder prayer links, kept in SharedPreferences. */
object PrayerSettings {
    private const val PREFS = "prayer_settings"
    private const val KEY_CITY = "city"
    private const val KEY_METHOD = "method"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun cityIndex(context: Context): Int =
        prefs(context).getInt(KEY_CITY, 0).coerceIn(0, PrayerCities.all.lastIndex)

    fun city(context: Context): PrayerCity = PrayerCities.all[cityIndex(context)]

    fun setCityIndex(context: Context, index: Int) {
        prefs(context).edit().putInt(KEY_CITY, index.coerceIn(0, PrayerCities.all.lastIndex)).apply()
    }

    fun method(context: Context): CalcMethod =
        runCatching { CalcMethod.valueOf(prefs(context).getString(KEY_METHOD, null) ?: "") }
            .getOrDefault(CalcMethod.ALGERIA)

    fun setMethod(context: Context, method: CalcMethod) {
        prefs(context).edit().putString(KEY_METHOD, method.name).apply()
    }

    private fun linkKey(profileId: Long, category: String) = "link_${profileId}_$category"

    fun link(context: Context, profileId: Long, category: String): PrayerLink? {
        val raw = prefs(context).getString(linkKey(profileId, category), null) ?: return null
        val parts = raw.split(":")
        if (parts.size != 2) return null
        val anchor = runCatching { Prayer.valueOf(parts[0]) }.getOrNull() ?: return null
        val offset = parts[1].toIntOrNull() ?: return null
        return PrayerLink(anchor, offset)
    }

    fun setLink(context: Context, profileId: Long, category: String, link: PrayerLink?) {
        val e = prefs(context).edit()
        if (link == null) e.remove(linkKey(profileId, category))
        else e.putString(linkKey(profileId, category), "${link.anchor.name}:${link.offsetMinutes}")
        e.apply()
    }

    /** All five (plus sunrise) times for [date] in the chosen city, or null if not computable. */
    fun timesFor(context: Context, date: LocalDate): Map<Prayer, ZonedDateTime>? {
        val c = city(context)
        return PrayerCalculator.compute(
            date, c.latitude, c.longitude, ZoneId.of(c.zoneId), method(context)
        )
    }

    /** Next moment (after [nowMillis]) at which [link] fires, or null if it cannot be computed. */
    fun nextTrigger(context: Context, link: PrayerLink, nowMillis: Long): Long? {
        val c = city(context)
        val zone = ZoneId.of(c.zoneId)
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0L..2L) {
            val times = PrayerCalculator.compute(
                today.plusDays(offset), c.latitude, c.longitude, zone, method(context)
            ) ?: continue
            val at = times.getValue(link.anchor).plusMinutes(link.offsetMinutes.toLong()).toInstant().toEpochMilli()
            if (at > nowMillis) return at
        }
        return null
    }

    /** The five daily prayers (no sunrise). */
    val fardPrayers = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)

    /** Next of the five prayers after [nowMillis] and its time, or null if it cannot be computed. */
    fun nextPrayer(context: Context, nowMillis: Long): Pair<Prayer, Long>? {
        val c = city(context)
        val zone = ZoneId.of(c.zoneId)
        val today = java.time.Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        for (offset in 0L..1L) {
            val times = PrayerCalculator.compute(
                today.plusDays(offset), c.latitude, c.longitude, zone, method(context)
            ) ?: continue
            val next = fardPrayers
                .map { it to times.getValue(it).toInstant().toEpochMilli() }
                .firstOrNull { it.second > nowMillis }
            if (next != null) return next
        }
        return null
    }

    // ---- Adhan (prayer-time) reminders -------------------------------------------------

    fun adhanEnabled(context: Context, prayer: Prayer): Boolean =
        prefs(context).getBoolean("adhan_${prayer.name}", false)

    fun setAdhanEnabled(context: Context, prayer: Prayer, value: Boolean) {
        prefs(context).edit().putBoolean("adhan_${prayer.name}", value).apply()
    }

    /** Whether the adhan recording plays when a prayer-time alert fires (on by default). */
    fun adhanSoundEnabled(context: Context): Boolean = prefs(context).getBoolean("adhan_sound", true)

    fun setAdhanSoundEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean("adhan_sound", value).apply()
    }

    /** Minutes before the prayer at which the adhan reminder fires (0 = at the prayer time). */
    fun adhanLeadMinutes(context: Context): Int = prefs(context).getInt("adhan_lead", 0).coerceIn(0, 60)

    fun setAdhanLeadMinutes(context: Context, minutes: Int) {
        prefs(context).edit().putInt("adhan_lead", minutes.coerceIn(0, 60)).apply()
    }

    /** Next moment the adhan reminder of [prayer] should fire, or null if it cannot be computed. */
    fun nextAdhanTrigger(context: Context, prayer: Prayer, nowMillis: Long): Long? =
        nextTrigger(context, PrayerLink(prayer, -adhanLeadMinutes(context)), nowMillis)
}
