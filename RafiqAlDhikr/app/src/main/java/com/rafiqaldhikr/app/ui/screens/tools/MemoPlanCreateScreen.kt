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
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
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
    val today = remember { LocalDate.now().toEpochDay() }
    var mode by remember { mutableStateOf(0) } // 0 = study days, 1 = daily amount, 2 = end date
    var amount by remember { mutableStateOf("20") }
    var startDay by remember { mutableStateOf(today) }
    var endDay by remember { mutableStateOf(today + 29) }
    var mask by remember { mutableStateOf(MemoPlanMath.ALL_DAYS) }
    var ramp by remember { mutableStateOf(false) }
    var showAll by remember { mutableStateOf(false) }
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
    val count = seeds.size
    val sessions: Int? = when (mode) {
        0 -> n
        1 -> n?.let { MemoPlanMath.daysFor(count, it) }
        else -> MemoPlanMath.studyDaysBetween(startDay, endDay, mask).takeIf { it > 0 }
    }
    val plan = if (sessions != null && count > 0) MemoPlanMath.quotas(count, sessions, ramp) else null
    val dates = if (sessions != null && count > 0) MemoPlanMath.studyDates(startDay, sessions, mask) else emptyList()
    val totalWords = seeds.sumOf { MemoPlanMath.words(it.text) }
    val avgWordsPerDay = if (sessions != null && sessions > 0) totalWords.toDouble() / sessions else 0.0
    val newMin = MemoPlanMath.newMinutes(Math.ceil(avgWordsPerDay).toInt())
    val reviewMin = if (sessions == null || dates.isEmpty()) 0 else if (auto) {
        MemoPlanMath.reviewMinutes(Math.ceil(avgWordsPerDay * MemoPlanMath.peakReviewRounds((dates.last() - startDay + 1).toInt())).toInt())
    } else {
        val avgItem = if (count > 0) totalWords.toDouble() / count else 0.0
        MemoPlanMath.reviewMinutes(Math.ceil(avgItem * (reviewCount.toIntOrNull() ?: 0)).toInt())
    }

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

                Text("متى تبدأ؟", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = startDay == today, onClick = { startDay = today }, label = { Text("اليوم") })
                    FilterChip(selected = startDay == today + 1, onClick = { startDay = today + 1 }, label = { Text("غداً") })
                    DateChoice("تاريخ البداية", startDay, minEpochDay = today) { startDay = it; if (endDay < it) endDay = it + 29 }
                }

                Text("أيام الحفظ في الأسبوع", style = MaterialTheme.typography.titleMedium)
                StudyDayChips(mask) { mask = it }

                Text("المدة", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("عدد أيام الحفظ") })
                    FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("المقدار اليومي") })
                    FilterChip(selected = mode == 2, onClick = { mode = 2 }, label = { Text("تاريخ الانتهاء") })
                }
                if (mode == 2) {
                    DateChoice("تاريخ الانتهاء", endDay, minEpochDay = startDay) { endDay = it }
                } else {
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it.filter(Char::isDigit).take(4) },
                        label = { Text(if (mode == 0) "عدد أيام الحفظ" else "عدد النصوص كل يوم") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Switch(checked = ramp, onCheckedChange = { ramp = it })
                    Column {
                        Text("ابدأ بتدرج", style = MaterialTheme.typography.bodyLarge)
                        Text("ورد خفيف في الأسبوع الأول ثم يرتفع إلى الورد الكامل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                if (plan != null && sessions != null && dates.isNotEmpty()) {
                    RafiqCard {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("خلاصة الخطة", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                            Text("$sessions يوم حفظ، من ${dayLabel(dates.first())} إلى ${dayLabel(dates.last())}", style = MaterialTheme.typography.bodyMedium)
                            val nonZero = plan.filter { it > 0 }
                            Text(
                                if (nonZero.isEmpty()) "" else if (nonZero.min() == nonZero.max()) "الورد اليومي: ${nonZero.max()}" else "الورد اليومي: من ${nonZero.min()} إلى ${nonZero.max()}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "الوقت التقريبي يومياً: حفظ ≈ $newMin دقيقة" + if (reviewMin > 0) " + مراجعة ≈ $reviewMin دقيقة" + (if (auto) " (في ذروتها)" else "") else "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text("الأرقام تقديرية بحسب عدد كلمات النصوص.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    val warnings = ArrayList<String>()
                    if (sessions > count) warnings.add("عدد أيام الحفظ أكبر من عدد النصوص ($count)، فستمر أيام بلا حفظ. الأنسب ألا يزيد على $count.")
                    if (newMin + reviewMin > MemoPlanMath.HEAVY_MINUTES) {
                        val better = MemoPlanMath.suggestedSessions(totalWords, count)
                        warnings.add("الورد كبير (≈ ${newMin + reviewMin} دقيقة يومياً). يُنصح بنحو $better يوم حفظ أو أكثر.")
                    }
                    warnings.forEach { w ->
                        RafiqCard(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)) {
                            Text(w, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                        }
                    }

                    Text("معاينة الجدول", style = MaterialTheme.typography.titleMedium)
                    var cursor = 0
                    val rows = dates.indices.map { i ->
                        val q = plan[i]
                        val first = cursor
                        cursor += q
                        Triple(i, q, first)
                    }
                    val visible = if (showAll || rows.size <= 12) rows else rows.take(10) + rows.takeLast(2)
                    RafiqCard {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            visible.forEachIndexed { k, (i, q, first) ->
                                if (!showAll && rows.size > 12 && k == 10) Text("…", style = MaterialTheme.typography.bodySmall)
                                val label = if (q == 0) "—" else if (q == 1) seeds[first].title.ifBlank { "نص ${first + 1}" }
                                else "${seeds[first].title.ifBlank { "نص ${first + 1}" }} … ${seeds[first + q - 1].title.ifBlank { "نص ${first + q}" }}"
                                Text("${i + 1}. ${dayLabel(dates[i])} — $q: $label", style = MaterialTheme.typography.bodySmall)
                            }
                            if (rows.size > 12) {
                                TextButton(onClick = { showAll = !showAll }) { Text(if (showAll) "إخفاء التفاصيل" else "عرض كل الأيام") }
                            }
                        }
                    }
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
                    enabled = profileId != null && sessions != null && seeds.isNotEmpty() && title.isNotBlank() && (auto || (reviewCount.toIntOrNull() ?: 0) > 0),
                    onClick = {
                        val pid = profileId ?: return@Button
                        val d = sessions ?: return@Button
                        scope.launch {
                            val id = container.memoPlanRepository.createPlan(
                                pid, title, section.id, groupTitle, seeds, startDay, d,
                                if (auto) MemoPlanMath.REVIEW_AUTO else MemoPlanMath.REVIEW_MANUAL,
                                reviewCount.toIntOrNull() ?: 5, mask, ramp
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
