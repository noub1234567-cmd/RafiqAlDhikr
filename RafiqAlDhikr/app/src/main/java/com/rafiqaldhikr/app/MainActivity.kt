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
                RafiqNavHost(container = app.container)
            }
        }
    }
}
