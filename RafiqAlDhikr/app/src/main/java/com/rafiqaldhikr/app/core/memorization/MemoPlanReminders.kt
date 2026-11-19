package com.rafiqaldhikr.app.core.memorization

import android.content.Context

/** Per-plan daily reminder time, kept in SharedPreferences as minutes after midnight (absent = off). */
object MemoPlanReminders {
    private const val PREFS = "memo_plan_reminders"

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Minutes after midnight, or -1 when no reminder is set. */
    fun minutes(context: Context, planId: Long): Int = prefs(context).getInt("p_$planId", -1)

    fun set(context: Context, planId: Long, hour: Int, minute: Int) {
        prefs(context).edit().putInt("p_$planId", hour * 60 + minute).apply()
    }

    fun clear(context: Context, planId: Long) {
        prefs(context).edit().remove("p_$planId").apply()
    }

    fun label(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        val h12 = if (h % 12 == 0) 12 else h % 12
        return "%d:%02d %s".format(h12, m, if (h < 12) "صباحاً" else "مساءً")
    }
}
