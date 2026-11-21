package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaMemoSchedule
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.BookmarkEntity
import com.rafiqaldhikr.app.data.local.db.entities.DuaFavoriteEntity
import com.rafiqaldhikr.app.data.local.db.entities.DuaMemoEntity
import com.rafiqaldhikr.app.data.local.db.entities.NightReviewEntity
import kotlinx.coroutines.flow.Flow

/** Global favorites, du'a memorization and the nightly self-accounting log. */
class PersonalToolsRepository(database: AppDatabase) {
    private val dao = database.personalToolsDao()

    // Favorites
    fun observeFavorites(profileId: Long): Flow<List<DuaFavoriteEntity>> = dao.observeFavorites(profileId)

    suspend fun isFavorite(profileId: Long, text: String): Boolean =
        dao.getFavorite(profileId, DuaLibrary.itemKey(text)) != null

    /** Stars the item, or removes the star if it is already there; returns the new state. */
    suspend fun toggleFavorite(profileId: Long, title: String, text: String, source: String): Boolean {
        val key = DuaLibrary.itemKey(text)
        val existing = dao.getFavorite(profileId, key)
        if (existing != null) {
            dao.deleteFavorite(existing.id)
            return false
        }
        dao.insertFavorite(
            DuaFavoriteEntity(
                profileId = profileId, itemKey = key, title = title.trim(), text = text.trim(),
                source = source.trim(), createdAtMillis = System.currentTimeMillis()
            )
        )
        return true
    }

    // Bookmarks on library texts
    fun observeBookmarks(profileId: Long): Flow<List<BookmarkEntity>> = dao.observeBookmarks(profileId)

    suspend fun isBookmarked(profileId: Long, sectionId: String, locator: String): Boolean =
        dao.getBookmark(profileId, "$sectionId|$locator") != null

    /** Adds the bookmark, or removes it if it is already there; returns the new state. */
    suspend fun toggleBookmark(
        profileId: Long, sectionId: String, sectionTitle: String, locator: String,
        title: String, text: String, source: String
    ): Boolean {
        val key = "$sectionId|$locator"
        val existing = dao.getBookmark(profileId, key)
        if (existing != null) {
            dao.deleteBookmark(existing.id)
            return false
        }
        dao.insertBookmark(
            BookmarkEntity(
                profileId = profileId, itemKey = key, sectionId = sectionId, sectionTitle = sectionTitle,
                locator = locator, title = title.trim(), text = text.trim(), source = source.trim(),
                createdAtMillis = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun deleteBookmark(id: Long) = dao.deleteBookmark(id)

    // Night reviews
    fun observeReviews(profileId: Long): Flow<List<NightReviewEntity>> = dao.observeReviews(profileId)

    suspend fun getReview(profileId: Long, day: Long): NightReviewEntity? = dao.getReview(profileId, day)

    /** Saves the night's answers; all three blank removes the entry instead. */
    suspend fun saveReview(profileId: Long, day: Long, shortfall: String, good: String, forgiveness: String) {
        val existing = dao.getReview(profileId, day)
        if (shortfall.isBlank() && good.isBlank() && forgiveness.isBlank()) {
            existing?.let { dao.deleteReview(it.id) }
            return
        }
        dao.upsertReview(
            NightReviewEntity(
                id = existing?.id ?: 0, profileId = profileId, dateEpochDay = day,
                shortfall = shortfall.trim(), good = good.trim(), forgiveness = forgiveness.trim(),
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteReview(id: Long) = dao.deleteReview(id)

    // Du'a memorization
    fun observeMemos(profileId: Long): Flow<List<DuaMemoEntity>> = dao.observeMemos(profileId)

    /** Adds the du'a to memorization (first review today); returns false if it is already there. */
    suspend fun addMemo(profileId: Long, title: String, text: String, source: String, today: Long): Boolean {
        val id = dao.insertMemo(
            DuaMemoEntity(
                profileId = profileId, itemKey = DuaLibrary.itemKey(text), title = title.trim().ifBlank { "دعاء" },
                text = text.trim(), source = source.trim(), stage = 0, dueEpochDay = today,
                lastEpochDay = -1, createdAtMillis = System.currentTimeMillis()
            )
        )
        return id != -1L
    }

    suspend fun reviewMemo(memo: DuaMemoEntity, remembered: Boolean, today: Long) {
        val next = DuaMemoSchedule.afterReview(memo.stage, memo.lastEpochDay, remembered, today)
        dao.updateMemo(memo.copy(stage = next.stage, dueEpochDay = next.dueEpochDay, lastEpochDay = today))
    }

    suspend fun deleteMemo(id: Long) = dao.deleteMemo(id)
}
