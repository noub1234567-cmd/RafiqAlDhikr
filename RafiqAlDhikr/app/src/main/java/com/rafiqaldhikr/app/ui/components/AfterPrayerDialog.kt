package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.model.AfterPrayerAdhkar
import com.rafiqaldhikr.app.core.prayer.AfterPrayerPrefs

/**
 * The adhkar to say right after a fard prayer, with a tap counter for each. Counts are not saved: it is a
 * checklist for the moment, not a record.
 */
@Composable
fun AfterPrayerDialog(prayerKey: String, prayerLabel: String, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val items = remember(prayerKey) { AfterPrayerAdhkar.forPrayer(prayerKey) }
    val counts = remember(prayerKey) { mutableStateMapOf<String, Int>() }
    var auto by remember { mutableStateOf(AfterPrayerPrefs.autoShow(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("أذكار بعد صلاة $prayerLabel") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach { d ->
                    val target = d.countFor(prayerKey)
                    val done = counts[d.key] ?: 0
                    val finished = done >= target
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = !finished) {
                            counts[d.key] = done + 1
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(d.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text(
                                    if (finished) "✓" else "$done / $target",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (finished) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (d.text != d.title) {
                                Spacer(Modifier.height(4.dp))
                                Text(d.text, style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(d.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            DuaActionsRow(text = d.text, source = d.source, saveTitle = d.title, saveCategory = "من الأذكار")
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إظهارها تلقائياً بعد تسجيل الفريضة", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Switch(checked = auto, onCheckedChange = {
                        auto = it
                        AfterPrayerPrefs.setAutoShow(context, it)
                    })
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("تم") } }
    )
}
