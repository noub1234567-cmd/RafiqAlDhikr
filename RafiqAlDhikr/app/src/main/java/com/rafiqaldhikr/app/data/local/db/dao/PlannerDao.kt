package com.rafiqaldhikr.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rafiqaldhikr.app.data.local.db.entities.PlannerReportEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerWeekEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_weeks WHERE profileId = :profileId AND weekStartEpochDay = :weekStart LIMIT 1")
    fun observeWeek(profileId: Long, weekStart: Long): Flow<PlannerWeekEntity?>

    @Query("SELECT * FROM planner_slots WHERE weekId = :weekId")
    fun observeSlots(weekId: Long): Flow<List<PlannerSlotEntity>>

    @Query("SELECT COUNT(*) FROM planner_weeks WHERE profileId = :profileId AND weekStartEpochDay < :weekStart")
    fun observePreviousCount(profileId: Long, weekStart: Long): Flow<Int>

    @Query("SELECT * FROM planner_weeks WHERE profileId = :profileId AND weekStartEpochDay = :weekStart LIMIT 1")
    suspend fun getWeek(profileId: Long, weekStart: Long): PlannerWeekEntity?

    @Query("SELECT * FROM planner_weeks WHERE id = :id")
    suspend fun getWeekById(id: Long): PlannerWeekEntity?

    @Query("SELECT * FROM planner_weeks WHERE profileId = :profileId AND weekStartEpochDay < :weekStart ORDER BY weekStartEpochDay ASC")
    suspend fun getWeeksBefore(profileId: Long, weekStart: Long): List<PlannerWeekEntity>

    @Query("SELECT * FROM planner_weeks WHERE weekStartEpochDay >= :weekStart")
    suspend fun getWeeksFrom(weekStart: Long): List<PlannerWeekEntity>

    @Query("SELECT * FROM planner_slots WHERE weekId = :weekId")
    suspend fun getSlotsForWeek(weekId: Long): List<PlannerSlotEntity>

    @Query("SELECT * FROM planner_slots WHERE id = :id")
    suspend fun getSlotById(id: Long): PlannerSlotEntity?

    @Query(
        "SELECT s.* FROM planner_slots s INNER JOIN planner_weeks w ON s.weekId = w.id WHERE w.profileId = :profileId"
    )
    suspend fun getSlotsByProfile(profileId: Long): List<PlannerSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWeek(week: PlannerWeekEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlot(slot: PlannerSlotEntity): Long

    @Query("DELETE FROM planner_slots WHERE id = :id")
    suspend fun deleteSlot(id: Long)

    @Query("DELETE FROM planner_slots WHERE weekId = :weekId AND rowIndex >= :fromRow")
    suspend fun deleteSlotsFromRow(weekId: Long, fromRow: Int)

    @Query("DELETE FROM planner_weeks WHERE id = :id")
    suspend fun deleteWeek(id: Long)

    @Query("UPDATE planner_weeks SET rowCount = :rows WHERE id = :id")
    suspend fun setRowCount(id: Long, rows: Int)

    @Query("UPDATE planner_weeks SET autoRepeat = :enabled WHERE id = :id")
    suspend fun setAutoRepeat(id: Long, enabled: Boolean)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReport(report: PlannerReportEntity): Long

    @Query("SELECT * FROM planner_reports WHERE profileId = :profileId AND kind = :kind ORDER BY startEpochDay DESC")
    fun observeReports(profileId: Long, kind: String): Flow<List<PlannerReportEntity>>

    @Query("DELETE FROM planner_reports WHERE id = :id")
    suspend fun deleteReport(id: Long)

    @Query("SELECT * FROM planner_weeks")
    suspend fun getAllWeeks(): List<PlannerWeekEntity>

    @Query("SELECT * FROM planner_slots")
    suspend fun getAllSlots(): List<PlannerSlotEntity>

    @Query("SELECT * FROM planner_reports")
    suspend fun getAllReports(): List<PlannerReportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWeeks(items: List<PlannerWeekEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSlots(items: List<PlannerSlotEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllReports(items: List<PlannerReportEntity>)
}
