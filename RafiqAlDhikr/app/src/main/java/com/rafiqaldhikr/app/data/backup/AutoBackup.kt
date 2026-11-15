package com.rafiqaldhikr.app.data.backup

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Settings and file handling for the automatic weekly backup (kept in the app's own folder). */
object AutoBackup {
    private const val PREFS = "auto_backup"
    private const val KEY_ENABLED = "enabled"
    private const val KEEP = 4

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    private fun dir(context: Context): File {
        val base = context.getExternalFilesDir(null) ?: context.filesDir
        return File(base, "backups").apply { mkdirs() }
    }

    /** Newest automatic backup, or null if none exists yet. */
    fun latest(context: Context): File? =
        dir(context).listFiles { f -> f.name.startsWith("auto-") && f.name.endsWith(".json") }
            ?.maxByOrNull { it.name }

    fun describe(file: File): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(file.lastModified()))

    /** Writes [json] as a new automatic backup and prunes old ones. */
    fun save(context: Context, json: String): File {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val d = dir(context)
        val tmp = File(d, "auto-$stamp.tmp")
        tmp.writeText(json, Charsets.UTF_8)
        val target = File(d, "auto-$stamp.json")
        check(tmp.renameTo(target)) { "تعذّر حفظ النسخة التلقائية" }
        d.listFiles { f -> f.name.startsWith("auto-") && f.name.endsWith(".json") }
            ?.sortedByDescending { it.name }
            ?.drop(KEEP)
            ?.forEach { it.delete() }
        return target
    }
}
