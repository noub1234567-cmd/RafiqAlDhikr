package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

private val sectionGroups: List<Pair<String, List<String>>> = listOf(
    "الأدعية" to listOf(
        DuaLibrary.LIBRARY, DuaLibrary.PRAISE, DuaLibrary.ISTIGHFAR, DuaLibrary.PROPHET,
        DuaLibrary.QURAN, DuaLibrary.VIRTUE, DuaLibrary.CONDITIONS
    ),
    "السنة والأحاديث" to listOf(DuaLibrary.NAWAWI, DuaLibrary.SUNAN)
)

/** The "الأدعية" tab: one button per section, plus the personal du'a notebook that already exists. */
@Composable
fun DuaHubScreen(
    onOpenSection: (String) -> Unit,
    onOpenPersonalNotebook: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNow: () -> Unit,
    onOpenQuiz: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenMemo: () -> Unit,
    onOpenPlans: () -> Unit
) {
    val context = LocalContext.current
    val sections by produceState(DuaLibrary.skeleton) {
        value = withContext(Dispatchers.Default) { DuaLibrary.sections(context) }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "الأدعية")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard(onClick = onOpenNow) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("ماذا أقول الآن؟", style = MaterialTheme.typography.titleMedium)
                        Text("أذكار تناسب وقتك: بعد الأذان، بعد الصلاة، الصباح، النوم…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                RafiqCard(onClick = onOpenSearch) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("بحث في كل الأذكار والأدعية", style = MaterialTheme.typography.titleMedium)
                        Text("اكتب كلمة من النص أو العنوان أو المصدر", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                RafiqCard(onClick = onOpenQuiz) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("اختبر حفظك للأذكار", style = MaterialTheme.typography.titleMedium)
                        Text("أكمل الذكر من أربعة خيارات، عشرة أسئلة في كل جولة", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            item {
                RafiqCard {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("أدعيتي", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = onOpenPersonalNotebook) { Text("دفتر الأدعية") }
                            OutlinedButton(onClick = onOpenFavorites) { Text("مفضلتي") }
                        }
                        OutlinedButton(onClick = onOpenMemo) { Text("حفظ الأذكار والأحاديث") }
                        OutlinedButton(onClick = onOpenPlans) { Text("خطط الحفظ") }
                    }
                }
            }
            sectionGroups.forEach { (heading, ids) ->
                val inGroup = sections.filter { it.id in ids }
                if (inGroup.isNotEmpty()) {
                    item(key = "h_$heading") {
                        Text(heading, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 6.dp))
                    }
                    items(inGroup, key = { it.id }) { s ->
                        RafiqCard(onClick = { onOpenSection(s.id) }) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(s.title, style = MaterialTheme.typography.titleMedium)
                                    Text(s.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                if (s.itemCount > 0) {
                                    Text("${s.itemCount}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
