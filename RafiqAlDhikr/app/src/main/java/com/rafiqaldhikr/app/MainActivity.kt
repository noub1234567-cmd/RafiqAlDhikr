package com.rafiqaldhikr.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.data.local.datastore.AppSettingsSnapshot
import com.rafiqaldhikr.app.ui.navigation.RafiqNavHost
import com.rafiqaldhikr.app.ui.theme.RafiqAlDhikrTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as RafiqApplication
        setContent {
            val settings by app.container.preferences.settingsFlow
                .collectAsStateWithLifecycle(initialValue = AppSettingsSnapshot())

            RafiqAlDhikrTheme(themeMode = settings.themeMode) {
                RafiqNavHost(container = app.container)
            }
        }
    }
}
