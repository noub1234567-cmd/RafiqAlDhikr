package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemorizationDao {
    @Query("SELECT * FROM memorization_plans WHERE profileId = :profileId ORDER BY createdAt DESC")
    fun observePlans(profileId: Long): Flow<List<MemorizationPlanEntity>>

    @Query("SELECT * FROM memorization_plans WHERE id = :planId LIMIT 1")
    suspend fun getPlanById(planId: Long): MemorizationPlanEntity?

    @Query("SELECT * FROM memorization_plans WHERE isActive = 1 AND (memReminderEnabled = 1 OR revReminderEnabled = 1)")
    suspend fun getAllReminderPlans(): List<MemorizationPlanEntity>

    @Query("SELECT * FROM memorization_plans")
    suspend fun getAllPlans(): List<MemorizationPlanEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: MemorizationPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlans(plans: List<MemorizationPlanEntity>)

    @Update
    suspend fun updatePlan(plan: MemorizationPlanEntity)

    @Delete
    suspend fun deletePlan(plan: MemorizationPlanEntity)

    @Query("SELECT * FROM memorization_sessions WHERE profileId = :profileId ORDER BY completedAtMillis DESC")
    fun observeSessions(profileId: Long): Flow<List<MemorizationSessionEntity>>

    @Query("SELECT * FROM memorization_sessions")
    suspend fun getAllSessions(): List<MemorizationSessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: MemorizationSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSessions(sessions: List<MemorizationSessionEntity>)

    @Delete
    suspend fun deleteSession(session: MemorizationSessionEntity)
}
