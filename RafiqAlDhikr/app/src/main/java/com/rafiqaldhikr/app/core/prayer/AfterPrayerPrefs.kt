package com.rafiqaldhikr.app.core.prayer

import android.content.Context

/** Whether the after-prayer adhkar sheet opens by itself when a fard prayer is ticked in the tracker. */
object AfterPrayerPrefs {
    private const val PREFS = "after_prayer"
    private const val KEY_AUTO = "auto_show"

    fun autoShow(context: Context): Boolean =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_AUTO, true)

    fun setAutoShow(context: Context, value: Boolean) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_AUTO, value).apply()
    }
}
