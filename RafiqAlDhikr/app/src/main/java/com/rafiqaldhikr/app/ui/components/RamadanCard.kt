package com.rafiqaldhikr.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.core.ramadan.FastMarker
import com.rafiqaldhikr.app.core.ramadan.RamadanCalendar
import com.rafiqaldhikr.app.core.ramadan.RamadanStatus
import com.rafiqaldhikr.app.ui.screens.ramadan.formatLeft
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

/** True when the dashboard should show the Ramadan card: in Ramadan, or within 30 days before it. */
fun showRamadanCard(status: RamadanStatus): Boolean = when (status) {
    is RamadanStatus.During -> true
    is RamadanStatus.Before -> status.daysUntil <= 30
}

@Composable
fun RamadanCard(status: RamadanStatus, onClick: () -> Unit) {
    val context = LocalContext.current
    val zone = ZoneId.of(PrayerSettings.city(context).zoneId)
    val now by produceState(ZonedDateTime.now(zone)) {
        while (true) {
            value = ZonedDateTime.now(zone)
            delay(20_000)
        }
    }
    RafiqCard(containerColor = MaterialTheme.colorScheme.tertiaryContainer, onClick = onClick) {
        when (status) {
            is RamadanStatus.Before ->
                Text("يبقى ${status.daysUntil} يوماً على رمضان", style = MaterialTheme.typography.titleMedium)
            is RamadanStatus.During -> {
                val today = now.toLocalDate()
                Text("رمضان — اليوم ${status.dayNumber}", style = MaterialTheme.typography.titleMedium)
                val marker = RamadanCalendar.nextMarker(
                    now,
                    PrayerSettings.timesFor(context, today).orEmpty(),
                    PrayerSettings.timesFor(context, today.plusDays(1)).orEmpty()
                )
                if (marker != null) {
                    val label = if (marker.kind == FastMarker.IFTAR) "الإفطار" else "الإمساك"
                    Text("$label بعد ${formatLeft(Duration.between(now, marker.at))}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
