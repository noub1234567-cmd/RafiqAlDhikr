package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.calendar.todayFlow
import com.rafiqaldhikr.app.core.dua.DuaDaily
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaSectionDef
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** "حديث اليوم" (from فضل الدعاء) and "دعاء اليوم" (from the Prophet's and Quranic du'as). */
@Composable
fun DuaOfTheDayCards() {
    val context = LocalContext.current
    val today by remember { todayFlow() }.collectAsState(initial = LocalDate.now())
    val sections by produceState<List<DuaSectionDef>?>(null) {
        value = withContext(Dispatchers.Default) { DuaLibrary.sections(context) }
    }
    val all = sections ?: return
    DayCard("حديث اليوم", DuaDaily.pick(all, DuaDaily.HADITH_IDS, today.toEpochDay()), "من فضل الدعاء")
    Spacer(Modifier.height(14.dp))
    DayCard("دعاء اليوم", DuaDaily.pick(all, DuaDaily.DUA_IDS, today.toEpochDay()), "من أدعية اليوم")
}

@Composable
private fun DayCard(label: String, pick: DuaDaily.Pick?, category: String) {
    if (pick == null) return
    var expanded by remember(pick.item.text) { mutableStateOf(false) }
    RafiqCard(onClick = { expanded = !expanded }) {
        Text(
            if (pick.item.title.isBlank()) label else "$label — ${pick.item.title}",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(6.dp))
        Text(
            pick.item.text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis
        )
        if (pick.item.source.isNotBlank()) {
            Text(pick.item.source, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            if (expanded) "اضغط للطيّ" else "اضغط لقراءة النص كاملاً",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        DuaActionsRow(
            text = pick.item.text,
            source = pick.item.source,
            saveTitle = DuaLibrary.saveTitle(pick.group, pick.item),
            saveCategory = category
        )
    }
}
