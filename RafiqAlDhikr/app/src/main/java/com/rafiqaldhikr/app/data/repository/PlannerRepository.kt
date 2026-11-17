package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.planner.PlannerReports
import com.rafiqaldhikr.app.core.planner.PlannerWeek
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.PlannerReportEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity
import com.rafiqaldhikr.app.data.local.db.entities.PlannerWeekEntity
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate

class PlannerRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler,
    private val context: android.content.Context
) {
    private val dao = database.plannerDao()
    private val mutex = Mutex()
    private val json = Json

    fun observeWeek(profileId: Long, weekStartEpochDay: Long): Flow<PlannerWeekEntity?> =
        dao.observeWeek(profileId, weekStartEpochDay)

    fun observeSlots(weekId: Long): Flow<List<PlannerSlotEntity>> = dao.observeSlots(weekId)

    fun observePreviousCount(profileId: Long, weekStartEpochDay: Long): Flow<Int> =
        dao.observePreviousCount(profileId, weekStartEpochDay)

    /** Starts an empty table with [rows] rows for the week beginning [weekStartEpochDay]. */
    suspend fun createWeek(profileId: Long, weekStartEpochDay: Long, rows: Int): Unit = mutex.withLock {
        if (dao.getWeek(profileId, weekStartEpochDay) != null) return@withLock
        dao.upsertWeek(
            PlannerWeekEntity(
                profileId = profileId,
                weekStartEpochDay = weekStartEpochDay,
                rowCount = PlannerWeek.clampRows(rows),
                autoRepeat = false
            )
        )
    }

    /** Copies the most recent earlier week (rows, cells, alarms; nothing marked done) into this week. */
    suspend fun copyLastWeek(profileId: Long, weekStartEpochDay: Long): Unit = mutex.withLock {
        if (dao.getWeek(profileId, weekStartEpochDay) != null) return@withLock
        val previous = dao.getWeeksBefore(profileId, weekStartEpochDay).lastOrNull() ?: return@withLock
        copyInto(previous, weekStartEpochDay, previous.autoRepeat)
    }

    private suspend fun copyInto(from: PlannerWeekEntity, weekStartEpochDay: Long, autoRepeat: Boolean) {
        val newWeek = from.copy(id = 0, weekStartEpochDay = weekStartEpochDay, autoRepeat = autoRepeat)
        val newId = dao.upsertWeek(newWeek)
        val saved = newWeek.copy(id = newId)
        dao.getSlotsForWeek(from.id).forEach { old ->
            val id = dao.upsertSlot(old.copy(id = 0, weekId = newId, doneAtMillis = 0))
            scheduler.schedulePlannerSlot(old.copy(id = id, weekId = newId, doneAtMillis = 0), saved)
        }
    }

    /** Adds or removes rows. Removing rows deletes the cells in them. */
    suspend fun setRows(weekId: Long, rows: Int): Unit = mutex.withLock {
        val week = dao.getWeekById(weekId) ?: return@withLock
        val target = PlannerWeek.clampRows(rows)
        if (target < week.rowCount) {
            dao.getSlotsForWeek(weekId).filter { it.rowIndex >= target }.forEach { scheduler.cancelPlannerSlot(it.id) }
            dao.deleteSlotsFromRow(weekId, target)
        }
        dao.setRowCount(weekId, target)
    }

    suspend fun setAutoRepeat(weekId: Long, enabled: Boolean) = dao.setAutoRepeat(weekId, enabled)

    /**
     * Saves one cell. When [slotId] is non-null that slot is edited; the same worship is also copied
     * into every free cell of [extraDays] in the same row (independent copies sharing a groupId).
     */
    suspend fun saveSlot(
        weekId: Long,
        slotId: Long?,
        rowIndex: Int,
        dayIndex: Int,
        title: String,
        type: String,
        timeMinutes: Int,
        extraDays: Set<Int>
    ): Unit = mutex.withLock {
        val week = dao.getWeekById(weekId) ?: return@withLock
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || rowIndex !in 0 until week.rowCount || dayIndex !in 0 until PlannerWeek.DAYS) {
            return@withLock
        }
        val existing = slotId?.let { dao.getSlotById(it) }
        val taken = dao.getSlotsForWeek(weekId)
            .filter { it.rowIndex == rowIndex && it.id != existing?.id }
            .map { it.dayIndex }.toSet()
        if (dayIndex in taken) return@withLock
        val groupId = existing?.groupId ?: System.currentTimeMillis()
        val time = if (timeMinutes in 0 until 24 * 60) timeMinutes else PlannerWeek.NO_TIME

        val main = (existing ?: PlannerSlotEntity(
            weekId = weekId, rowIndex = rowIndex, dayIndex = dayIndex, title = cleanTitle,
            type = type, timeMinutes = time, groupId = groupId, doneAtMillis = 0
        )).copy(title = cleanTitle, type = type, timeMinutes = time, rowIndex = rowIndex, dayIndex = dayIndex)
        val mainId = dao.upsertSlot(main)
        scheduler.schedulePlannerSlot(main.copy(id = if (main.id == 0L) mainId else main.id), week)

        extraDays.filter { it in 0 until PlannerWeek.DAYS && it != dayIndex && it !in taken }.forEach { day ->
            val copy = PlannerSlotEntity(
                weekId = weekId, rowIndex = rowIndex, dayIndex = day, title = cleanTitle,
                type = type, timeMinutes = time, groupId = groupId, doneAtMillis = 0
            )
            val id = dao.upsertSlot(copy)
            scheduler.schedulePlannerSlot(copy.copy(id = id), week)
        }
    }

    suspend fun toggleDone(slotId: Long, nowMillis: Long): Unit = mutex.withLock {
        val slot = dao.getSlotById(slotId) ?: return@withLock
        val updated = slot.copy(doneAtMillis = if (slot.doneAtMillis == 0L) nowMillis else 0L)
        dao.upsertSlot(updated)
        val week = dao.getWeekById(slot.weekId) ?: return@withLock
        scheduler.schedulePlannerSlot(updated, week)
    }

    suspend fun deleteSlot(slotId: Long): Unit = mutex.withLock {
        scheduler.cancelPlannerSlot(slotId)
        dao.deleteSlot(slotId)
    }

    fun observeReports(profileId: Long, kind: String): Flow<List<PlannerReportEntity>> =
        dao.observeReports(profileId, kind)

    suspend fun deleteReport(id: Long) = dao.deleteReport(id)

    /**
     * Turns the table over for every profile: archives each finished week into a report (never
     * deleted automatically), keeps only the latest finished week for "copy last week", and
     * copies it into the new week when auto-repeat was on.
     */
    suspend fun rolloverAll(today: LocalDate): Unit = mutex.withLock {
        val currentStart = PlannerWeek.weekStart(today).toEpochDay()
        database.profileDao().getAll().forEach { profile ->
            val past = dao.getWeeksBefore(profile.id, currentStart)
            past.forEach { week ->
                val slots = dao.getSlotsForWeek(week.id)
                slots.forEach { scheduler.cancelPlannerSlot(it.id) }
                if (slots.isNotEmpty()) {
                    val lines = PlannerReports.summarize(slots.map { Triple(it.title, it.type, it.doneAtMillis != 0L) })
                    dao.insertReport(
                        PlannerReportEntity(
                            profileId = profile.id,
                            kind = KIND_WEEK,
                            startEpochDay = week.weekStartEpochDay,
                            endEpochDay = week.weekStartEpochDay + PlannerWeek.DAYS - 1,
                            summaryJson = json.encodeToString(lines),
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                }
            }
            val latest = past.lastOrNull()
            if (latest != null && latest.autoRepeat && dao.getWeek(profile.id, currentStart) == null) {
                copyInto(latest, currentStart, autoRepeat = true)
            }
            archiveMonths(profile.id, currentStart)
            // Empty finished weeks and anything older than the latest are no longer needed.
            past.forEach { week ->
                if (week.id != latest?.id || dao.getSlotsForWeek(week.id).isEmpty()) dao.deleteWeek(week.id)
            }
        }
    }

    /**
     * Once 30 days have passed since the first archived week (and a new week has begun), merges the
     * weekly reports of that period into one monthly report. Weekly reports are kept.
     */
    private suspend fun archiveMonths(profileId: Long, currentStart: Long) {
        val prefs = context.getSharedPreferences("planner_state", android.content.Context.MODE_PRIVATE)
        val key = "month_end_$profileId"
        val weeks = dao.getReports(profileId, KIND_WEEK)
        var lastEnd = maxOf(
            prefs.getLong(key, Long.MIN_VALUE),
            dao.getReports(profileId, KIND_MONTH).maxOfOrNull { it.endEpochDay } ?: Long.MIN_VALUE
        )
        while (true) {
            val pending = weeks.filter { it.startEpochDay > lastEnd }
            val periodStart = pending.minOfOrNull { it.startEpochDay } ?: break
            if (currentStart < periodStart + PlannerReports.MONTH_DAYS) break
            val inPeriod = pending.filter { it.startEpochDay < periodStart + PlannerReports.MONTH_DAYS }
            val lines = PlannerReports.merge(inPeriod.map { r ->
                runCatching { json.decodeFromString<List<com.rafiqaldhikr.app.core.planner.ReportLine>>(r.summaryJson) }
                    .getOrDefault(emptyList())
            })
            val end = periodStart + PlannerReports.MONTH_DAYS - 1
            dao.insertReport(
                PlannerReportEntity(
                    profileId = profileId, kind = KIND_MONTH, startEpochDay = periodStart, endEpochDay = end,
                    summaryJson = json.encodeToString(lines), createdAtMillis = System.currentTimeMillis()
                )
            )
            lastEnd = end
            prefs.edit().putLong(key, end).apply()
        }
    }

    companion object {
        const val KIND_WEEK = "WEEK"
        const val KIND_MONTH = "MONTH"
    }
}
