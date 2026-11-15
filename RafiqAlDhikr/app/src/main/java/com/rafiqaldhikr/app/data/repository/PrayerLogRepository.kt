package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.PrayerLogEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Items tracked in the prayer tracker. The key is what is stored in the database. */
enum class PrayerItem(val key: String, val labelAr: String, val isFard: Boolean) {
    FAJR("FAJR", "الفجر", true),
    DHUHR("DHUHR", "الظهر", true),
    ASR("ASR", "العصر", true),
    MAGHRIB("MAGHRIB", "المغرب", true),
    ISHA("ISHA", "العشاء", true),

    FAJR_SUNNAH("FAJR_SUNNAH", "ركعتا الفجر", false),
    DHUHR_BEFORE("DHUHR_BEFORE", "أربع قبل الظهر", false),
    DHUHR_AFTER("DHUHR_AFTER", "ركعتان بعد الظهر", false),
    MAGHRIB_AFTER("MAGHRIB_AFTER", "ركعتان بعد المغرب", false),
    ISHA_AFTER("ISHA_AFTER", "ركعتان بعد العشاء", false),
    DUHA("DUHA", "صلاة الضحى", false),
    WITR("WITR", "الوتر", false)
}

class PrayerLogRepository(private val database: AppDatabase) {
    private val dao = database.prayerLogDao()
    private val mutex = Mutex()

    fun observeRange(profileId: Long, fromDay: Long, toDay: Long): Flow<List<PrayerLogEntity>> =
        dao.observeRange(profileId, fromDay, toDay)

    /** Ticks the item if it is not ticked, clears it otherwise. */
    suspend fun toggle(profileId: Long, day: Long, item: PrayerItem): Unit = mutex.withLock {
        val removed = dao.delete(profileId, day, item.key)
        if (removed == 0) {
            dao.insert(
                PrayerLogEntity(
                    profileId = profileId,
                    dateEpochDay = day,
                    itemKey = item.key,
                    completedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }
}
