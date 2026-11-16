package com.rafiqaldhikr.app.ui.screens.khatma

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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.model.QuranSurahs
import com.rafiqaldhikr.app.core.quran.KhatmaMath
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.ConfirmHost
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.rememberConfirm
import java.time.LocalDate

@Composable
fun KhatmaScreen(viewModel: KhatmaViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val k = state.khatma
    val confirm = rememberConfirm()
    ConfirmHost(confirm)

    Scaffold(topBar = { RafiqTopBar(title = "ختمة القرآن", onBackClick = onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.finishedCount > 0) {
                item { Text("ختمات مكتملة: ${state.finishedCount}", style = MaterialTheme.typography.titleMedium) }
            }
            if (k == null) {
                item { StartCard(enabled = state.activeProfile != null, onStart = viewModel::start) }
            } else {
                item { ProgressCard(state, viewModel) }
                item { PositionCard(readAyahs = k.readAyahs, onSave = viewModel::setPosition) }
                item {
                    TextButton(onClick = {
                        confirm.ask("سيُحذف تقدّم هذه الختمة. هل تريد المتابعة؟") { viewModel.delete() }
                    }) { Text("حذف الختمة الحالية") }
                }
            }
        }
    }
}

@Composable
private fun StartCard(enabled: Boolean, onStart: (Int) -> Unit) {
    var text by remember { mutableStateOf("30") }
    RafiqCard {
        Text("ابدأ ختمة جديدة", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Text("اختر عدد الأيام لإنهائها، وسيُحسب وردك اليومي تلقائياً.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(30, 60, 90, 180).forEach { d ->
                OutlinedButton(onClick = { text = d.toString() }) { Text("$d") }
            }
        }
        OutlinedTextField(
            value = text,
            onValueChange = { v -> text = v.filter { it.isDigit() }.take(4) },
            label = { Text("عدد الأيام") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            enabled = enabled && (text.toIntOrNull() ?: 0) > 0,
            onClick = { onStart(text.toInt()) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("ابدأ الختمة") }
    }
}

@Composable
private fun ProgressCard(state: KhatmaUiState, vm: KhatmaViewModel) {
    val k = state.khatma ?: return
    val today = state.today.toEpochDay()
    val pos = KhatmaMath.positionOf(k.readAyahs)
    val remaining = KhatmaMath.remaining(k.readAyahs)
    val daysLeft = KhatmaMath.daysLeft(today, k.targetEpochDay)
    val wird = KhatmaMath.dailyWirdAyahs(k.readAyahs, today, k.targetEpochDay)
    val ahead = KhatmaMath.aheadBy(k.readAyahs, k.startEpochDay, k.targetEpochDay, today)
    val overdue = today > k.targetEpochDay
    RafiqCard {
        Text(
            pos?.let { "وصلتَ إلى: ${QuranSurahs.byNumber(it.first).nameAr} — الآية ${it.second}" } ?: "لم تبدأ القراءة بعد",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { k.readAyahs.toFloat() / KhatmaMath.TOTAL_AYAHS },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(6.dp))
        Text("المقروء: ${k.readAyahs} من ${KhatmaMath.TOTAL_AYAHS} آية، والمتبقي $remaining")
        Text("الموعد: ${LocalDate.ofEpochDay(k.targetEpochDay)}" + if (overdue) " (انتهى الموعد)" else " — بقي $daysLeft يوماً")
        Spacer(Modifier.height(6.dp))
        Text(
            "وردك اليوم: $wird آية (≈ ${KhatmaMath.approxPages(wird)} صفحة، تقريباً)",
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            when {
                ahead > 0 -> "أنت متقدّم عن الوتيرة المنتظمة بنحو $ahead آية"
                ahead < 0 -> "أنت متأخر عن الوتيرة المنتظمة بنحو ${-ahead} آية"
                else -> "أنت على الوتيرة المنتظمة"
            },
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.addAyahs(wird) }, enabled = remaining > 0) { Text("قرأتُ ورد اليوم") }
            OutlinedButton(onClick = { vm.addAyahs(10) }, enabled = remaining > 0) { Text("+١٠ آيات") }
        }
        if (k.readAyahs >= KhatmaMath.TOTAL_AYAHS) {
            Spacer(Modifier.height(6.dp))
            Text("تقبّل الله منك، أتممتَ الختمة.", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PositionCard(readAyahs: Int, onSave: (Int) -> Unit) {
    val initial = KhatmaMath.positionOf(readAyahs)
    var surah by remember(readAyahs) { mutableStateOf(initial?.first ?: 1) }
    var ayahText by remember(readAyahs) { mutableStateOf((initial?.second ?: 0).toString()) }
    var open by remember { mutableStateOf(false) }
    val info = QuranSurahs.byNumber(surah)
    RafiqCard {
        Text("تحديد موضع القراءة", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Column {
            OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth()) {
                Text("${info.number}. ${info.nameAr} (${info.ayahCount} آية)")
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
                QuranSurahs.all.forEach { s ->
                    DropdownMenuItem(
                        text = { Text("${s.number}. ${s.nameAr}") },
                        onClick = { surah = s.number; open = false }
                    )
                }
            }
        }
        OutlinedTextField(
            value = ayahText,
            onValueChange = { v -> ayahText = v.filter { it.isDigit() }.take(3) },
            label = { Text("آخر آية قرأتها") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        Button(
            onClick = { onSave(KhatmaMath.readAyahsFor(surah, ayahText.toIntOrNull() ?: 0)) },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) { Text("حفظ الموضع") }
    }
}
