package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.rafiqaldhikr.app.core.dua.DuaSearch
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One search box over every section of the library; tashkeel is ignored. */
@Composable
fun DuaSearchScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val index by produceState<List<DuaSearch.Entry>?>(null) {
        value = withContext(Dispatchers.Default) { DuaSearch.buildIndex(DuaLibrary.sections(context)) }
    }
    var query by remember { mutableStateOf("") }
    val hits = remember(index, query) { index?.let { DuaSearch.search(it, query) }.orEmpty() }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "بحث في الأذكار والأدعية", onBackClick = onBack)
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("ابحث (بدون تشكيل)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )
        if (query.isNotBlank()) {
            Text(
                if (hits.isEmpty()) "لا توجد نتائج" else "${hits.size} نتيجة",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(hits, key = { i, h -> "${h.sectionId}_${h.group.title}_${i}" }) { _, h ->
                Column {
                    Text(
                        "${h.sectionTitle} — ${h.group.title}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    ItemCard(h.item, DuaLibrary.saveTitle(h.group, h.item), h.sectionTitle)
                }
            }
        }
    }
}
