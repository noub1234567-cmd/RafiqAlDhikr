package com.rafiqaldhikr.app.notifications

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiny on-device log of alarm scheduling / delivery, shown in Settings.
 * Helps find out whether a missing notification was never scheduled, never fired (OEM battery
 * management) or was skipped by the app, without needing adb.
 */
object AlarmDiagnostics {
    private const val PREFS = "alarm_diagnostics"
    private const val KEY = "lines"
    private const val MAX_LINES = 80
    private const val SEP = "\u001F"

    private fun stamp(): String =
        SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(Date())

    fun record(context: Context, message: String) {
        runCatching {
            synchronized(this) {
                val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                val old = prefs.getString(KEY, "").orEmpty()
                    .split(SEP).filter { it.isNotBlank() }
                val updated = (old + "${stamp()}  $message").takeLast(MAX_LINES)
                prefs.edit().putString(KEY, updated.joinToString(SEP)).apply()
            }
        }
    }

    /** Newest first. */
    fun read(context: Context): List<String> = runCatching {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, "").orEmpty()
            .split(SEP).filter { it.isNotBlank() }.reversed()
    }.getOrDefault(emptyList())

    fun clear(context: Context) {
        runCatching {
            context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().remove(KEY).apply()
        }
    }
}
