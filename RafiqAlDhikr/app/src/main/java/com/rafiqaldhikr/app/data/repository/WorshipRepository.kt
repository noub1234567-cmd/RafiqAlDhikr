package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.notifications.MissedActivityEvaluator
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class WorshipRepository(
    private val database: AppDatabase,
    private val scheduler: ReminderScheduler
) {
    private val dao = database.worshipDao()

    fun observeActivities(profileId: Long): Flow<List<WorshipActivityEntity>> =
        dao.observeActivities(profileId)

    fun observeLogs(profileId: Long): Flow<List<ActivityLogEntity>> =
        dao.observeLogs(profileId)

    fun observeLogsForDay(profileId: Long, date: LocalDate): Flow<List<ActivityLogEntity>> =
        dao.observeLogsForDay(profileId, date.toEpochDay())

    suspend fun saveActivity(activity: WorshipActivityEntity): Long {
        val id = if (activity.id == 0L) {
            dao.insertActivity(activity)
        } else {
            dao.updateActivity(activity)
            activity.id
        }
        val saved = dao.getActivityById(id) ?: activity.copy(id = id)
        ensureTodayLogExists(saved)
        scheduler.scheduleActivityReminder(saved)
        return id
    }

    suspend fun toggleActivityEnabled(activity: WorshipActivityEntity, enabled: Boolean) {
        val updated = activity.copy(isEnabled = enabled)
        dao.updateActivity(updated)
        scheduler.scheduleActivityReminder(updated)
    }

    suspend fun deleteActivity(activity: WorshipActivityEntity) {
        val disabled = activity.copy(isEnabled = false, reminderEnabled = false)
        scheduler.scheduleActivityReminder(disabled)
        dao.deleteActivity(activity)
    }

    suspend fun ensureTodayLogsAndEvaluate(profileId: Long, date: LocalDate = LocalDate.now()) {
        val now = System.currentTimeMillis()
        val activities = dao.getActivitiesByProfile(profileId)
        for (activity in activities) {
            if (MissedActivityEvaluator.isScheduledOnDate(activity, date)) {
                ensureLogExistsForDate(activity, date)
            }
        }
        val logs = dao.getLogsForDay(profileId, date.toEpochDay())
        for (log in logs) {
            val evaluated = MissedActivityEvaluator.evaluateStatus(log, now)
            if (evaluated != log) {
                dao.updateLog(evaluated)
            }
        }
    }

    suspend fun evaluateMissedActivitiesForAllProfiles() {
        val profiles = database.profileDao().getAll()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        for (profile in profiles) {
            ensureTodayLogsAndEvaluate(profile.id, yesterday)
            ensureTodayLogsAndEvaluate(profile.id, today)
        }
    }

    suspend fun markActivityCompleted(
        profileId: Long,
        activity: WorshipActivityEntity,
        date: LocalDate = LocalDate.now()
    ) {
        val log = ensureLogExistsForDate(activity, date)
        val completed = MissedActivityEvaluator.markExplicitlyCompleted(log, System.currentTimeMillis())
        dao.updateLog(completed)
    }

    suspend fun markLogCompleted(log: ActivityLogEntity) {
        val completed = MissedActivityEvaluator.markExplicitlyCompleted(log, System.currentTimeMillis())
        dao.updateLog(completed)
    }

    suspend fun revertLogToPendingOrMissed(log: ActivityLogEntity) {
        val base = log.copy(status = ActivityStatus.PENDING, completedAtMillis = null)
        val evaluated = MissedActivityEvaluator.evaluateStatus(base, System.currentTimeMillis())
        dao.updateLog(evaluated)
    }

    /**
     * Reschedules a reminder for a pending/missed activity WITHOUT marking it completed.
     */
    suspend fun rescheduleActivityReminder(
        log: ActivityLogEntity,
        newTriggerMillis: Long
    ) {
        if (log.status == ActivityStatus.COMPLETED) return
        val updatedLog = MissedActivityEvaluator.rescheduleWithoutCompleting(log, newTriggerMillis)
        dao.updateLog(updatedLog)

        val activity = dao.getActivityById(log.activityId)
        scheduler.scheduleOneOffRescheduledReminder(
            logId = updatedLog.id,
            activityId = updatedLog.activityId,
            profileId = updatedLog.profileId,
            title = "تذكير مُعاد جدولته: ${updatedLog.activityTitleSnapshot}",
            body = activity?.description?.ifBlank { "حان الموعد الجديد لتذكيرك بالعبادة." }
                ?: "حان الموعد الجديد لتذكيرك بالعبادة.",
            triggerAtMillis = newTriggerMillis
        )
    }

    suspend fun deleteLog(log: ActivityLogEntity) {
        dao.deleteLog(log)
    }

    private suspend fun ensureTodayLogExists(activity: WorshipActivityEntity) {
        val today = LocalDate.now()
        if (MissedActivityEvaluator.isScheduledOnDate(activity, today)) {
            ensureLogExistsForDate(activity, today)
        }
    }

    private suspend fun ensureLogExistsForDate(
        activity: WorshipActivityEntity,
        date: LocalDate
    ): ActivityLogEntity {
        val epochDay = date.toEpochDay()
        val existing = dao.getLogForActivityDay(activity.profileId, activity.id, epochDay)
        if (existing != null) return existing
        val scheduledMillis = MissedActivityEvaluator.scheduledMillisForDate(activity, date)
        val newLog = ActivityLogEntity(
            profileId = activity.profileId,
            activityId = activity.id,
            activityTitleSnapshot = activity.title,
            categorySnapshot = activity.category,
            dateEpochDay = epochDay,
            scheduledAtMillis = scheduledMillis,
            status = ActivityStatus.PENDING
        )
        val id = dao.insertLog(newLog)
        return newLog.copy(id = id)
    }
}
