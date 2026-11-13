package com.rafiqaldhikr.app.data.backup

import com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarProgressEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.data.local.db.entities.DuaEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationSessionEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranReadingLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehLogEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val profiles: List<UserProfileEntity> = emptyList(),
    val worshipActivities: List<WorshipActivityEntity> = emptyList(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val adhkarEntries: List<AdhkarEntryEntity> = emptyList(),
    val adhkarProgress: List<AdhkarProgressEntity> = emptyList(),
    val adhkarReminders: List<AdhkarReminderEntity> = emptyList(),
    val quranGoals: List<QuranGoalEntity> = emptyList(),
    val quranReadingLogs: List<QuranReadingLogEntity> = emptyList(),
    val memorizationPlans: List<MemorizationPlanEntity> = emptyList(),
    val memorizationSessions: List<MemorizationSessionEntity> = emptyList(),
    val fastingDays: List<FastingDayEntity> = emptyList(),
    val tasbeehTypes: List<TasbeehTypeEntity> = emptyList(),
    val tasbeehLogs: List<TasbeehLogEntity> = emptyList(),
    val duaEntries: List<DuaEntryEntity> = emptyList(),
    val seasonalSelections: List<SeasonalSelectionEntity> = emptyList()
)

@Serializable
data class RafiqBackupFile(
    val appId: String = BACKUP_APP_ID,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAtMillis: Long = System.currentTimeMillis(),
    val payloadSha256: String = "",
    val settings: AppSettingsSnapshot = AppSettingsSnapshot(),
    val payload: BackupPayload = BackupPayload()
) {
    companion object {
        const val BACKUP_APP_ID = "com.rafiqaldhikr.app"
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

sealed interface BackupValidationResult {
    data class Valid(val backup: RafiqBackupFile) : BackupValidationResult
    data class Invalid(val reasonAr: String) : BackupValidationResult
}
