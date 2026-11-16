package com.rafiqaldhikr.app.core.tasbeeh

import android.content.Context

/** Small settings for hands-free tasbeeh: volume-key counting and the dhikr the widget counts. */
object TasbeehPrefs {
    private const val PREFS = "tasbeeh_prefs"
    private const val KEY_VOLUME_KEYS = "volume_keys"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun volumeKeysEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_VOLUME_KEYS, true)

    fun setVolumeKeysEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_VOLUME_KEYS, value).apply()
    }

    fun lastTypeId(context: Context, profileId: Long): Long =
        prefs(context).getLong("last_type_$profileId", -1L)

    fun setLastTypeId(context: Context, profileId: Long, typeId: Long) {
        prefs(context).edit().putLong("last_type_$profileId", typeId).apply()
    }
}

/**
 * While the tasbeeh screen is open, the volume buttons count instead of changing the volume.
 * The screen sets [handler] on entry and clears it on exit; MainActivity forwards key presses here.
 */
object VolumeKeyBridge {
    @Volatile
    var handler: (() -> Unit)? = null
}
