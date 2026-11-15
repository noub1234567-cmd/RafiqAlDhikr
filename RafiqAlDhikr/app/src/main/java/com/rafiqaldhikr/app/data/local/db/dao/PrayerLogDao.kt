package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerLogDao {
    @Query("SELECT * FROM prayer_logs WHERE profileId = :profileId AND dateEpochDay BETWEEN :fromDay AND :toDay")
    fun observeRange(profileId: Long, fromDay: Long, toDay: Long): Flow<List<PrayerLogEntity>>

    @Query("SELECT * FROM prayer_logs WHERE profileId = :profileId AND dateEpochDay >= :fromDay")
    suspend fun getSince(profileId: Long, fromDay: Long): List<PrayerLogEntity>

    @Query("SELECT * FROM prayer_logs")
    suspend fun getAll(): List<PrayerLogEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(log: PrayerLogEntity): Long

    @Query("DELETE FROM prayer_logs WHERE profileId = :profileId AND dateEpochDay = :day AND itemKey = :itemKey")
    suspend fun delete(profileId: Long, day: Long, itemKey: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<PrayerLogEntity>)
}
