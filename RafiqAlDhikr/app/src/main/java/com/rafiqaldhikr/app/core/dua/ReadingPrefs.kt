package com.rafiqaldhikr.app.core.dua

import android.content.Context

/** Reading comfort for the long library sections: one text scale for all, and the last spot per section. */
object ReadingPrefs {
    private const val FILE = "reading_prefs"
    private const val SCALE = "scale"
    val scales = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f, 1.7f)

    private fun prefs(c: Context) = c.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun scale(c: Context): Float = prefs(c).getFloat(SCALE, 1f).takeIf { it in scales } ?: 1f

    /** The next larger (+1) or smaller (-1) step, saved; stays put at the ends. */
    fun step(c: Context, delta: Int): Float {
        val i = (scales.indexOf(scale(c)).coerceAtLeast(0) + delta).coerceIn(0, scales.lastIndex)
        prefs(c).edit().putFloat(SCALE, scales[i]).apply()
        return scales[i]
    }

    fun position(c: Context, sectionId: String): String? = prefs(c).getString("pos_$sectionId", null)

    fun savePosition(c: Context, sectionId: String, key: String) {
        prefs(c).edit().putString("pos_$sectionId", key).apply()
    }
}
