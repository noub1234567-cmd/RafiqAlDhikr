package com.rafiqaldhikr.app.ui.screens.ramadan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.core.quran.KhatmaMath
import com.rafiqaldhikr.app.core.ramadan.FastMarker
import com.rafiqaldhikr.app.core.ramadan.RamadanCalendar
import com.rafiqaldhikr.app.core.ramadan.RamadanStatus
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

private fun hm(t: ZonedDateTime?): String = t?.let { "%02d:%02d".format(it.hour, it.minute) } ?: "--:--"

fun formatLeft(d: Duration): String {
    val mins = d.toMinutes().coerceAtLeast(0)
    return if (mins >= 60) "%d س %02d د".format(mins / 60, mins % 60) else "$mins د"
}

@Composable
fun RamadanScreen(viewModel: RamadanViewModel, onBack: () -> Unit, onOpenKhatma: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val status = state.status
    val zone = remember { ZoneId.of(PrayerSettings.city(context).zoneId) }

    Scaffold(topBar = { RafiqTopBar(title = "رمضان", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (status == null) return@LazyColumn

            item { StatusCard(status, state.today, zone) }

            if (status is RamadanStatus.During) {
                item {
                    val todayDone = state.today.toEpochDay() in state.taraweehDays
                    RafiqCard {
                        Text("التراويح", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("صلّيتُ التراويح الليلة")
                            Checkbox(checked = todayDone, onCheckedChange = { viewModel.toggleTaraweeh(state.today) })
                        }
                        val inRamadan = state.taraweehDays.count {
                            it >= status.start.toEpochDay() && it < status.start.toEpochDay() + status.length
                        }
                        Text("ليالٍ صُلّيت: $inRamadan من ${status.length}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                RafiqCard {
                    Text("ختمة رمضان", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    val k = state.khatma
                    if (k == null) {
                        Text("ختمة كاملة تنتهي في آخر يوم من رمضان، بورد يومي يُحسب تلقائياً.")
                        Button(
                            onClick = viewModel::startRamadanKhatma,
                            enabled = state.activeProfile != null,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) { Text("ابدأ ختمة رمضان") }
                    } else {
                        val wird = KhatmaMath.dailyWirdAyahs(k.readAyahs, state.today.toEpochDay(), k.targetEpochDay)
                        Text("وردك اليوم: $wird آية (≈ ${KhatmaMath.approxPages(wird)} صفحة، تقريباً)")
                        OutlinedButton(onClick = onOpenKhatma, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("فتح الختمة")
                        }
                    }
                }
            }

            item {
                Text("الإمساكية", style = MaterialTheme.typography.titleLarge)
                Text(
                    "الإمساك قبل الفجر بـ ${RamadanCalendar.IMSAK_MINUTES} دقائق احتياطاً. المواقيت محسوبة لمدينتك وطريقتك، والتقويم هجري محسوب وقد يختلف يوماً عن الرؤية (يمكن تعديله من شاشة الصيام).",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            val days = (0 until status.length).map { i ->
                status.start.plusDays(i.toLong())
            }
            items(days.size) { i ->
                val date = days[i]
                val times = remember(date) { PrayerSettings.timesFor(context, date) }
                val isToday = date == state.today
                RafiqCard(
                    containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${i + 1} رمضان — ${date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("ar"))} ${date.dayOfMonth}/${date.monthValue}",
                            style = MaterialTheme.typography.titleSmall)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إمساك ${hm(times?.let { RamadanCalendar.imsakOf(it) })}")
                        Text("فجر ${hm(times?.get(Prayer.FAJR))}")
                        Text("إفطار ${hm(times?.get(Prayer.MAGHRIB))}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(status: RamadanStatus, today: LocalDate, zone: ZoneId) {
    val context = LocalContext.current
    RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        when (status) {
            is RamadanStatus.Before -> {
                Text("رمضان بعد ${status.daysUntil} يوماً", style = MaterialTheme.typography.titleLarge)
                Text("أول أيامه المتوقع: ${status.start}", style = MaterialTheme.typography.bodyMedium)
            }
            is RamadanStatus.During -> {
                val now by produceState(ZonedDateTime.now(zone)) {
                    while (true) {
                        value = ZonedDateTime.now(zone)
                        delay(15_000)
                    }
                }
                Text("اليوم ${status.dayNumber} من رمضان", style = MaterialTheme.typography.titleLarge)
                val marker = RamadanCalendar.nextMarker(
                    now,
                    PrayerSettings.timesFor(context, today).orEmpty(),
                    PrayerSettings.timesFor(context, today.plusDays(1)).orEmpty()
                )
                if (marker != null) {
                    val label = if (marker.kind == FastMarker.IFTAR) "الإفطار" else "الإمساك"
                    Text(
                        "$label بعد ${formatLeft(Duration.between(now, marker.at))} (${hm(marker.at)})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
