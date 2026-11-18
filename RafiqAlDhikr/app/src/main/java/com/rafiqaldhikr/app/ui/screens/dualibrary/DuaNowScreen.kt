package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaNow
import com.rafiqaldhikr.app.core.dua.DuaSectionDef
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The suggested keys for right now, using the city's prayer times when they can be computed. */
fun suggestedNowKeys(context: android.content.Context): List<String> {
    val zone = ZoneId.of(PrayerSettings.city(context).zoneId)
    val now = ZonedDateTime.now(zone)
    return DuaNow.suggest(now, PrayerSettings.timesFor(context, now.toLocalDate()))
}

/** "ماذا أقول الآن؟": the adhkar that fit this moment first, every other situation one tap away. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DuaNowScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val data by produceState<Pair<DuaSectionDef?, List<String>>?>(null) {
        value = withContext(Dispatchers.Default) {
            DuaLibrary.byId(DuaLibrary.sections(context), DuaLibrary.LIBRARY) to suggestedNowKeys(context)
        }
    }
    val library = data?.first
    val suggested = data?.second.orEmpty()
    var chosen by remember { mutableStateOf<String?>(null) }
    val selected = DuaNow.byKey(chosen ?: suggested.firstOrNull())

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "ماذا أقول الآن؟", onBackClick = onBack)
        if (data == null) return@Column
        val groups = if (selected == null) emptyList() else DuaNow.groupsFor(library, selected)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "chips") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (suggested.isNotEmpty()) {
                        Text("المناسب لوقتك الآن", style = MaterialTheme.typography.titleSmall)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            suggested.mapNotNull { DuaNow.byKey(it) }.forEach { c ->
                                FilterChip(selected = selected?.key == c.key, onClick = { chosen = c.key }, label = { Text(c.label) })
                            }
                        }
                    }
                    Text("مواقف أخرى", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DuaNow.contexts.filter { it.key !in suggested }.forEach { c ->
                            FilterChip(selected = selected?.key == c.key, onClick = { chosen = c.key }, label = { Text(c.label) })
                        }
                    }
                }
            }
            groups.forEach { g ->
                item(key = "h_${g.title}") {
                    Text(g.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
                items(g.items.size, key = { "i_${g.title}_$it" }) { i ->
                    val d = g.items[i]
                    ItemCard(d, DuaLibrary.saveTitle(g, d), library?.title.orEmpty())
                }
            }
        }
    }
}
