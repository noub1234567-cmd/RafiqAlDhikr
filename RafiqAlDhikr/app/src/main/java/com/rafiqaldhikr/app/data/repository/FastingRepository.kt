package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.FastingOccasion
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FastingRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.fastingDao()

    fun observeFastingDays(profileId: Long): Flow<List<FastingDayEntity>> =
        dao.observeFastingDays(profileId)

    suspend fun saveFastingDay(
        profileId: Long,
        date: LocalDate,
        occasion: FastingOccasion,
        isPlanned: Boolean,
        isCompleted: Boolean,
        reminderEnabled: Boolean,
        reminderHour: Int = 20,
        reminderMinute: Int = 0,
        notes: String = ""
    ) {
        val epochDay = date.toEpochDay()
        val existing = dao.getByDate(profileId, epochDay)
        val entity = (existing ?: FastingDayEntity(
            profileId = profileId,
            dateEpochDay = epochDay
        )).copy(
            occasion = occasion,
            isPlanned = isPlanned,
            isCompleted = isCompleted,
            reminderEnabled = reminderEnabled,
            reminderHour = reminderHour,
            reminderMinute = reminderMinute,
            notes = notes.trim()
        )
        dao.upsert(entity)
        scheduler.scheduleFastingReminder(entity)
    }

    suspend fun markCompleted(day: FastingDayEntity, completed: Boolean) {
        val updated = day.copy(isCompleted = completed)
        dao.upsert(updated)
        scheduler.scheduleFastingReminder(updated)
    }

    suspend fun removeFastingDay(day: FastingDayEntity) {
        scheduler.scheduleFastingReminder(day.copy(reminderEnabled = false))
        dao.delete(day)
    }
}
