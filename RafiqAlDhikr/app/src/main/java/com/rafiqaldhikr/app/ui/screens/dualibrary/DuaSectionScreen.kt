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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import com.rafiqaldhikr.app.core.dua.ReadingPrefs
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
import androidx.compose.ui.unit.isSpecified
import com.rafiqaldhikr.app.core.dua.DuaItem
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaSectionDef
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    var fontScale by remember { mutableStateOf(ReadingPrefs.scale(context)) }
    val listState = rememberLazyListState()

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("بحث في القسم") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = { fontScale = ReadingPrefs.step(context, -1) },
                enabled = fontScale > ReadingPrefs.scales.first()
            ) { Text("أ-") }
            TextButton(
                onClick = { fontScale = ReadingPrefs.step(context, 1) },
                enabled = fontScale < ReadingPrefs.scales.last()
            ) { Text("أ+") }
        }
        val q = query.trim()
        val groups = remember(section, q) {
            val pq = DuaLibrary.plain(q)
            section.groups.map { g ->
                g to g.items.filter {
                    pq.isEmpty() || DuaLibrary.plain(it.title).contains(pq) || DuaLibrary.plain(it.text).contains(pq)
                }
            }.filter { it.second.isNotEmpty() }
        }

        // The list's keys in order, for a given set of opened chapters (used to remember and restore the reading spot).
        fun listKeys(isOpen: (String) -> Boolean): List<String> = buildList {
            groups.forEach { (g, items) ->
                add("h_${g.title}")
                if (isOpen(g.title)) items.indices.forEach { add("i_${g.title}_$it") }
            }
        }
        LaunchedEffect(section.id) {
            val saved = ReadingPrefs.position(context, sectionId) ?: return@LaunchedEffect
            val g = groups.firstOrNull { saved == "h_${it.first.title}" || saved.startsWith("i_${it.first.title}_") }
                ?: return@LaunchedEffect
            expanded[g.first.title] = true
            androidx.compose.runtime.withFrameNanos { }
            val keys = listKeys { expanded[it] ?: (section.groups.size == 1) }
            val at = keys.indexOf(saved)
            if (at >= 0) listState.scrollToItem(at)
        }
        LaunchedEffect(section.id, q) {
            if (q.isNotEmpty()) return@LaunchedEffect
            snapshotFlow { listState.firstVisibleItemIndex }.collect { idx ->
                val keys = listKeys { expanded[it] ?: (section.groups.size == 1) }
                keys.getOrNull(idx)?.let { ReadingPrefs.savePosition(context, sectionId, it) }
            }
        }

        LazyColumn(
            state = listState,
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
                        item(key = "i_${g.title}_$i") { ItemCard(
                            d, DuaLibrary.saveTitle(g, d), section.title, fontScale,
                            BookmarkRef(sectionId, "i_${g.title}_${g.items.indexOf(d)}")
                        )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ItemCard(
    d: DuaItem, saveTitle: String, sectionTitle: String, fontScale: Float = 1f, bookmark: BookmarkRef? = null
) {
    RafiqCard {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(d.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                if (bookmark != null) BookmarkButton(bookmark, sectionTitle, d)
            }
            if (d.note.isNotBlank()) {
                Text(d.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
            val body = MaterialTheme.typography.bodyLarge
            Text(
                d.text,
                style = if (fontScale == 1f) body else body.copy(
                    fontSize = body.fontSize * fontScale,
                    lineHeight = if (body.lineHeight.isSpecified) body.lineHeight * fontScale else body.lineHeight
                )
            )
            if (d.source.isNotBlank()) {
                Text(d.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            DuaActionsRow(text = d.text, source = d.source, saveTitle = saveTitle, saveCategory = "من المكتبة: $sectionTitle")
        }
    }
}

/** Where a text lives in the library: its section and the list key used to scroll back to it. */
internal data class BookmarkRef(val sectionId: String, val locator: String)

@Composable
private fun BookmarkButton(ref: BookmarkRef, sectionTitle: String, d: DuaItem) {
    val container = com.rafiqaldhikr.app.ui.screens.tools.rememberContainer()
    val profileId by com.rafiqaldhikr.app.ui.screens.tools.activeProfileIdState(container)
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val context = LocalContext.current
    val tools = container.personalToolsRepository
    var marked by remember(ref, profileId) { mutableStateOf(false) }
    LaunchedEffect(ref, profileId) {
        marked = profileId?.let { tools.isBookmarked(it, ref.sectionId, ref.locator) } ?: false
    }
    androidx.compose.material3.IconButton(onClick = {
        val pid = profileId ?: return@IconButton
        scope.launch {
            marked = tools.toggleBookmark(pid, ref.sectionId, sectionTitle, ref.locator, d.title, d.text, d.source)
            android.widget.Toast.makeText(context, if (marked) "أُضيفت إشارة مرجعية" else "أُزيلت الإشارة", android.widget.Toast.LENGTH_SHORT).show()
        }
    }) {
        androidx.compose.material3.Icon(
            if (marked) androidx.compose.material.icons.Icons.Filled.Bookmark else androidx.compose.material.icons.Icons.Filled.BookmarkBorder,
            contentDescription = "إشارة مرجعية",
            tint = if (marked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
