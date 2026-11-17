package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.model.VerifiedAdhkarCatalog
import java.time.LocalDate

/**
 * One dhikr per day, taken only from the app's verified catalog (never written ad hoc),
 * with its source. Tap to expand or collapse long texts.
 */
@Composable
fun DhikrOfTheDayCard() {
    val today by remember { todayFlow() }.collectAsState(initial = LocalDate.now())
    val entries = VerifiedAdhkarCatalog.defaultEntries
    if (entries.isEmpty()) return
    // Same dhikr all day for everyone; changes at midnight.
    val entry = entries[Math.floorMod(today.toEpochDay(), entries.size.toLong()).toInt()]
    var expanded by remember(entry.key) { mutableStateOf(false) }

    RafiqCard(onClick = { expanded = !expanded }, ornament = true) {
        Text("ذكر اليوم — ${entry.title}", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = entry.arabicText,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = (if (entry.targetCount > 1) "يُقال ${entry.targetCount} مرات — " else "") + entry.sourceReference,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (expanded) "اضغط للطيّ" else "اضغط لقراءة النص كاملاً",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
