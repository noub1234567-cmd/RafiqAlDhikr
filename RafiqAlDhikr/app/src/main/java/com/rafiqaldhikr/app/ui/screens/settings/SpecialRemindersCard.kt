package com.rafiqaldhikr.app.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.special.SpecialReminder
import com.rafiqaldhikr.app.core.special.SpecialReminders
import com.rafiqaldhikr.app.ui.components.RafiqCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SpecialRemindersCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var enabled by remember {
        mutableStateOf(SpecialReminder.entries.associateWith { SpecialReminders.isEnabled(context, it) })
    }

    RafiqCard {
        Text("تذكيرات السنن الأسبوعية والشهرية", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            "تواريخ الأيام البيض تُحسب بتقويم أم القرى وقد تختلف يوماً عن رؤية الهلال في بلدك.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        SpecialReminder.entries.forEach { r ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(r.labelAr, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        r.descriptionAr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = enabled[r] == true,
                    onCheckedChange = { on ->
                        SpecialReminders.setEnabled(context, r, on)
                        enabled = enabled + (r to on)
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                (context.applicationContext as RafiqApplication)
                                    .container.reminderScheduler.scheduleSpecialAlarm(r)
                            }
                        }
                    }
                )
            }
        }
    }
}
