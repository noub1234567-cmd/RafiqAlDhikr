package com.rafiqaldhikr.app.notifications

import android.content.Context
import org.json.JSONObject

/**
 * Remembers which alarms were armed and which ones actually reached the app.
 * Some OEMs (ColorOS / Realme, MIUI...) silently drop pending alarms when the app is swiped away;
 * comparing "armed" with "fired" lets the app deliver a late reminder the next time it runs.
 */
object AlarmState {
    private const val PREFS = "alarm_state"
    private const val ARMED = "armed_"
    private const val FIRED = "fired_"
    private const val OPT_ALARM_CLOCK = "use_alarm_clock"
    /** A reminder older than this is no longer worth delivering. */
    const val PRAYER_CATCH_UP_WINDOW_MS = 15L * 60 * 1000
    const val SPECIAL_CATCH_UP_WINDOW_MS = 60L * 60 * 1000
    const val CATCH_UP_WINDOW_MS = 6L * 60 * 60 * 1000

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun useAlarmClock(context: Context): Boolean = prefs(context).getBoolean(OPT_ALARM_CLOCK, true)

    fun setUseAlarmClock(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(OPT_ALARM_CLOCK, value).apply()
    }

    fun markArmed(
        context: Context, requestCode: Int, trigger: Long, kind: String, targetId: Long,
        profileId: Long, activityId: Long, title: String, body: String, sound: String
    ) {
        val json = JSONObject()
            .put("t", trigger).put("k", kind).put("id", targetId).put("p", profileId)
            .put("a", activityId).put("title", title).put("body", body).put("s", sound)
        prefs(context).edit().putString(ARMED + requestCode, json.toString()).apply()
    }

    fun clearArmed(context: Context, requestCode: Int) {
        prefs(context).edit().remove(ARMED + requestCode).apply()
    }

    fun markFired(context: Context, requestCode: Int) {
        prefs(context).edit().putLong(FIRED + requestCode, System.currentTimeMillis()).apply()
    }

    data class Armed(
        val requestCode: Int, val trigger: Long, val kind: String, val targetId: Long,
        val profileId: Long, val activityId: Long, val title: String, val body: String, val sound: String
    )

    /** Every alarm currently armed (soonest first), for the diagnostics screen. */
    fun armedAll(context: Context): List<Armed> =
        prefs(context).all.entries.mapNotNull { (key, value) ->
            if (!key.startsWith(ARMED) || value !is String) return@mapNotNull null
            runCatching {
                val o = JSONObject(value)
                Armed(key.removePrefix(ARMED).toInt(), o.getLong("t"), o.getString("k"), o.getLong("id"),
                    o.getLong("p"), o.getLong("a"), o.getString("title"), o.getString("body"), o.getString("s"))
            }.getOrNull()
        }.sortedBy { it.trigger }

    /** Alarms that should have fired already (within the catch-up window) but never reached the receiver. */
    fun overdue(context: Context, now: Long): List<Armed> {
        val p = prefs(context)
        return p.all.entries.mapNotNull { (key, value) ->
            if (!key.startsWith(ARMED) || value !is String) return@mapNotNull null
            runCatching {
                val code = key.removePrefix(ARMED).toInt()
                val o = JSONObject(value)
                val trigger = o.getLong("t")
                val lastFired = p.getLong(FIRED + code, 0L)
                // A prayer-time reminder delivered hours late is worse than none.
                val window = when (o.getString("k")) {
                    ReminderScheduler.KIND_PRAYER -> PRAYER_CATCH_UP_WINDOW_MS
                    ReminderScheduler.KIND_SPECIAL -> SPECIAL_CATCH_UP_WINDOW_MS
                    else -> CATCH_UP_WINDOW_MS
                }
                if (trigger < now && now - trigger <= window && lastFired < trigger) {
                    Armed(code, trigger, o.getString("k"), o.getLong("id"), o.getLong("p"),
                        o.getLong("a"), o.getString("title"), o.getString("body"), o.getString("s"))
                } else null
            }.getOrNull()
        }
    }
}
