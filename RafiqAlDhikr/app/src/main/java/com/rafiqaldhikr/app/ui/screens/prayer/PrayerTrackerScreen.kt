package com.rafiqaldhikr.app.ui.screens.prayer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.data.repository.PrayerItem
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private fun PrayerItem.prayer(): Prayer? = when (this) {
    PrayerItem.FAJR -> Prayer.FAJR
    PrayerItem.DHUHR -> Prayer.DHUHR
    PrayerItem.ASR -> Prayer.ASR
    PrayerItem.MAGHRIB -> Prayer.MAGHRIB
    PrayerItem.ISHA -> Prayer.ISHA
    else -> null
}

private fun dayLabel(day: LocalDate, today: LocalDate): String {
    val name = day.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ar"))
    return when (day) {
        today -> "اليوم — $name"
        today.minusDays(1) -> "أمس — $name"
        else -> "$name ${day.dayOfMonth}/${day.monthValue}"
    }
}

@Composable
fun PrayerTrackerScreen(viewModel: PrayerTrackerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val times = remember(state.day) { PrayerSettings.timesFor(context, state.day) }
    val earliest = state.today.minusDays(PrayerTrackerUiState.WINDOW_DAYS - 1L)
    val fardDone = PrayerItem.entries.count { it.isFard && it.key in state.doneOnDay }
    var afterPrayer by remember { mutableStateOf<PrayerItem?>(null) }
    afterPrayer?.let { item ->
        com.rafiqaldhikr.app.ui.components.AfterPrayerDialog(
            prayerKey = item.key,
            prayerLabel = item.labelAr,
            onDismiss = { afterPrayer = null }
        )
    }

    Scaffold(topBar = { RafiqTopBar(title = "متابعة الصلاة", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        enabled = state.day > earliest,
                        onClick = { viewModel.selectDay(state.day.minusDays(1)) }
                    ) { Text("اليوم السابق") }
                    Text(dayLabel(state.day, state.today), style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(
                        enabled = state.day < state.today,
                        onClick = { viewModel.selectDay(state.day.plusDays(1)) }
                    ) { Text("اليوم التالي") }
                }
            }

            item {
                RafiqCard {
                    Text("الفرائض: $fardDone من ٥", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(progress = { fardDone / 5f }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    PrayerItem.entries.filter { it.isFard }.forEach { item ->
                        ItemRow(
                            label = item.labelAr,
                            extra = item.prayer()?.let { p -> times?.get(p) }?.let { "%02d:%02d".format(it.hour, it.minute) },
                            checked = item.key in state.doneOnDay,
                            enabled = state.activeProfile != null,
                            onToggle = {
                                val wasDone = item.key in state.doneOnDay
                                viewModel.toggle(item)
                                if (!wasDone && com.rafiqaldhikr.app.core.prayer.AfterPrayerPrefs.autoShow(context)) {
                                    afterPrayer = item
                                }
                            },
                            trailing = {
                                TextButton(onClick = { afterPrayer = item }) { Text("الأذكار") }
                            }
                        )
                    }
                }
            }

            item {
                RafiqCard {
                    Text("السنن الرواتب والنوافل", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    PrayerItem.entries.filter { !it.isFard && !it.ramadanOnly }.forEach { item ->
                        ItemRow(
                            label = item.labelAr,
                            extra = null,
                            checked = item.key in state.doneOnDay,
                            enabled = state.activeProfile != null,
                            onToggle = { viewModel.toggle(item) }
                        )
                    }
                }
            }

            item {
                RafiqCard {
                    Text("آخر ٧ أيام", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    for (offset in (PrayerTrackerUiState.WINDOW_DAYS - 1) downTo 0) {
                        val d = state.today.minusDays(offset.toLong())
                        val count = state.fardPerDay[d] ?: 0
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                d.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ar")),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(56.dp)
                            )
                            LinearProgressIndicator(
                                progress = { count / 5f },
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$count/5",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRow(
    label: String,
    extra: String?,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = { onToggle() }, enabled = enabled)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (extra != null) {
            Text(extra, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        trailing?.invoke()
    }
}
