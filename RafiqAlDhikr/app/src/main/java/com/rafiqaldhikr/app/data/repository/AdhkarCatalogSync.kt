package com.rafiqaldhikr.app.data.repository

import android.content.Context
import com.rafiqaldhikr.app.core.model.HisnAdhkarCatalog
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity

/**
 * Adds the Hisn al-Muslim adhkar to profiles that were created before they existed.
 *
 * Each profile is synced once per [VERSION]: only template keys the profile does not have yet are inserted, so an
 * adhkar the user deleted afterwards is not brought back. New profiles already contain everything and are just marked.
 */
object AdhkarCatalogSync {
    private const val PREFS = "adhkar_catalog_sync"
    private const val KEY_SYNCED = "synced_profiles"
    private const val VERSION = "hisn1"

    private val hisnKeys: Set<String> by lazy { HisnAdhkarCatalog.entries.map { it.key }.toSet() }

    suspend fun run(context: Context, database: AppDatabase) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val synced = prefs.getStringSet(KEY_SYNCED, emptySet()).orEmpty().toMutableSet()
        val dao = database.adhkarDao()
        var changed = false
        for (profile in database.profileDao().getAll()) {
            val marker = "${profile.id}:$VERSION"
            if (marker in synced) continue
            val existing = dao.getEntriesByProfile(profile.id)
            val have = existing.mapNotNull { it.templateKey }.toSet()
            // Already contains Hisn entries (new profile, or a backup taken after this feature): nothing to add,
            // and anything the user removed from it stays removed.
            if (hisnKeys.any { it in have }) {
                synced.add(marker)
                changed = true
                continue
            }
            var order = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1
            val missing = HisnAdhkarCatalog.entries.filter { it.key !in have }.map { item ->
                AdhkarEntryEntity(
                    profileId = profile.id,
                    templateKey = item.key,
                    category = item.category,
                    title = item.title,
                    arabicText = item.arabicText,
                    targetCount = item.targetCount,
                    sourceReference = item.sourceReference,
                    isCustom = false,
                    sortOrder = order++
                )
            }
            if (missing.isNotEmpty()) dao.insertEntries(missing)
            synced.add(marker)
            changed = true
        }
        if (changed) prefs.edit().putStringSet(KEY_SYNCED, synced).apply()
    }

    /** After a restore the profiles come from the backup and may predate the Hisn adhkar, so they must be checked again. */
    fun forgetAll(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_SYNCED).apply()
    }
}
