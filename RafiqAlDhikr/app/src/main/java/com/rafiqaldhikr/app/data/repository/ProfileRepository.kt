package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.model.GoalPeriod
import com.rafiqaldhikr.app.core.model.QuranGoalType
import com.rafiqaldhikr.app.core.model.ScheduleType
import com.rafiqaldhikr.app.core.model.VerifiedAdhkarCatalog
import com.rafiqaldhikr.app.data.local.datastore.UserPreferencesDataStore
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import com.rafiqaldhikr.app.data.local.db.entities.UserProfileEntity
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

class ProfileRepository(
    private val database: AppDatabase,
    private val preferences: UserPreferencesDataStore,
    private val scheduler: com.rafiqaldhikr.app.notifications.ReminderScheduler? = null
) {
    private val profileDao = database.profileDao()

    val profilesFlow: Flow<List<UserProfileEntity>> = profileDao.observeAll()

    val activeProfileFlow: Flow<UserProfileEntity?> = combine(
        profileDao.observeAll(),
        preferences.settingsFlow
    ) { profiles, settings ->
        profiles.firstOrNull { it.id == settings.activeProfileId } ?: profiles.firstOrNull()
    }

    suspend fun ensureInitialized(): UserProfileEntity {
        val existing = profileDao.getAll()
        if (existing.isEmpty()) {
            val defaultId = createProfile(
                name = "الملف الشخصي الرئيسي",
                avatarColorHex = 0xFF1B5E4A,
                note = "الملف الافتراضي",
                isDefault = true
            )
            preferences.setActiveProfileId(defaultId)
            return profileDao.getById(defaultId)!!
        }
        val activeId = preferences.getSnapshot().activeProfileId
        val active = existing.firstOrNull { it.id == activeId } ?: existing.first()
        if (active.id != activeId) {
            preferences.setActiveProfileId(active.id)
        }
        return active
    }

    suspend fun createProfile(
        name: String,
        avatarColorHex: Long = 0xFF1B5E4A,
        note: String = "",
        isDefault: Boolean = false
    ): Long {
        val trimmed = name.trim().ifBlank { "مستخدم جديد" }
        // One transaction: a failure while seeding must not leave a half-initialised profile behind.
        return database.withTransaction {
            val newId = profileDao.insert(
                UserProfileEntity(
                    name = trimmed,
                    avatarColorHex = avatarColorHex,
                    note = note.trim(),
                    isDefault = isDefault
                )
            )
            seedProfileDefaults(newId)
            newId
        }
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        profileDao.update(profile.copy(name = profile.name.trim().ifBlank { profile.name }))
    }

    suspend fun switchProfile(profileId: Long) {
        if (profileDao.getById(profileId) != null) {
            preferences.setActiveProfileId(profileId)
        }
    }

    suspend fun deleteProfile(profile: UserProfileEntity): Boolean {
        val all = profileDao.getAll()
        if (all.size <= 1) return false
        scheduler?.cancelAllForProfile(profile.id)
        profileDao.delete(profile)
        val remaining = profileDao.getAll()
        val currentActive = preferences.getSnapshot().activeProfileId
        if (currentActive == profile.id && remaining.isNotEmpty()) {
            preferences.setActiveProfileId(remaining.first().id)
        }
        return true
    }

    suspend fun seedProfileDefaults(profileId: Long) {
        // 1. Seed verified Adhkar entries
        val adhkarEntries = VerifiedAdhkarCatalog.defaultEntries.mapIndexed { idx, item ->
            AdhkarEntryEntity(
                profileId = profileId,
                templateKey = item.key,
                category = item.category,
                title = item.title,
                arabicText = item.arabicText,
                targetCount = item.targetCount,
                sourceReference = item.sourceReference,
                isCustom = false,
                sortOrder = idx
            )
        }
        database.adhkarDao().insertEntries(adhkarEntries)

        // 2. Seed Adhkar category reminder settings (off by default, ready to customize)
        val reminders = AdhkarCategory.entries.map { cat ->
            AdhkarReminderEntity(
                profileId = profileId,
                category = cat,
                enabled = false,
                hour = cat.defaultHour,
                minute = cat.defaultMinute
            )
        }
        database.adhkarDao().insertAllReminders(reminders)

        // 3. Seed optional built-in acts of worship (some disabled by default, all optional)
        val todayEpoch = LocalDate.now().toEpochDay()
        val fridayMask = 1 shl (DayOfWeek.FRIDAY.value - 1)
        val builtInActs = listOf(
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "duha",
                title = "صلاة الضحى",
                description = "ركعتان فأكثر في وقت الضحى تقرّباً إلى الله تعالى.",
                category = ActivityCategory.PRAYER_SUNNAH,
                goalPeriod = GoalPeriod.DAILY,
                scheduleType = ScheduleType.DAILY,
                reminderHour = 9,
                reminderMinute = 0,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "qiyam",
                title = "قيام الليل والوتر",
                description = "ركعات في جوف الليل أو قبل النوم مع صلاة الوتر.",
                category = ActivityCategory.PRAYER_SUNNAH,
                goalPeriod = GoalPeriod.DAILY,
                scheduleType = ScheduleType.DAILY,
                reminderHour = 3,
                reminderMinute = 30,
                isEnabled = false,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "charity",
                title = "الصدقة",
                description = "صدقة مالية أو معنوية ولو بالقليل أو الكلمة الطيبة.",
                category = ActivityCategory.CHARITY_TIES,
                goalPeriod = GoalPeriod.WEEKLY,
                scheduleType = ScheduleType.SPECIFIC_WEEKDAYS,
                weekdaysMask = fridayMask,
                reminderHour = 10,
                reminderMinute = 0,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "family_ties",
                title = "صلة الرحم",
                description = "التواصل مع الأقارب بالسؤال والزيارة والدعاء.",
                category = ActivityCategory.CHARITY_TIES,
                goalPeriod = GoalPeriod.WEEKLY,
                scheduleType = ScheduleType.SPECIFIC_WEEKDAYS,
                weekdaysMask = fridayMask,
                reminderHour = 17,
                reminderMinute = 0,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "parents_kindness",
                title = "بر الوالدين",
                description = "الإحسان إليهما والدعاء لهما والتلطف في الحديث معهما.",
                category = ActivityCategory.CHARITY_TIES,
                goalPeriod = GoalPeriod.DAILY,
                scheduleType = ScheduleType.DAILY,
                reminderHour = 18,
                reminderMinute = 0,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "friday_kahf",
                title = "قراءة سورة الكهف يوم الجمعة",
                description = "تلاوة سورة الكهف في يوم الجمعة.",
                category = ActivityCategory.FRIDAY,
                goalPeriod = GoalPeriod.WEEKLY,
                scheduleType = ScheduleType.SPECIFIC_WEEKDAYS,
                weekdaysMask = fridayMask,
                reminderHour = 10,
                reminderMinute = 30,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            ),
            WorshipActivityEntity(
                profileId = profileId,
                templateKey = "friday_salawat",
                title = "الصلاة على النبي ﷺ يوم الجمعة",
                description = "الإكثار من الصلاة على النبي محمد ﷺ في يوم الجمعة وليلته.",
                category = ActivityCategory.FRIDAY,
                goalPeriod = GoalPeriod.WEEKLY,
                scheduleType = ScheduleType.SPECIFIC_WEEKDAYS,
                weekdaysMask = fridayMask,
                reminderHour = 14,
                reminderMinute = 0,
                isEnabled = true,
                isOptionalBuiltIn = true,
                anchorEpochDay = todayEpoch
            )
        )
        database.worshipDao().insertActivities(builtInActs)

        // 4. Seed default Quran goal
        database.quranDao().upsertGoal(
            QuranGoalEntity(
                profileId = profileId,
                goalType = QuranGoalType.PAGES,
                customUnitLabel = "صفحة",
                targetAmount = 4.0,
                reminderEnabled = false,
                reminderHour = 19,
                reminderMinute = 0
            )
        )

        // 5. Seed default Tasbeeh types
        val tasbeehTypes = listOf(
            TasbeehTypeEntity(profileId = profileId, title = "الاستغفار", phraseArabic = "أستغفر الله العظيم وأتوب إليه", dailyTarget = 100, isBuiltIn = true, sortOrder = 0),
            TasbeehTypeEntity(profileId = profileId, title = "التسبيح", phraseArabic = "سبحان الله وبحمده", dailyTarget = 100, isBuiltIn = true, sortOrder = 1),
            TasbeehTypeEntity(profileId = profileId, title = "التحميد", phraseArabic = "الحمد لله", dailyTarget = 33, isBuiltIn = true, sortOrder = 2),
            TasbeehTypeEntity(profileId = profileId, title = "التكبير", phraseArabic = "الله أكبر", dailyTarget = 34, isBuiltIn = true, sortOrder = 3),
            TasbeehTypeEntity(profileId = profileId, title = "التهليل", phraseArabic = "لا إله إلا الله وحده لا شريك له", dailyTarget = 100, isBuiltIn = true, sortOrder = 4)
        )
        database.tasbeehDao().insertTypes(tasbeehTypes)
    }

    suspend fun getAllProfilesSnapshot(): List<UserProfileEntity> = profileDao.getAll()
    suspend fun getActiveProfileSnapshot(): UserProfileEntity? = activeProfileFlow.first()
}
