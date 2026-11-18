package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.DuaFavoriteEntity
import com.rafiqaldhikr.app.data.local.db.entities.DuaMemoEntity
import com.rafiqaldhikr.app.data.local.db.entities.NightReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonalToolsDao {
    // Favorites
    @Query("SELECT * FROM dua_favorites WHERE profileId = :profileId ORDER BY createdAtMillis DESC")
    fun observeFavorites(profileId: Long): Flow<List<DuaFavoriteEntity>>

    @Query("SELECT * FROM dua_favorites WHERE profileId = :profileId AND itemKey = :itemKey LIMIT 1")
    suspend fun getFavorite(profileId: Long, itemKey: String): DuaFavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavorite(entity: DuaFavoriteEntity): Long

    @Query("DELETE FROM dua_favorites WHERE id = :id")
    suspend fun deleteFavorite(id: Long)

    @Query("SELECT * FROM dua_favorites")
    suspend fun getAllFavorites(): List<DuaFavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFavorites(items: List<DuaFavoriteEntity>)

    // Night reviews
    @Query("SELECT * FROM night_reviews WHERE profileId = :profileId ORDER BY dateEpochDay DESC")
    fun observeReviews(profileId: Long): Flow<List<NightReviewEntity>>

    @Query("SELECT * FROM night_reviews WHERE profileId = :profileId AND dateEpochDay = :day LIMIT 1")
    suspend fun getReview(profileId: Long, day: Long): NightReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(entity: NightReviewEntity): Long

    @Query("DELETE FROM night_reviews WHERE id = :id")
    suspend fun deleteReview(id: Long)

    @Query("SELECT * FROM night_reviews")
    suspend fun getAllReviews(): List<NightReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReviews(items: List<NightReviewEntity>)

    // Du'a memorization
    @Query("SELECT * FROM dua_memos WHERE profileId = :profileId ORDER BY dueEpochDay ASC, id ASC")
    fun observeMemos(profileId: Long): Flow<List<DuaMemoEntity>>

    @Query("SELECT * FROM dua_memos WHERE profileId = :profileId AND itemKey = :itemKey LIMIT 1")
    suspend fun getMemo(profileId: Long, itemKey: String): DuaMemoEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMemo(entity: DuaMemoEntity): Long

    @Update
    suspend fun updateMemo(entity: DuaMemoEntity)

    @Query("DELETE FROM dua_memos WHERE id = :id")
    suspend fun deleteMemo(id: Long)

    @Query("SELECT * FROM dua_memos")
    suspend fun getAllMemos(): List<DuaMemoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMemos(items: List<DuaMemoEntity>)
}
