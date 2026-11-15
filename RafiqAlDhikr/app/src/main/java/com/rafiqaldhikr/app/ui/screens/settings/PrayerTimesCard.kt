package com.rafiqaldhikr.app.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.prayer.CalcMethod
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerCities
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.ui.components.RafiqCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PrayerTimesCard() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var cityIndex by remember { mutableStateOf(PrayerSettings.cityIndex(context)) }
    var method by remember { mutableStateOf(PrayerSettings.method(context)) }
    var showCities by remember { mutableStateOf(false) }

    // Linked adhkar reminders follow the prayer time, so they must be re-armed after a change.
    fun rearm() {
        scope.launch {
            withContext(Dispatchers.IO) {
                (context.applicationContext as RafiqApplication).container.reminderScheduler.rescheduleAll()
            }
        }
    }

    val times = remember(cityIndex, method) { PrayerSettings.timesFor(context, LocalDate.now()) }

    RafiqCard {
        Text("مواقيت الصلاة", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "تُحسب المواقيت داخل التطبيق دون إنترنت ولا إذن موقع، فاختر مدينتك. " +
                "يمكن ربط أذكار الصباح والمساء والنوم بها من شاشة الأذكار. المواقيت تقريبية وقد تختلف دقيقة أو دقيقتين عن التقويم الرسمي.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = { showCities = true }, modifier = Modifier.fillMaxWidth()) {
            Text("المدينة: ${PrayerCities.all[cityIndex].nameAr}")
        }
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CalcMethod.entries.forEach { m ->
                FilterChip(
                    selected = method == m,
                    onClick = {
                        method = m
                        PrayerSettings.setMethod(context, m)
                        rearm()
                    },
                    label = { Text(m.labelAr) }
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (times == null) {
            Text("تعذّر حساب المواقيت لهذه المدينة.", style = MaterialTheme.typography.bodySmall)
        } else {
            Prayer.entries.forEach { p ->
                val t = times.getValue(p)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(p.labelAr, style = MaterialTheme.typography.bodyMedium)
                    Text("%02d:%02d".format(t.hour, t.minute), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    if (showCities) {
        AlertDialog(
            onDismissRequest = { showCities = false },
            title = { Text("اختر المدينة") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    itemsIndexed(PrayerCities.all) { index, city ->
                        Text(
                            text = city.nameAr,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (index == cityIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cityIndex = index
                                    PrayerSettings.setCityIndex(context, index)
                                    showCities = false
                                    rearm()
                                }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCities = false }) { Text("إغلاق") } }
        )
    }
}
