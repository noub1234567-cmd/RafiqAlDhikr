package com.rafiqaldhikr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.security.AppLock
import com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot
import com.rafiqaldhikr.app.ui.navigation.RafiqNavHost
import com.rafiqaldhikr.app.ui.theme.RafiqAlDhikrTheme

class MainActivity : ComponentActivity() {

    private var unlocked by androidx.compose.runtime.mutableStateOf(true)
    private var awaitingCredential = false

    private companion object {
        // Process-wide so that rotating the screen neither re-prompts nor bypasses the lock.
        @Volatile var sessionUnlocked = false
        @Volatile var lastStopMillis = 0L
    }

    private val credentialLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        awaitingCredential = false
        if (result.resultCode == RESULT_OK) {
            sessionUnlocked = true
            unlocked = true
            lastStopMillis = System.currentTimeMillis()
        }
    }

    private fun requestUnlock() {
        val intent = AppLock.credentialIntent(this)
        if (intent == null) {
            // The phone's screen lock was removed after the lock was enabled: do not trap the user.
            AppLock.setEnabled(this, false)
            sessionUnlocked = true
            unlocked = true
            return
        }
        awaitingCredential = true
        credentialLauncher.launch(intent)
    }

    private fun applyLockWindowFlags() {
        // While the lock is on, hide the app in the recents screen and from screenshots.
        if (AppLock.isEnabled(this)) {
            window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onStart() {
        super.onStart()
        applyLockWindowFlags()
        if (!AppLock.isEnabled(this)) {
            sessionUnlocked = true
            unlocked = true
        } else if (!awaitingCredential) {
            val away = System.currentTimeMillis() - lastStopMillis
            if (!sessionUnlocked || away > AppLock.RELOCK_AFTER_MILLIS) {
                sessionUnlocked = false
                unlocked = false
                requestUnlock()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as RafiqApplication
        unlocked = !AppLock.isEnabled(this) || sessionUnlocked

        if (savedInstanceState == null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val granted = androidx.core.content.ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!granted) {
                registerForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { }.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            val settings by app.container.preferences.settingsFlow
                .collectAsStateWithLifecycle(initialValue = AppSettingsSnapshot())

            RafiqAlDhikrTheme(themeMode = settings.themeMode) {
                var onboardingDone by androidx.compose.runtime.remember {
                    androidx.compose.runtime.mutableStateOf(com.rafiqaldhikr.app.core.health.Onboarding.isDone(this@MainActivity))
                }
                if (unlocked && !onboardingDone) {
                    com.rafiqaldhikr.app.ui.screens.onboarding.OnboardingScreen(onFinish = { onboardingDone = true })
                } else if (unlocked) {
                    RafiqNavHost(container = app.container)
                } else {
                    com.rafiqaldhikr.app.ui.components.LockScreen(onUnlock = { requestUnlock() })
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (!awaitingCredential) lastStopMillis = System.currentTimeMillis()
        // Keep the home-screen widget in step with what the user just did.
        com.rafiqaldhikr.app.widget.TodayWidgetProvider.refresh(this)
        com.rafiqaldhikr.app.widget.TasbeehWidgetProvider.refresh(this)
    }

    // Volume buttons count tasbeeh while that screen is open (see VolumeKeyBridge).
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        val handler = com.rafiqaldhikr.app.core.tasbeeh.VolumeKeyBridge.handler
        if (handler != null &&
            (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
        ) {
            if (event?.repeatCount == 0) handler()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (com.rafiqaldhikr.app.core.tasbeeh.VolumeKeyBridge.handler != null &&
            (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_UP || keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN)
        ) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }
}
