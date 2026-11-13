package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DuaDao {
    @Query("SELECT * FROM dua_entries WHERE profileId = :profileId ORDER BY isFavorite DESC, updatedAt DESC")
    fun observeDuas(profileId: Long): Flow<List<DuaEntryEntity>>

    @Query("SELECT * FROM dua_entries")
    suspend fun getAll(): List<DuaEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dua: DuaEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(duas: List<DuaEntryEntity>)

    @Update
    suspend fun update(dua: DuaEntryEntity)

    @Delete
    suspend fun delete(dua: DuaEntryEntity)
}

@Dao
interface SeasonalDao {
    @Query("SELECT * FROM seasonal_selections WHERE profileId = :profileId")
    fun observeSelections(profileId: Long): Flow<List<SeasonalSelectionEntity>>

    @Query("SELECT * FROM seasonal_selections WHERE profileId = :profileId AND seasonId = :seasonId LIMIT 1")
    suspend fun getSelection(profileId: Long, seasonId: String): SeasonalSelectionEntity?

    @Query("SELECT * FROM seasonal_selections WHERE isSelected = 1 AND reminderEnabled = 1")
    suspend fun getAllEnabledReminders(): List<SeasonalSelectionEntity>

    @Query("SELECT * FROM seasonal_selections")
    suspend fun getAll(): List<SeasonalSelectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(selection: SeasonalSelectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(selections: List<SeasonalSelectionEntity>)
}
