package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import kotlinx.coroutines.delay

/** Dashboard card: the next prayer, its time and the time left. Tapping opens the prayer tracker. */
@Composable
fun NextPrayerCard(onClick: () -> Unit) {
    val context = LocalContext.current
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            value = System.currentTimeMillis()
            delay(20_000)
        }
    }
    val next = PrayerSettings.nextPrayer(context, now)

    RafiqCard(onClick = onClick) {
        if (next == null) {
            Text("مواقيت الصلاة غير متاحة لهذه المدينة", style = MaterialTheme.typography.bodyMedium)
            return@RafiqCard
        }
        val (prayer, at) = next
        val zone = java.time.ZoneId.of(PrayerSettings.city(context).zoneId)
        val time = java.time.Instant.ofEpochMilli(at).atZone(zone)
        val left = ((at - now) / 60_000L).coerceAtLeast(0L)
        val leftText = when {
            left >= 60 -> "%d س %02d د".format(left / 60, left % 60)
            else -> "$left د"
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الصلاة القادمة: ${prayer.labelAr}", style = MaterialTheme.typography.titleMedium)
            Text("%02d:%02d".format(time.hour, time.minute), style = MaterialTheme.typography.titleMedium)
        }
        Text(
            "بعد $leftText — ${PrayerSettings.city(context).nameAr}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
