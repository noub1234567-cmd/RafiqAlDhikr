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
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
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
    val seasonalSelections: List<SeasonalSelectionEntity> = emptyList(),
    /**
     * Omitted from the JSON while empty, so the SHA-256 of a backup made before this field existed
     * is unchanged when it is re-encoded for verification.
     */
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val prayerLogs: List<PrayerLogEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val khatmas: List<com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val plannerWeeks: List<com.rafiqaldhikr.app.data.local.db.entities.PlannerWeekEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val plannerSlots: List<com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val plannerReports: List<com.rafiqaldhikr.app.data.local.db.entities.PlannerReportEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val duaFavorites: List<com.rafiqaldhikr.app.data.local.db.entities.DuaFavoriteEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val nightReviews: List<com.rafiqaldhikr.app.data.local.db.entities.NightReviewEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val duaMemos: List<com.rafiqaldhikr.app.data.local.db.entities.DuaMemoEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val memoPlans: List<com.rafiqaldhikr.app.data.local.db.entities.MemoPlanEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val memoPlanItems: List<com.rafiqaldhikr.app.data.local.db.entities.MemoPlanItemEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val bookmarks: List<com.rafiqaldhikr.app.data.local.db.entities.BookmarkEntity> = emptyList(),
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @kotlinx.serialization.EncodeDefault(kotlinx.serialization.EncodeDefault.Mode.NEVER)
    val testResults: List<com.rafiqaldhikr.app.data.local.db.entities.TestResultEntity> = emptyList()
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
