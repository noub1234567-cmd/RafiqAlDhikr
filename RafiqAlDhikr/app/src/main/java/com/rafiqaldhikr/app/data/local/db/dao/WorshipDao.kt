package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorshipDao {
    // Hidden "sys-" rows only carry history logs for adhkar / quran / memorization reminders.
    @Query("SELECT * FROM worship_activities WHERE profileId = :profileId AND (templateKey IS NULL OR templateKey NOT LIKE 'sys-%') ORDER BY isOptionalBuiltIn ASC, createdAt ASC")
    fun observeActivities(profileId: Long): Flow<List<WorshipActivityEntity>>

    @Query("SELECT * FROM worship_activities WHERE profileId = :profileId")
    suspend fun getActivitiesByProfile(profileId: Long): List<WorshipActivityEntity>

    @Query("SELECT * FROM worship_activities WHERE reminderEnabled = 1 AND isEnabled = 1")
    suspend fun getAllActiveReminderActivities(): List<WorshipActivityEntity>

    @Query("SELECT * FROM worship_activities")
    suspend fun getAllActivities(): List<WorshipActivityEntity>

    @Query("SELECT * FROM worship_activities WHERE profileId = :profileId AND templateKey = :key LIMIT 1")
    suspend fun getActivityByTemplateKey(profileId: Long, key: String): WorshipActivityEntity?

    @Query("SELECT * FROM worship_activities WHERE id = :id LIMIT 1")
    suspend fun getActivityById(id: Long): WorshipActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: WorshipActivityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<WorshipActivityEntity>)

    @Update
    suspend fun updateActivity(activity: WorshipActivityEntity)

    @Delete
    suspend fun deleteActivity(activity: WorshipActivityEntity)

    @Query("SELECT * FROM activity_logs WHERE profileId = :profileId ORDER BY dateEpochDay DESC, scheduledAtMillis DESC")
    fun observeLogs(profileId: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE profileId = :profileId AND dateEpochDay = :dateEpochDay ORDER BY scheduledAtMillis ASC")
    fun observeLogsForDay(profileId: Long, dateEpochDay: Long): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE profileId = :profileId AND dateEpochDay = :dateEpochDay")
    suspend fun getLogsForDay(profileId: Long, dateEpochDay: Long): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE profileId = :profileId AND activityId = :activityId AND dateEpochDay = :dateEpochDay LIMIT 1")
    suspend fun getLogForActivityDay(profileId: Long, activityId: Long, dateEpochDay: Long): ActivityLogEntity?

    @Query("SELECT * FROM activity_logs WHERE id = :logId LIMIT 1")
    suspend fun getLogById(logId: Long): ActivityLogEntity?

    @Query("SELECT * FROM activity_logs")
    suspend fun getAllLogs(): List<ActivityLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ActivityLogEntity>)

    @Update
    suspend fun updateLog(log: ActivityLogEntity)

    @Delete
    suspend fun deleteLog(log: ActivityLogEntity)
}
