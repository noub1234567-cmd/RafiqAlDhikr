package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdhkarDao {
    @Query("SELECT * FROM adhkar_entries WHERE profileId = :profileId ORDER BY sortOrder ASC, id ASC")
    fun observeEntries(profileId: Long): Flow<List<AdhkarEntryEntity>>

    @Query("SELECT * FROM adhkar_entries WHERE profileId = :profileId")
    suspend fun getEntriesByProfile(profileId: Long): List<AdhkarEntryEntity>

    @Query("SELECT * FROM adhkar_entries")
    suspend fun getAllEntries(): List<AdhkarEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: AdhkarEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<AdhkarEntryEntity>)

    @Update
    suspend fun updateEntry(entry: AdhkarEntryEntity)

    @Delete
    suspend fun deleteEntry(entry: AdhkarEntryEntity)

    @Query("SELECT * FROM adhkar_progress WHERE profileId = :profileId AND dateEpochDay = :dateEpochDay")
    fun observeProgressForDay(profileId: Long, dateEpochDay: Long): Flow<List<AdhkarProgressEntity>>

    @Query("SELECT * FROM adhkar_progress WHERE profileId = :profileId AND adhkarEntryId = :entryId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getProgress(profileId: Long, entryId: Long, dateEpochDay: Long): AdhkarProgressEntity?

    @Query("SELECT * FROM adhkar_progress")
    suspend fun getAllProgress(): List<AdhkarProgressEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: AdhkarProgressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProgress(items: List<AdhkarProgressEntity>)

    @Query("SELECT * FROM adhkar_reminders WHERE profileId = :profileId")
    fun observeReminders(profileId: Long): Flow<List<AdhkarReminderEntity>>

    @Query("SELECT * FROM adhkar_reminders WHERE enabled = 1")
    suspend fun getAllEnabledReminders(): List<AdhkarReminderEntity>

    @Query("SELECT * FROM adhkar_reminders WHERE profileId = :profileId AND category = :category LIMIT 1")
    suspend fun getReminder(profileId: Long, category: AdhkarCategory): AdhkarReminderEntity?

    @Query("SELECT * FROM adhkar_reminders")
    suspend fun getAllReminders(): List<AdhkarReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReminder(reminder: AdhkarReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReminders(reminders: List<AdhkarReminderEntity>)
}
