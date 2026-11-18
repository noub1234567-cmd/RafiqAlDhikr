package com.rafiqaldhikr.app.ui.screens.dualibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.dua.DuaLibrary
import com.rafiqaldhikr.app.core.dua.DuaQuiz
import com.rafiqaldhikr.app.core.dua.QuizQuestion
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Ten "complete the dhikr" questions drawn from the app's own texts. */
@Composable
fun DuaQuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var round by remember { mutableIntStateOf(0) }
    val questions by produceState<List<QuizQuestion>?>(null, round) {
        value = null
        value = withContext(Dispatchers.Default) { DuaQuiz.generate(DuaLibrary.sections(context)) }
    }
    var index by remember(round) { mutableIntStateOf(0) }
    var score by remember(round) { mutableIntStateOf(0) }
    var picked by remember(round, index) { mutableStateOf<Int?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(title = "اختبار الأذكار", onBackClick = onBack)
        val qs = questions ?: return@Column
        if (qs.isEmpty()) {
            Text("لا تتوفر نصوص كافية للاختبار.", modifier = Modifier.padding(16.dp))
            return@Column
        }
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (index >= qs.size) {
                RafiqCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("النتيجة: $score من ${qs.size}", style = MaterialTheme.typography.titleLarge)
                        Button(onClick = { round++ }) { Text("جولة جديدة") }
                    }
                }
                return@Column
            }
            val q = qs[index]
            Text("السؤال ${index + 1} من ${qs.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            RafiqCard {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("أكمل الذكر:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(q.prefix, style = MaterialTheme.typography.titleMedium)
                }
            }
            q.options.forEachIndexed { i, option ->
                val chosen = picked
                val label = when {
                    chosen == null -> option
                    i == q.correctIndex -> "✓ $option"
                    i == chosen -> "✗ $option"
                    else -> option
                }
                OutlinedButton(
                    enabled = chosen == null,
                    onClick = {
                        picked = i
                        if (i == q.correctIndex) score++
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(label) }
            }
            if (picked != null) {
                RafiqCard {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(q.fullText, style = MaterialTheme.typography.bodyLarge)
                        if (q.source.isNotBlank()) {
                            Text(q.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                Button(onClick = { index++ }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (index == qs.size - 1) "إنهاء" else "التالي")
                }
            }
        }
    }
}
