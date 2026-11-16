package com.rafiqaldhikr.app.data.repository

import com.rafiqaldhikr.app.core.quran.KhatmaMath
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.local.db.entities.KhatmaEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class KhatmaRepository(database: AppDatabase) {
    private val dao = database.khatmaDao()
    private val mutex = Mutex()

    fun observeActive(profileId: Long): Flow<KhatmaEntity?> = dao.observeActive(profileId)
    fun observeFinishedCount(profileId: Long): Flow<Int> = dao.observeFinishedCount(profileId)

    suspend fun start(profileId: Long, title: String, startEpochDay: Long, targetEpochDay: Long): Unit =
        mutex.withLock {
            dao.upsert(
                KhatmaEntity(
                    profileId = profileId,
                    title = title,
                    startEpochDay = startEpochDay,
                    targetEpochDay = maxOf(targetEpochDay, startEpochDay),
                    readAyahs = 0,
                    finishedEpochDay = 0
                )
            )
        }

    /** Sets the position; reaching the end of the Mushaf finishes the khatma on [todayEpoch]. */
    suspend fun setReadAyahs(id: Long, readAyahs: Int, todayEpoch: Long): Unit = mutex.withLock {
        val current = dao.getById(id) ?: return@withLock
        val clamped = readAyahs.coerceIn(0, KhatmaMath.TOTAL_AYAHS)
        dao.upsert(
            current.copy(
                readAyahs = clamped,
                finishedEpochDay = if (clamped >= KhatmaMath.TOTAL_AYAHS) todayEpoch else 0
            )
        )
    }

    suspend fun addAyahs(id: Long, ayahs: Int, todayEpoch: Long): Unit = mutex.withLock {
        val current = dao.getById(id) ?: return@withLock
        val next = (current.readAyahs + ayahs).coerceIn(0, KhatmaMath.TOTAL_AYAHS)
        dao.upsert(
            current.copy(
                readAyahs = next,
                finishedEpochDay = if (next >= KhatmaMath.TOTAL_AYAHS) todayEpoch else 0
            )
        )
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
