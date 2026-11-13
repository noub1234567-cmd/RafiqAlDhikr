package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class AdhkarRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.adhkarDao()

    fun observeEntries(profileId: Long): Flow<List<AdhkarEntryEntity>> =
        dao.observeEntries(profileId)

    fun observeProgressForDay(profileId: Long, date: LocalDate = LocalDate.now()): Flow<List<AdhkarProgressEntity>> =
        dao.observeProgressForDay(profileId, date.toEpochDay())

    fun observeReminders(profileId: Long): Flow<List<AdhkarReminderEntity>> =
        dao.observeReminders(profileId)

    suspend fun incrementDhikr(
        profileId: Long,
        entry: AdhkarEntryEntity,
        date: LocalDate = LocalDate.now()
    ) {
        val epochDay = date.toEpochDay()
        val existing = dao.getProgress(profileId, entry.id, epochDay)
        val current = existing?.currentCount ?: 0
        if (current >= entry.targetCount && existing?.isCompleted == true) return
        val nextCount = (current + 1).coerceAtMost(entry.targetCount)
        val completed = nextCount >= entry.targetCount
        val progress = (existing ?: AdhkarProgressEntity(
            profileId = profileId,
            adhkarEntryId = entry.id,
            dateEpochDay = epochDay
        )).copy(
            currentCount = nextCount,
            isCompleted = completed,
            completedAtMillis = if (completed) System.currentTimeMillis() else null
        )
        dao.upsertProgress(progress)
    }

    suspend fun toggleDhikrCompleted(
        profileId: Long,
        entry: AdhkarEntryEntity,
        date: LocalDate = LocalDate.now()
    ) {
        val epochDay = date.toEpochDay()
        val existing = dao.getProgress(profileId, entry.id, epochDay)
        val nowCompleted = !(existing?.isCompleted ?: false)
        val progress = (existing ?: AdhkarProgressEntity(
            profileId = profileId,
            adhkarEntryId = entry.id,
            dateEpochDay = epochDay
        )).copy(
            currentCount = if (nowCompleted) entry.targetCount else 0,
            isCompleted = nowCompleted,
            completedAtMillis = if (nowCompleted) System.currentTimeMillis() else null
        )
        dao.upsertProgress(progress)
    }

    suspend fun resetCategoryForDay(
        profileId: Long,
        category: AdhkarCategory,
        date: LocalDate = LocalDate.now()
    ) {
        val epochDay = date.toEpochDay()
        val entries = dao.getEntriesByProfile(profileId).filter { it.category == category }
        for (entry in entries) {
            val existing = dao.getProgress(profileId, entry.id, epochDay)
            if (existing != null) {
                dao.upsertProgress(
                    existing.copy(currentCount = 0, isCompleted = false, completedAtMillis = null)
                )
            }
        }
    }

    suspend fun saveCustomEntry(entry: AdhkarEntryEntity) {
        if (entry.id == 0L) {
            dao.insertEntry(entry.copy(isCustom = true))
        } else {
            dao.updateEntry(entry)
        }
    }

    suspend fun deleteEntry(entry: AdhkarEntryEntity) {
        dao.deleteEntry(entry)
    }

    suspend fun updateReminder(reminder: AdhkarReminderEntity) {
        val id = dao.upsertReminder(reminder)
        val saved = if (reminder.id == 0L) reminder.copy(id = id) else reminder
        scheduler.scheduleAdhkarReminder(saved)
    }
}
