package com.rafiqaldhikr.app.core.security

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent

/**
 * Optional app lock that reuses the phone's own screen lock (fingerprint, pattern, PIN or password).
 * No password is stored by the app.
 */
object AppLock {
    private const val PREFS = "app_lock"
    private const val KEY_ENABLED = "enabled"

    /** After leaving the app for longer than this, it asks again. */
    const val RELOCK_AFTER_MILLIS = 30_000L

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    /** True when the phone has a screen lock this feature can rely on. */
    fun isDeviceSecure(context: Context): Boolean {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return false
        return km.isDeviceSecure
    }

    /** The system credential screen, or null when the phone has no screen lock. */
    @Suppress("DEPRECATION")
    fun credentialIntent(context: Context): Intent? {
        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return null
        return km.createConfirmDeviceCredentialIntent("رفيقك في الذكر", "أكّد هويتك لفتح التطبيق")
    }
}
