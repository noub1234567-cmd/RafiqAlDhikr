package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.MemoPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemoPlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoPlanDao {
    @Query("SELECT * FROM memo_plans WHERE profileId = :profileId ORDER BY createdAtMillis DESC")
    fun observePlans(profileId: Long): Flow<List<MemoPlanEntity>>

    @Query("SELECT * FROM memo_plans WHERE id = :id")
    fun observePlan(id: Long): Flow<MemoPlanEntity?>

    @Query("SELECT * FROM memo_plan_items WHERE planId = :planId ORDER BY position ASC")
    fun observeItems(planId: Long): Flow<List<MemoPlanItemEntity>>

    @Query(
        "SELECT i.* FROM memo_plan_items i JOIN memo_plans p ON p.id = i.planId " +
            "WHERE p.profileId = :profileId"
    )
    fun observeAllItems(profileId: Long): Flow<List<MemoPlanItemEntity>>

    @Insert
    suspend fun insertPlan(plan: MemoPlanEntity): Long

    @Insert
    suspend fun insertItems(items: List<MemoPlanItemEntity>)

    @Query("UPDATE memo_plan_items SET doneEpochDay = :day WHERE id = :id")
    suspend fun setDone(id: Long, day: Long)

    @Query("DELETE FROM memo_plans WHERE id = :id")
    suspend fun deletePlan(id: Long)

    @Query("SELECT * FROM memo_plans")
    suspend fun getAllPlans(): List<MemoPlanEntity>

    @Query("SELECT * FROM memo_plan_items")
    suspend fun getAllItems(): List<MemoPlanItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPlans(items: List<MemoPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllItems(items: List<MemoPlanItemEntity>)
}
