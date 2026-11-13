package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FastingDao {
    @Query("SELECT * FROM fasting_days WHERE profileId = :profileId ORDER BY dateEpochDay DESC")
    fun observeFastingDays(profileId: Long): Flow<List<FastingDayEntity>>

    @Query("SELECT * FROM fasting_days WHERE profileId = :profileId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getByDate(profileId: Long, dateEpochDay: Long): FastingDayEntity?

    @Query("SELECT * FROM fasting_days WHERE reminderEnabled = 1 AND isCompleted = 0")
    suspend fun getAllActiveReminders(): List<FastingDayEntity>

    @Query("SELECT * FROM fasting_days")
    suspend fun getAll(): List<FastingDayEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(day: FastingDayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<FastingDayEntity>)

    @Delete
    suspend fun delete(day: FastingDayEntity)
}
