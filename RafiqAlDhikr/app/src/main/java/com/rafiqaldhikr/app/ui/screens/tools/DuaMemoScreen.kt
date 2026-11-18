package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.data.local.db.entities.DuaMemoEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import kotlinx.coroutines.launch

/** Memorize du'as with spaced repetition: recall first, then reveal the text and rate yourself. */
@Composable
fun DuaMemoScreen(onBack: () -> Unit) {
    val container = rememberContainer()
    val repo = container.personalToolsRepository
    val profileId by activeProfileIdState(container)
    val memos = profileList(profileId) { repo.observeMemos(it) }
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toEpochDay()
    val due = memos.filter { it.dueEpochDay <= today }
    val later = memos.filter { it.dueEpochDay > today }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "حفظ الأدعية", onBackClick = onBack)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (memos.isEmpty()) {
                item {
                    RafiqCard {
                        Text(
                            "لم تضف دعاءً للحفظ بعد. اضغط «حفظ» تحت أي دعاء في التطبيق، وسيعرضه عليك هنا في مواعيد المراجعة.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
            if (due.isNotEmpty()) {
                item { Text("للمراجعة اليوم (${due.size})", style = MaterialTheme.typography.titleMedium) }
                items(due, key = { "d_${it.id}" }) { m ->
                    DueCard(m) { remembered -> scope.launch { repo.reviewMemo(m, remembered, today) } }
                }
            } else if (memos.isNotEmpty()) {
                item {
                    RafiqCard {
                        Text("أنهيت مراجعة اليوم.", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp))
                    }
                }
            }
            if (later.isNotEmpty()) {
                item { Text("مراجعات قادمة", style = MaterialTheme.typography.titleMedium) }
                items(later, key = { "l_${it.id}" }) { m ->
                    RafiqCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(m.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "بعد ${m.dueEpochDay - today} يوم",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { scope.launch { repo.deleteMemo(m.id) } }) { Text("إزالة") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DueCard(m: DuaMemoEntity, onRate: (Boolean) -> Unit) {
    var revealed by remember(m.id, m.dueEpochDay) { mutableStateOf(false) }
    RafiqCard {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(m.title, style = MaterialTheme.typography.titleSmall)
            if (!revealed) {
                Text("حاول استحضار الدعاء من ذاكرتك، ثم اكشف النص للتأكد.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onClick = { revealed = true }) { Text("إظهار النص") }
            } else {
                Text(m.text, style = MaterialTheme.typography.bodyLarge)
                if (m.source.isNotBlank()) {
                    Text(m.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { onRate(true) }) { Text("حفظته") }
                    OutlinedButton(onClick = { onRate(false) }) { Text("صعب، أعد عليّ") }
                }
            }
        }
    }
}
