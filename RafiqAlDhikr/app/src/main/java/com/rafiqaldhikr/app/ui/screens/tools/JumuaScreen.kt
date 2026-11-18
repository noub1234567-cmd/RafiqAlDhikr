package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.jumua.JumuaChecklist
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate

/** سنن الجمعة: ticking is open on Friday; on other days the latest Friday is shown read-only. */
@Composable
fun JumuaScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val profileId by activeProfileIdState(container)
    val today = LocalDate.now()
    val friday = JumuaChecklist.fridayFor(today)
    val editable = JumuaChecklist.isFriday(today)
    var checked by remember(profileId) { mutableStateOf(profileId?.let { JumuaChecklist.checked(context, it, friday) } ?: emptySet()) }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "سنن الجمعة", onBackClick = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                RafiqCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (editable) "أنجزت ${checked.size} من ${JumuaChecklist.items.size}" else "آخر جمعة: ${friday} — ${checked.size} من ${JumuaChecklist.items.size}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!editable) {
                            Text(
                                "يُفتح التعليم يوم الجمعة.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            items(JumuaChecklist.items, key = { it.key }) { item ->
                RafiqCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = item.key in checked,
                            enabled = editable && profileId != null,
                            onCheckedChange = { profileId?.let { pid -> checked = JumuaChecklist.toggle(context, pid, friday, item.key) } }
                        )
                        Text(item.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
