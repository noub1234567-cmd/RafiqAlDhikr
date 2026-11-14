package com.rafiqaldhikr.app.di

import android.content.Context
import com.rafiqaldhikr.app.data.backup.BackupManager
import com.rafiqaldhikr.app.data.local.datastore.UserPreferencesDataStore
import com.rafiqaldhikr.app.data.local.db.AppDatabase
import com.rafiqaldhikr.app.data.repository.AdhkarRepository
import com.rafiqaldhikr.app.data.repository.DuaRepository
import com.rafiqaldhikr.app.data.repository.FastingRepository
import com.rafiqaldhikr.app.data.repository.MemorizationRepository
import com.rafiqaldhikr.app.data.repository.ProfileRepository
import com.rafiqaldhikr.app.data.repository.QuranRepository
import com.rafiqaldhikr.app.data.repository.SeasonalRepository
import com.rafiqaldhikr.app.data.repository.StatisticsRepository
import com.rafiqaldhikr.app.data.repository.TasbeehRepository
import com.rafiqaldhikr.app.data.repository.WorshipRepository
import com.rafiqaldhikr.app.notifications.ReminderScheduler

interface AppContainer {
    val database: AppDatabase
    val preferences: UserPreferencesDataStore
    val reminderScheduler: ReminderScheduler
    val profileRepository: ProfileRepository
    val worshipRepository: WorshipRepository
    val adhkarRepository: AdhkarRepository
    val quranRepository: QuranRepository
    val memorizationRepository: MemorizationRepository
    val fastingRepository: FastingRepository
    val tasbeehRepository: TasbeehRepository
    val duaRepository: DuaRepository
    val seasonalRepository: SeasonalRepository
    val statisticsRepository: StatisticsRepository
    val backupManager: BackupManager
}

class AppContainerImpl(context: Context) : AppContainer {
    private val appContext = context.applicationContext

    override val database: AppDatabase by lazy { AppDatabase.create(appContext) }
    override val preferences: UserPreferencesDataStore by lazy { UserPreferencesDataStore(appContext) }
    override val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext, database) }

    override val profileRepository: ProfileRepository by lazy { ProfileRepository(database, preferences, reminderScheduler) }
    override val worshipRepository: WorshipRepository by lazy { WorshipRepository(database, reminderScheduler) }
    override val adhkarRepository: AdhkarRepository by lazy { AdhkarRepository(database, reminderScheduler) }
    override val quranRepository: QuranRepository by lazy { QuranRepository(database, reminderScheduler) }
    override val memorizationRepository: MemorizationRepository by lazy { MemorizationRepository(database, reminderScheduler) }
    override val fastingRepository: FastingRepository by lazy { FastingRepository(database, reminderScheduler) }
    override val tasbeehRepository: TasbeehRepository by lazy { TasbeehRepository(database) }
    override val duaRepository: DuaRepository by lazy { DuaRepository(database) }
    override val seasonalRepository: SeasonalRepository by lazy { SeasonalRepository(database, reminderScheduler) }
    override val statisticsRepository: StatisticsRepository by lazy { StatisticsRepository(database) }
    override val backupManager: BackupManager by lazy {
        BackupManager(appContext, database, preferences, reminderScheduler)
    }
}
