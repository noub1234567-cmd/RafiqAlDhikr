package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KhatmaDao {
    /** The unfinished khatma, if any (at most one per profile is created by the app). */
    @Query("SELECT * FROM khatmas WHERE profileId = :profileId AND finishedEpochDay = 0 ORDER BY id DESC LIMIT 1")
    fun observeActive(profileId: Long): Flow<KhatmaEntity?>

    @Query("SELECT COUNT(*) FROM khatmas WHERE profileId = :profileId AND finishedEpochDay > 0")
    fun observeFinishedCount(profileId: Long): Flow<Int>

    @Query("SELECT * FROM khatmas WHERE id = :id")
    suspend fun getById(id: Long): KhatmaEntity?

    @Query("SELECT * FROM khatmas")
    suspend fun getAll(): List<KhatmaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(khatma: KhatmaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<KhatmaEntity>)

    @Query("DELETE FROM khatmas WHERE id = :id")
    suspend fun deleteById(id: Long)
}
