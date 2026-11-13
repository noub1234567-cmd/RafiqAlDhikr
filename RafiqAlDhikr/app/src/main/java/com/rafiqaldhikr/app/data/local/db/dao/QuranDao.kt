package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {
    @Query("SELECT * FROM quran_goals WHERE profileId = :profileId LIMIT 1")
    fun observeGoal(profileId: Long): Flow<QuranGoalEntity?>

    @Query("SELECT * FROM quran_goals WHERE profileId = :profileId LIMIT 1")
    suspend fun getGoal(profileId: Long): QuranGoalEntity?

    @Query("SELECT * FROM quran_goals WHERE reminderEnabled = 1")
    suspend fun getAllEnabledGoalReminders(): List<QuranGoalEntity>

    @Query("SELECT * FROM quran_goals")
    suspend fun getAllGoals(): List<QuranGoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: QuranGoalEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGoals(goals: List<QuranGoalEntity>)

    @Query("SELECT * FROM quran_reading_logs WHERE profileId = :profileId ORDER BY dateEpochDay DESC, loggedAtMillis DESC")
    fun observeLogs(profileId: Long): Flow<List<QuranReadingLogEntity>>

    @Query("SELECT * FROM quran_reading_logs")
    suspend fun getAllLogs(): List<QuranReadingLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: QuranReadingLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<QuranReadingLogEntity>)

    @Delete
    suspend fun deleteLog(log: QuranReadingLogEntity)
}
