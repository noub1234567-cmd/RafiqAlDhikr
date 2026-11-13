package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_types WHERE profileId = :profileId ORDER BY sortOrder ASC, id ASC")
    fun observeTypes(profileId: Long): Flow<List<TasbeehTypeEntity>>

    @Query("SELECT * FROM tasbeeh_types WHERE profileId = :profileId")
    suspend fun getTypesByProfile(profileId: Long): List<TasbeehTypeEntity>

    @Query("SELECT * FROM tasbeeh_types")
    suspend fun getAllTypes(): List<TasbeehTypeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertType(type: TasbeehTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypes(types: List<TasbeehTypeEntity>)

    @Update
    suspend fun updateType(type: TasbeehTypeEntity)

    @Delete
    suspend fun deleteType(type: TasbeehTypeEntity)

    @Query("SELECT * FROM tasbeeh_logs WHERE profileId = :profileId ORDER BY dateEpochDay DESC")
    fun observeLogs(profileId: Long): Flow<List<TasbeehLogEntity>>

    @Query("SELECT * FROM tasbeeh_logs WHERE profileId = :profileId AND dateEpochDay = :dateEpochDay")
    fun observeLogsForDay(profileId: Long, dateEpochDay: Long): Flow<List<TasbeehLogEntity>>

    @Query("SELECT * FROM tasbeeh_logs WHERE profileId = :profileId AND tasbeehTypeId = :typeId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLogForDay(profileId: Long, typeId: Long, dateEpochDay: Long): TasbeehLogEntity?

    @Query("SELECT * FROM tasbeeh_logs")
    suspend fun getAllLogs(): List<TasbeehLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: TasbeehLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<TasbeehLogEntity>)
}
