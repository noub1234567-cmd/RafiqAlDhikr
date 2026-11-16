package com.rafiqaldhikr.app

import android.app.Application
import com.rafiqaldhikr.app.di.AppContainer
import com.rafiqaldhikr.app.di.AppContainerImpl
import com.rafiqaldhikr.app.notifications.MissedActivityWorker
import com.rafiqaldhikr.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RafiqApplication : Application() {

    lateinit var container: AppContainer
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        NotificationHelper.ensureChannels(this)
        MissedActivityWorker.enqueuePeriodic(this)
        com.rafiqaldhikr.app.notifications.AutoBackupWorker.enqueuePeriodic(this)

        appScope.launch {
            container.profileRepository.ensureInitialized()
            runCatching { com.rafiqaldhikr.app.data.repository.AdhkarCatalogSync.run(this@RafiqApplication, container.database) }
            container.worshipRepository.evaluateMissedActivitiesForAllProfiles()
            // Alarms are lost after a force-stop; re-arm them whenever the app process starts.
            container.reminderScheduler.rescheduleAll()
        }
    }
}
