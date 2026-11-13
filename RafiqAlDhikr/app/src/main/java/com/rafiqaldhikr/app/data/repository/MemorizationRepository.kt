package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.QuranSurahs
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class MemorizationRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.memorizationDao()

    fun observePlans(profileId: Long): Flow<List<MemorizationPlanEntity>> = dao.observePlans(profileId)
    fun observeSessions(profileId: Long): Flow<List<MemorizationSessionEntity>> = dao.observeSessions(profileId)

    suspend fun savePlan(plan: MemorizationPlanEntity): Long {
        val maxAyah = QuranSurahs.byNumber(plan.surahNumber).ayahCount
        val safeStart = plan.startAyah.coerceIn(1, maxAyah)
        val safeEnd = plan.endAyah.coerceIn(safeStart, maxAyah)
        val safeCurrent = plan.currentAyah.coerceIn(safeStart - 1, safeEnd)
        val normalized = plan.copy(
            startAyah = safeStart,
            endAyah = safeEnd,
            currentAyah = safeCurrent
        )
        val id = if (normalized.id == 0L) {
            dao.insertPlan(normalized)
        } else {
            dao.updatePlan(normalized)
            normalized.id
        }
        val saved = dao.getPlanById(id) ?: normalized.copy(id = id)
        scheduler.scheduleMemorizationReminders(saved)
        return id
    }

    suspend fun deletePlan(plan: MemorizationPlanEntity) {
        scheduler.scheduleMemorizationReminders(plan.copy(isActive = false))
        dao.deletePlan(plan)
    }

    suspend fun recordSession(
        plan: MemorizationPlanEntity,
        isRevision: Boolean,
        fromAyah: Int,
        toAyah: Int,
        notes: String = "",
        date: LocalDate = LocalDate.now()
    ) {
        val safeFrom = fromAyah.coerceIn(plan.startAyah, plan.endAyah)
        val safeTo = toAyah.coerceIn(safeFrom, plan.endAyah)
        dao.insertSession(
            MemorizationSessionEntity(
                profileId = plan.profileId,
                planId = plan.id,
                isRevision = isRevision,
                fromAyah = safeFrom,
                toAyah = safeTo,
                dateEpochDay = date.toEpochDay(),
                notes = notes.trim()
            )
        )
        if (!isRevision && safeTo > plan.currentAyah) {
            dao.updatePlan(plan.copy(currentAyah = safeTo))
        }
    }

    suspend fun deleteSession(session: MemorizationSessionEntity) {
        dao.deleteSession(session)
    }
}
