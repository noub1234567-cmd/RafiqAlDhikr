package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.MemoTest
import com.rafiqaldhikr.app.core.dua.TestQuestion
import com.rafiqaldhikr.app.core.dua.WrongAnswer
import com.rafiqaldhikr.app.data.local.db.entities.TestResultEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.screens.tools.activeProfileIdState
import com.rafiqaldhikr.app.ui.screens.tools.profileList
import com.rafiqaldhikr.app.ui.screens.tools.rememberContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val SCOPE_PLANS = 0
private const val SCOPE_MANUAL = 1
private const val SCOPE_WEAK = 2
private const val SCOPE_RETRY = 3

private fun parseWrong(json: String): List<WrongAnswer> =
    runCatching { Json.decodeFromString(ListSerializer(WrongAnswer.serializer()), json) }.getOrDefault(emptyList())

/** "اختبر حفظك": pick what to be tested on, answer with instant feedback, then keep a report of the mistakes. */
@Composable
fun TestYourselfScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = rememberContainer()
    val scope = rememberCoroutineScope()
    val profileId by activeProfileIdState(container)
    val sections by produceState(DuaLibrary.skeleton) { value = withContext(Dispatchers.Default) { DuaLibrary.sections(context) } }
    val planItems = profileList(profileId) { container.memoPlanRepository.observeAllItems(it) }
    val results = profileList(profileId) { container.personalToolsRepository.observeTestResults(it) }

    var scopeKind by remember { mutableIntStateOf(SCOPE_PLANS) }
    var count by remember { mutableIntStateOf(10) }
    val manual = remember { mutableStateListOf<String>() }
    var retryRefs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var label by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var questions by remember { mutableStateOf<List<TestQuestion>?>(null) }
    var finished by remember { mutableStateOf(false) }
    val answers = remember { mutableStateListOf<Int>() }

    fun start(kind: Int, refs: Set<String> = emptySet()) {
        scope.launch {
            message = null
            val (qs, why) = withContext(Dispatchers.Default) {
                val universe = MemoTest.flatten(sections)
                val items = when (kind) {
                    SCOPE_PLANS -> {
                        val done = planItems.filter { it.doneEpochDay >= 0 }.map { it.itemKey }.toSet()
                        universe.filter { it.ref in done }
                    }
                    SCOPE_MANUAL -> universe.filter { it.sectionId in manual }
                    SCOPE_WEAK -> {
                        val weak = results.take(20).flatMap { parseWrong(it.wrongJson) }.map { it.ref }.toSet()
                        universe.filter { it.ref in weak }
                    }
                    else -> universe.filter { it.ref in refs }
                }
                val text = when (kind) {
                    SCOPE_PLANS -> "من محفوظاتي في الخطط"
                    SCOPE_MANUAL -> "أقسام مختارة (${manual.size})"
                    SCOPE_WEAK -> "نقاط ضعفي"
                    else -> "إعادة الأخطاء"
                }
                MemoTest.generate(items, universe, sections, if (kind == SCOPE_RETRY) -1 else count) to text
            }
            if (qs.isEmpty()) {
                message = when (kind) {
                    SCOPE_PLANS -> "لا توجد نصوص محفوظة في خططك بعد. أتمم أجزاء من خطة، أو اختر أقساماً بنفسك."
                    SCOPE_MANUAL -> "اختر قسماً واحداً على الأقل فيه نصوص كافية."
                    SCOPE_WEAK -> "لا أخطاء محفوظة بعد. أجرِ اختباراً أولاً."
                    else -> "تعذر بناء الأسئلة."
                }
            } else {
                label = why
                answers.clear()
                finished = false
                questions = qs
            }
        }
    }

    val qs = questions
    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "اختبر حفظك", onBackClick = {
            if (qs != null) questions = null else onBack()
        })
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (qs == null) {
                SetupPart(
                    scopeKind = scopeKind, onScope = { scopeKind = it }, count = count, onCount = { count = it },
                    manual = manual, sections = sections.map { it.id to it.title }, message = message,
                    results = results, onStart = { start(scopeKind) },
                    onDelete = { id -> scope.launch { container.personalToolsRepository.deleteTestResult(id) } }
                )
                return@Column
            }

            val index = answers.size
            if (index >= qs.size) {
                val wrong = qs.indices.filter { answers[it] != qs[it].correctIndex }.map { i ->
                    val q = qs[i]
                    WrongAnswer(q.ref, q.title, q.instruction, q.prompt, q.options[answers[i]], q.options[q.correctIndex])
                }
                LaunchedEffect(finished) {
                    if (!finished) {
                        finished = true
                        profileId?.let { container.personalToolsRepository.saveTestResult(it, label, qs.size, wrong) }
                    }
                }
                RafiqCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("النتيجة: ${qs.size - wrong.size} من ${qs.size}", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "أجبت صحيحاً عن ${qs.size - wrong.size} وأخطأت في ${wrong.size}. حُفظت النتيجة في سجل الاختبارات.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { questions = null }) { Text("اختبار جديد") }
                            if (wrong.isNotEmpty()) {
                                OutlinedButton(onClick = {
                                    val refs = wrong.map { it.ref }.toSet()
                                    start(SCOPE_RETRY, refs)
                                }) { Text("أعد اختبار أخطائي") }
                            }
                        }
                    }
                }
                WrongReport(wrong)
                return@Column
            }

            val q = qs[index]
            Text("السؤال ${index + 1} من ${qs.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            var picked by remember(qs, index) { mutableStateOf<Int?>(null) }
            RafiqCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(q.instruction, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(q.prompt, style = MaterialTheme.typography.titleMedium)
                }
            }
            q.options.forEachIndexed { i, option ->
                val chosen = picked
                val text = when {
                    chosen == null -> option
                    i == q.correctIndex -> "✓ $option"
                    i == chosen -> "✗ $option"
                    else -> option
                }
                OutlinedButton(
                    enabled = chosen == null,
                    onClick = { picked = i },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(text) }
            }
            val chosen = picked
            if (chosen != null) {
                Text(
                    if (chosen == q.correctIndex) "إجابة صحيحة" else "إجابة خاطئة، والصواب ما عُلّم بعلامة ✓",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (chosen == q.correctIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                RafiqCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(q.fullText, style = MaterialTheme.typography.bodyLarge)
                        if (q.source.isNotBlank()) {
                            Text(q.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                Button(onClick = { answers.add(chosen) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (index == qs.size - 1) "إنهاء" else "التالي")
                }
            }
        }
    }
}

@Composable
private fun WrongReport(wrong: List<WrongAnswer>) {
    if (wrong.isEmpty()) {
        Text("لا أخطاء في هذا الاختبار، ما شاء الله.", style = MaterialTheme.typography.titleMedium)
        return
    }
    Text("تقرير الأخطاء (${wrong.size})", style = MaterialTheme.typography.titleMedium)
    wrong.forEach { w ->
        RafiqCard {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(w.title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text("${w.instruction} ${w.prompt}", style = MaterialTheme.typography.bodyMedium)
                Text("إجابتك: ${w.chosen}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                Text("الصواب: ${w.correct}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SetupPart(
    scopeKind: Int, onScope: (Int) -> Unit, count: Int, onCount: (Int) -> Unit,
    manual: MutableList<String>, sections: List<Pair<String, String>>, message: String?,
    results: List<TestResultEntity>, onStart: () -> Unit, onDelete: (Long) -> Unit
) {
    RafiqCard {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ما الذي تُختبر فيه؟", style = MaterialTheme.typography.titleMedium)
            listOf(SCOPE_PLANS to "من محفوظاتي في خطط الحفظ", SCOPE_MANUAL to "أقسام أختارها بنفسي", SCOPE_WEAK to "نقاط ضعفي (ما أخطأت فيه)").forEach { (k, t) ->
                FilterChip(selected = scopeKind == k, onClick = { onScope(k) }, label = { Text(t) })
            }
            if (scopeKind == SCOPE_MANUAL) {
                sections.forEach { (id, title) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = id in manual, onCheckedChange = { if (it) manual.add(id) else manual.remove(id) })
                        Text(title, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Text("عدد الأسئلة", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10 to "10", 20 to "20", 30 to "30", -1 to "الكل").forEach { (n, t) ->
                    FilterChip(selected = count == n, onClick = { onCount(n) }, label = { Text(t) })
                }
            }
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("ابدأ الاختبار") }
            if (message != null) Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
    if (results.isNotEmpty()) {
        Text("سجل اختباراتي", style = MaterialTheme.typography.titleMedium)
        val fmt = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
        results.forEach { r ->
            var open by remember(r.id) { mutableStateOf(false) }
            RafiqCard(onClick = { open = !open }) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("صحيح ${r.correct} من ${r.total} — ${r.scopeLabel}", style = MaterialTheme.typography.titleSmall)
                    Text(
                        Instant.ofEpochMilli(r.createdAtMillis).atZone(ZoneId.systemDefault()).format(fmt),
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (open) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            WrongReport(parseWrong(r.wrongJson))
                            TextButton(onClick = { onDelete(r.id) }) { Text("حذف هذا السجل") }
                        }
                    }
                }
            }
        }
    }
}
