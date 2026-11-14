package com.rafiqaldhikr.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.rafiqaldhikr.app.data.local.datastore.UserPreferencesDataStore
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.notifications.ReminderScheduler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest

object BackupCodec {
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = false
        encodeDefaults = true
    }

    fun sha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun encodeBackup(
        payload: BackupPayload,
        settings: com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot,
        exportedAtMillis: Long = System.currentTimeMillis()
    ): String {
        val payloadJson = json.encodeToString(payload)
        val hash = sha256(payloadJson)
        val wrapper = RafiqBackupFile(
            appId = RafiqBackupFile.BACKUP_APP_ID,
            schemaVersion = RafiqBackupFile.CURRENT_SCHEMA_VERSION,
            exportedAtMillis = exportedAtMillis,
            payloadSha256 = hash,
            settings = settings,
            payload = payload
        )
        return json.encodeToString(wrapper)
    }

    fun decodeAndValidate(rawJson: String): BackupValidationResult {
        if (rawJson.isBlank()) {
            return BackupValidationResult.Invalid("الملف المختار فارغ ولا يحتوي على بيانات.")
        }
        val parsed = runCatching {
            json.decodeFromString<RafiqBackupFile>(rawJson)
        }.getOrElse {
            return BackupValidationResult.Invalid("تعذّر قراءة ملف النسخة الاحتياطية؛ قد يكون تالفاً أو بصيغة غير متوافقة.")
        }

        if (parsed.appId != RafiqBackupFile.BACKUP_APP_ID) {
            return BackupValidationResult.Invalid("هذا الملف لا يخص تطبيق رفيقك في الذكر.")
        }

        if (parsed.schemaVersion <= 0 || parsed.schemaVersion > RafiqBackupFile.CURRENT_SCHEMA_VERSION) {
            return BackupValidationResult.Invalid("إصدار النسخة الاحتياطية غير مدعوم في هذا الإصدار من التطبيق.")
        }

        val payloadJson = json.encodeToString(parsed.payload)
        val actualHash = sha256(payloadJson)
        if (parsed.payloadSha256.isBlank() || parsed.payloadSha256 != actualHash) {
            return BackupValidationResult.Invalid("فشل التحقق من سلامة الملف؛ يبدو أن البيانات قد تغيرت أو تعرضت للتلف.")
        }

        if (parsed.payload.profiles.isEmpty()) {
            return BackupValidationResult.Invalid("النسخة الاحتياطية لا تحتوي على أي ملف شخصي صالح.")
        }

        val profileIds = parsed.payload.profiles.map { it.id }.toSet()
        val hasOrphan = parsed.payload.worshipActivities.any { it.profileId !in profileIds } ||
            parsed.payload.adhkarEntries.any { it.profileId !in profileIds } ||
            parsed.payload.duaEntries.any { it.profileId !in profileIds } ||
            parsed.payload.fastingDays.any { it.profileId !in profileIds } ||
            parsed.payload.tasbeehTypes.any { it.profileId !in profileIds } ||
            parsed.payload.activityLogs.any { it.profileId !in profileIds } ||
            parsed.payload.adhkarProgress.any { it.profileId !in profileIds } ||
            parsed.payload.adhkarReminders.any { it.profileId !in profileIds } ||
            parsed.payload.quranGoals.any { it.profileId !in profileIds } ||
            parsed.payload.quranReadingLogs.any { it.profileId !in profileIds } ||
            parsed.payload.memorizationPlans.any { it.profileId !in profileIds } ||
            parsed.payload.memorizationSessions.any { it.profileId !in profileIds } ||
            parsed.payload.tasbeehLogs.any { it.profileId !in profileIds } ||
            parsed.payload.seasonalSelections.any { it.profileId !in profileIds }

        val activityIds = parsed.payload.worshipActivities.map { it.id }.toSet()
        val adhkarIds = parsed.payload.adhkarEntries.map { it.id }.toSet()
        val planIds = parsed.payload.memorizationPlans.map { it.id }.toSet()
        val tasbeehIds = parsed.payload.tasbeehTypes.map { it.id }.toSet()
        val hasBrokenLink = parsed.payload.activityLogs.any { it.activityId !in activityIds } ||
            parsed.payload.adhkarProgress.any { it.adhkarEntryId !in adhkarIds } ||
            parsed.payload.memorizationSessions.any { it.planId !in planIds } ||
            parsed.payload.tasbeehLogs.any { it.tasbeehTypeId !in tasbeehIds }
        if (hasBrokenLink) {
            return BackupValidationResult.Invalid("بنية الملف غير متسقة (سجلات مرتبطة بعناصر غير موجودة).")
        }

        if (hasOrphan) {
            return BackupValidationResult.Invalid("بنية الملف غير متسقة (وجود سجلات غير مرتبطة بملف شخصي).")
        }

        return BackupValidationResult.Valid(parsed)
    }
}

class BackupManager(
    private val context: Context,
    private val database: AppDatabase,
    private val preferences: UserPreferencesDataStore,
    private val scheduler: ReminderScheduler
) {
    suspend fun buildBackupJson(): String {
        val payload = BackupPayload(
            profiles = database.profileDao().getAll(),
            worshipActivities = database.worshipDao().getAllActivities(),
            activityLogs = database.worshipDao().getAllLogs(),
            adhkarEntries = database.adhkarDao().getAllEntries(),
            adhkarProgress = database.adhkarDao().getAllProgress(),
            adhkarReminders = database.adhkarDao().getAllReminders(),
            quranGoals = database.quranDao().getAllGoals(),
            quranReadingLogs = database.quranDao().getAllLogs(),
            memorizationPlans = database.memorizationDao().getAllPlans(),
            memorizationSessions = database.memorizationDao().getAllSessions(),
            fastingDays = database.fastingDao().getAll(),
            tasbeehTypes = database.tasbeehDao().getAllTypes(),
            tasbeehLogs = database.tasbeehDao().getAllLogs(),
            duaEntries = database.duaDao().getAll(),
            seasonalSelections = database.seasonalDao().getAll()
        )
        val settings = preferences.getSnapshot()
        return BackupCodec.encodeBackup(payload, settings)
    }

    suspend fun exportToUri(uri: Uri): Result<Unit> = runCatching {
        val jsonText = buildBackupJson()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonText.toByteArray(Charsets.UTF_8))
            stream.flush()
        } ?: error("تعذّر فتح الملف للحفظ.")
    }

    suspend fun readAndValidateFromUri(uri: Uri): BackupValidationResult {
        val raw = runCatching {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            }
        }.getOrNull() ?: return BackupValidationResult.Invalid("تعذّر قراءة الملف المحدد.")

        return BackupCodec.decodeAndValidate(raw)
    }

    suspend fun restoreValidatedBackup(backup: RafiqBackupFile): Result<Unit> = runCatching {
        val p = backup.payload
        scheduler.cancelAllForAllProfiles()
        database.withTransaction {
            // CASCADE deletes child tables automatically when deleting all profiles
            database.profileDao().deleteAll()

            database.profileDao().insertAll(p.profiles)
            database.worshipDao().insertActivities(p.worshipActivities)
            database.worshipDao().insertLogs(p.activityLogs)
            database.adhkarDao().insertEntries(p.adhkarEntries)
            database.adhkarDao().insertAllProgress(p.adhkarProgress)
            database.adhkarDao().insertAllReminders(p.adhkarReminders)
            database.quranDao().insertAllGoals(p.quranGoals)
            database.quranDao().insertAllLogs(p.quranReadingLogs)
            database.memorizationDao().insertAllPlans(p.memorizationPlans)
            database.memorizationDao().insertAllSessions(p.memorizationSessions)
            database.fastingDao().insertAll(p.fastingDays)
            database.tasbeehDao().insertTypes(p.tasbeehTypes)
            database.tasbeehDao().insertAllLogs(p.tasbeehLogs)
            database.duaDao().insertAll(p.duaEntries)
            database.seasonalDao().insertAll(p.seasonalSelections)
        }

        val targetActiveProfile = p.profiles.firstOrNull { it.id == backup.settings.activeProfileId }?.id
            ?: p.profiles.first().id
        preferences.setActiveProfileId(targetActiveProfile)
        preferences.setThemeMode(backup.settings.themeMode)
        preferences.setHijriOffsetDays(backup.settings.hijriOffsetDays)
        preferences.setTasbeehHapticEnabled(backup.settings.tasbeehHapticEnabled)

        scheduler.rescheduleAll()
    }
}
