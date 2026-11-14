package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate

class DuaRepository(private val database: AppDatabase) {
    private val dao = database.duaDao()

    fun observeDuas(profileId: Long): Flow<List<DuaEntryEntity>> = dao.observeDuas(profileId)

    suspend fun saveDua(dua: DuaEntryEntity) {
        val now = System.currentTimeMillis()
        if (dua.id == 0L) {
            dao.insert(dua.copy(createdAt = now, updatedAt = now))
        } else {
            dao.update(dua.copy(updatedAt = now))
        }
    }

    suspend fun toggleFavorite(dua: DuaEntryEntity) {
        dao.update(dua.copy(isFavorite = !dua.isFavorite, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteDua(dua: DuaEntryEntity) {
        dao.delete(dua)
    }
}

class SeasonalRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.seasonalDao()

    fun observeSelections(profileId: Long): Flow<List<SeasonalSelectionEntity>> =
        dao.observeSelections(profileId)

    suspend fun saveSelection(selection: SeasonalSelectionEntity) {
        val id = dao.upsert(selection)
        val saved = if (selection.id == 0L) selection.copy(id = id) else selection
        scheduler.scheduleSeasonalReminder(saved)
    }
}

data class ProfileStatisticsSnapshot(
    val todayCompleted: Int = 0,
    val todayPending: Int = 0,
    val todayMissed: Int = 0,
    val weekCompleted: Int = 0,
    val weekMissed: Int = 0,
    val monthCompleted: Int = 0,
    val monthMissed: Int = 0,
    val consecutiveDays: Int = 0,
    val quranWeekTotal: Double = 0.0,
    val quranMonthTotal: Double = 0.0,
    val memSessionsCount: Int = 0,
    val revSessionsCount: Int = 0,
    val completedFastingCount: Int = 0,
    val totalTasbeehToday: Int = 0
)

class StatisticsRepository(private val database: AppDatabase) {

    fun observeStatistics(profileId: Long): Flow<ProfileStatisticsSnapshot> {
        return combine(
            database.worshipDao().observeLogs(profileId),
            database.quranDao().observeLogs(profileId),
            database.memorizationDao().observeSessions(profileId),
            database.fastingDao().observeFastingDays(profileId),
            database.tasbeehDao().observeLogs(profileId),
            com.rafiqaldhikr.app.core.calendar.todayFlow()
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val activityLogs = values[0] as List<com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity>
            @Suppress("UNCHECKED_CAST")
            val quranLogs = values[1] as List<com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity>
            @Suppress("UNCHECKED_CAST")
            val memSessions = values[2] as List<com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity>
            @Suppress("UNCHECKED_CAST")
            val fastingDays = values[3] as List<com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity>
            @Suppress("UNCHECKED_CAST")
            val tasbeehLogs = values[4] as List<com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity>
            val today = values[5] as LocalDate
            val todayEpoch = today.toEpochDay()
            val weekStartEpoch = today.minusDays(6).toEpochDay()
            val monthStartEpoch = today.minusDays(29).toEpochDay()

            val todayLogs = activityLogs.filter { it.dateEpochDay == todayEpoch }
            val weekLogs = activityLogs.filter { it.dateEpochDay in weekStartEpoch..todayEpoch }
            val monthLogs = activityLogs.filter { it.dateEpochDay in monthStartEpoch..todayEpoch }

            val completedDays = activityLogs
                .filter { it.status == ActivityStatus.COMPLETED }
                .map { it.dateEpochDay }
                .toSet()

            var streak = 0
            var cursor = if (completedDays.contains(todayEpoch)) todayEpoch else todayEpoch - 1
            while (completedDays.contains(cursor)) {
                streak++
                cursor--
            }

            ProfileStatisticsSnapshot(
                todayCompleted = todayLogs.count { it.status == ActivityStatus.COMPLETED },
                todayPending = todayLogs.count {
                    it.status == ActivityStatus.PENDING || it.status == ActivityStatus.RESCHEDULED
                },
                todayMissed = todayLogs.count { it.status == ActivityStatus.MISSED },
                weekCompleted = weekLogs.count { it.status == ActivityStatus.COMPLETED },
                weekMissed = weekLogs.count { it.status == ActivityStatus.MISSED },
                monthCompleted = monthLogs.count { it.status == ActivityStatus.COMPLETED },
                monthMissed = monthLogs.count { it.status == ActivityStatus.MISSED },
                consecutiveDays = streak,
                quranWeekTotal = quranLogs.filter { it.dateEpochDay in weekStartEpoch..todayEpoch }
                    .sumOf { it.amountRead },
                quranMonthTotal = quranLogs.filter { it.dateEpochDay in monthStartEpoch..todayEpoch }
                    .sumOf { it.amountRead },
                memSessionsCount = memSessions.count { !it.isRevision },
                revSessionsCount = memSessions.count { it.isRevision },
                completedFastingCount = fastingDays.count { it.isCompleted },
                totalTasbeehToday = tasbeehLogs.filter { it.dateEpochDay == todayEpoch }.sumOf { it.count }
            )
        }
    }
}
