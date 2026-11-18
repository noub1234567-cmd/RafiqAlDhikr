package com.rafiqaldhikr.app.ui.screens.dualibrary

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaItem
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaSectionDef
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.rafiqaldhikr.app.ui.components.DuaActionsRow
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

/** Reads one library section: search, collapsible chapters, and copy for each item. */
@Composable
fun DuaSectionScreen(sectionId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val loaded by produceState<List<DuaSectionDef>?>(null) {
        value = withContext(Dispatchers.Default) { DuaLibrary.sections(context) }
    }
    val section = loaded?.let { DuaLibrary.byId(it, sectionId) }
    var query by remember { mutableStateOf("") }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    val clipboard = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = section?.title ?: "الأدعية", onBackClick = onBack)

        if (loaded == null) return@Column
        if (section == null || section.itemCount == 0) {
            RafiqCard(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(section?.subtitle.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Text("لم يُضف محتوى هذا القسم بعد.", style = MaterialTheme.typography.titleSmall)
                }
            }
            return@Column
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("بحث في القسم") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        val q = query.trim()
        val groups = remember(section, q) {
            val pq = DuaLibrary.plain(q)
            section.groups.map { g ->
                g to g.items.filter {
                    pq.isEmpty() || DuaLibrary.plain(it.title).contains(pq) || DuaLibrary.plain(it.text).contains(pq)
                }
            }.filter { it.second.isNotEmpty() }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            groups.forEach { (g, items) ->
                val open = q.isNotEmpty() || (expanded[g.title] ?: (section.groups.size == 1))
                item(key = "h_${g.title}") {
                    RafiqCard(onClick = { expanded[g.title] = !(expanded[g.title] ?: (section.groups.size == 1)) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(g.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(if (open) "▲" else "▼ ${items.size}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                if (open) {
                    items.forEachIndexed { i, d ->
                        item(key = "i_${g.title}_$i") { ItemCard(d, DuaLibrary.saveTitle(g, d), section.title) }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ItemCard(d: DuaItem, saveTitle: String, sectionTitle: String) {
    RafiqCard {
        Column(modifier = Modifier.padding(14.dp)) {
            if (d.title.isNotBlank()) Text(d.title, style = MaterialTheme.typography.titleSmall)
            if (d.note.isNotBlank()) {
                Text(d.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            Text(d.text, style = MaterialTheme.typography.bodyLarge)
            if (d.source.isNotBlank()) {
                Text(d.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            DuaActionsRow(text = d.text, source = d.source, saveTitle = saveTitle, saveCategory = "من المكتبة: $sectionTitle")
        }
    }
}
