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

        appScope.launch {
            container.profileRepository.ensureInitialized()
            container.worshipRepository.evaluateMissedActivitiesForAllProfiles()
        }
    }
}
