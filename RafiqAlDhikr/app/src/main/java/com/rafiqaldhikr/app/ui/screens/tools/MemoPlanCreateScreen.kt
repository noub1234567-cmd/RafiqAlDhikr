package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaSectionDef
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import com.rafiqaldhikr.app.data.repository.PlanSeed
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val WHOLE_SECTION = ""

/** Pick a section (or one chapter of it), the number of days or the daily amount, and how to review. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoPlanCreateScreen(onBack: () -> Unit, onCreated: (Long) -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val profileId by activeProfileIdState(container)
    val scope = rememberCoroutineScope()
    val sections by produceState<List<DuaSectionDef>>(emptyList()) {
        value = withContext(Dispatchers.Default) { DuaLibrary.sections(context) }
    }

    var sectionId by remember { mutableStateOf<String?>(null) }
    var groupTitle by remember { mutableStateOf(WHOLE_SECTION) }
    var byDays by remember { mutableStateOf(true) }
    var amount by remember { mutableStateOf("20") }
    var auto by remember { mutableStateOf(true) }
    var reviewCount by remember { mutableStateOf("5") }
    var title by remember { mutableStateOf("") }

    val section = sections.firstOrNull { it.id == sectionId }
    val seeds: List<PlanSeed> = remember(section, groupTitle) {
        section?.groups.orEmpty()
            .filter { groupTitle == WHOLE_SECTION || it.title == groupTitle }
            .flatMap { g -> g.items.filter { it.text.isNotBlank() }.map { PlanSeed(DuaLibrary.saveTitle(g, it), it.text, it.source) } }
    }
    val n = amount.toIntOrNull()?.takeIf { it > 0 }
    val days = if (n == null) null else if (byDays) n else MemoPlanMath.daysFor(seeds.size, n)
    val perDay = if (n == null || days == null) null else if (byDays) MemoPlanMath.perDayFor(seeds.size, n) else n

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "خطة حفظ جديدة", onBackClick = onBack)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("ماذا تريد أن تحفظ؟", style = MaterialTheme.typography.titleMedium)
            sections.filter { s -> s.groups.any { g -> g.items.any { it.text.isNotBlank() } } }.forEach { s ->
                RafiqCard(onClick = { sectionId = s.id; groupTitle = WHOLE_SECTION; if (title.isBlank()) title = s.title }) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = sectionId == s.id, onClick = { sectionId = s.id; groupTitle = WHOLE_SECTION; if (title.isBlank()) title = s.title })
                        Text(s.title, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (section != null && section.groups.size > 1) {
                Text("أي جزء؟", style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = groupTitle == WHOLE_SECTION, onClick = { groupTitle = WHOLE_SECTION }, label = { Text("القسم كله") })
                    section.groups.forEach { g ->
                        FilterChip(selected = groupTitle == g.title, onClick = { groupTitle = g.title }, label = { Text(g.title) })
                    }
                }
            }
            if (section != null) {
                Text("${seeds.size} نصاً في اختيارك", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)

                Text("المدة", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = byDays, onClick = { byDays = true }, label = { Text("أحدد عدد الأيام") })
                    FilterChip(selected = !byDays, onClick = { byDays = false }, label = { Text("أحدد المقدار اليومي") })
                }
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it.filter(Char::isDigit).take(4) },
                    label = { Text(if (byDays) "عدد الأيام" else "عدد النصوص كل يوم") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (days != null && perDay != null && seeds.isNotEmpty()) {
                    Text("النتيجة: حتى $perDay يومياً، وتنتهي في $days يوماً.", style = MaterialTheme.typography.bodyMedium)
                }

                Text("المراجعة", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = auto, onClick = { auto = true }, label = { Text("تلقائية") })
                    FilterChip(selected = !auto, onClick = { auto = false }, label = { Text("يدوية") })
                }
                Text(
                    if (auto) "كل ما تحفظه يدخل في «حفظ الأذكار والأحاديث» لتراجعه في مواعيد متباعدة."
                    else "تراجع كل يوم آخر عدد تحدده مما حفظته.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!auto) {
                    OutlinedTextField(
                        value = reviewCount, onValueChange = { reviewCount = it.filter(Char::isDigit).take(3) },
                        label = { Text("راجع آخر كم نصاً كل يوم؟") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("اسم الخطة") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(
                    enabled = profileId != null && days != null && seeds.isNotEmpty() && title.isNotBlank() && (auto || (reviewCount.toIntOrNull() ?: 0) > 0),
                    onClick = {
                        val pid = profileId ?: return@Button
                        val d = days ?: return@Button
                        scope.launch {
                            val id = container.memoPlanRepository.createPlan(
                                pid, title, section.id, groupTitle, seeds, LocalDate.now().toEpochDay(), d,
                                if (auto) MemoPlanMath.REVIEW_AUTO else MemoPlanMath.REVIEW_MANUAL,
                                reviewCount.toIntOrNull() ?: 5
                            )
                            onCreated(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("ابدأ الخطة") }
            }
        }
    }
}
