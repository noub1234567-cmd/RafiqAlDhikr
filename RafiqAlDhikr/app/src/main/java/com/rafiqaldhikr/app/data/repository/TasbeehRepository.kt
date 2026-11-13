package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TasbeehRepository(
    private val database: AppDatabase
) {
    private val dao = database.tasbeehDao()

    fun observeTypes(profileId: Long): Flow<List<TasbeehTypeEntity>> = dao.observeTypes(profileId)
    fun observeAllLogs(profileId: Long): Flow<List<TasbeehLogEntity>> = dao.observeLogs(profileId)
    fun observeTodayLogs(profileId: Long, date: LocalDate = LocalDate.now()): Flow<List<TasbeehLogEntity>> =
        dao.observeLogsForDay(profileId, date.toEpochDay())

    suspend fun increment(profileId: Long, typeId: Long, date: LocalDate = LocalDate.now()) {
        val epochDay = date.toEpochDay()
        val existing = dao.getLogForDay(profileId, typeId, epochDay)
        val updated = (existing ?: TasbeehLogEntity(
            profileId = profileId,
            tasbeehTypeId = typeId,
            dateEpochDay = epochDay,
            count = 0
        )).copy(
            count = (existing?.count ?: 0) + 1,
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertLog(updated)
    }

    suspend fun resetToday(profileId: Long, typeId: Long, date: LocalDate = LocalDate.now()) {
        val epochDay = date.toEpochDay()
        val existing = dao.getLogForDay(profileId, typeId, epochDay) ?: return
        dao.upsertLog(existing.copy(count = 0, updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun saveType(type: TasbeehTypeEntity) {
        if (type.id == 0L) {
            dao.insertType(type)
        } else {
            dao.updateType(type)
        }
    }

    suspend fun deleteType(type: TasbeehTypeEntity) {
        dao.deleteType(type)
    }
}
