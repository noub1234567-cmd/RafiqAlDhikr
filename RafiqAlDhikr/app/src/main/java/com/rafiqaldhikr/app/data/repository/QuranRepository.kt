package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class QuranRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.quranDao()

    fun observeGoal(profileId: Long): Flow<QuranGoalEntity?> = dao.observeGoal(profileId)
    fun observeLogs(profileId: Long): Flow<List<QuranReadingLogEntity>> = dao.observeLogs(profileId)

    suspend fun saveGoal(goal: QuranGoalEntity) {
        val updated = goal.copy(updatedAt = System.currentTimeMillis())
        dao.upsertGoal(updated)
        scheduler.scheduleQuranReminder(updated)
    }

    suspend fun logReading(
        profileId: Long,
        amount: Double,
        surahNote: String = "",
        date: LocalDate = LocalDate.now()
    ) {
        val goal = dao.getGoal(profileId) ?: QuranGoalEntity(profileId = profileId)
        val unitLabel = if (goal.goalType.name == "CUSTOM") goal.customUnitLabel else goal.goalType.unitAr
        dao.insertLog(
            QuranReadingLogEntity(
                profileId = profileId,
                dateEpochDay = date.toEpochDay(),
                amountRead = amount.coerceAtLeast(0.0),
                goalType = goal.goalType,
                unitLabel = unitLabel,
                surahNote = surahNote.trim()
            )
        )
    }

    suspend fun deleteLog(log: QuranReadingLogEntity) {
        dao.deleteLog(log)
    }
}
