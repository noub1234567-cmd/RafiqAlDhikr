package com.rafiqaldhikr.app.ui.screens.quran

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.model.QuranGoalType
import com.rafiqaldhikr.app.data.local.db.entities.QuranGoalEntity
import com.rafiqaldhikr.app.ui.components.EmptyStateCard
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.SectionHeader
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel,
    onOpenProfileSwitcher: () -> Unit,
    onOpenMemorization: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val goal = state.goal
    var showGoalDialog by remember { mutableStateOf(false) }
    var customAmount by remember { mutableStateOf("2") }
    var note by remember { mutableStateOf("") }

    val unitLabel = if (goal?.goalType == QuranGoalType.CUSTOM) {
        goal.customUnitLabel
    } else {
        goal?.goalType?.unitAr ?: "صفحة"
    }
    val target = (goal?.targetAmount ?: 4.0).coerceAtLeast(0.5)
    val progress = (state.todayTotal / target).toFloat().coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(
            title = "ورد القرآن الكريم",
            activeProfile = state.activeProfile,
            onProfileClick = onOpenProfileSwitcher,
            actions = {
                IconButton(onClick = { showGoalDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "تعديل الهدف")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(goal?.goalType?.labelAr ?: "صفحات يومية", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "الهدف اليومي: ${ArabicFormatters.toArabicDigits(target.toString())} $unitLabel",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(onClick = { showGoalDialog = true }) {
                            Text("تعديل الهدف")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "أُنجز اليوم: ${ArabicFormatters.toArabicDigits(state.todayTotal.toString())} من ${ArabicFormatters.toArabicDigits(target.toString())} $unitLabel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item {
                RafiqCard {
                    Text("تسجيل قراءة جديدة", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1.0, 2.0, 4.0, 10.0, 20.0).forEach { amt ->
                            OutlinedButton(onClick = { viewModel.logReading(amt, note) }) {
                                Text("+ ${ArabicFormatters.toArabicDigits(amt.toInt())} $unitLabel")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { customAmount = it },
                            label = { Text("كمية مخصصة ($unitLabel)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("السورة / ملاحظة") },
                            singleLine = true,
                            modifier = Modifier.weight(1.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val amt = customAmount.toDoubleOrNull() ?: 1.0
                            if (amt > 0) {
                                viewModel.logReading(amt, note)
                                note = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ القراءة")
                    }
                }
            }

            item {
                RafiqCard {
                    Text("ملخص التلاوة", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("خلال ٧ أيام: ${ArabicFormatters.toArabicDigits(state.weekTotal.toString())} $unitLabel")
                        Text("خلال ٣٠ يوماً: ${ArabicFormatters.toArabicDigits(state.monthTotal.toString())} $unitLabel")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onOpenMemorization, modifier = Modifier.fillMaxWidth()) {
                        Text("الانتقال إلى قسم الحفظ والمراجعة")
                    }
                }
            }

            item { SectionHeader(title = "سجل القراءة") }

            if (state.logs.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.MenuBook,
                        title = "لا يوجد سجل تلاوة بعد",
                        description = "ابدأ بتسجيل وردك اليومي من القرآن الكريم ليظهر هنا."
                    )
                }
            } else {
                items(state.logs, key = { it.id }) { log ->
                    RafiqCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${ArabicFormatters.toArabicDigits(log.amountRead.toString())} ${log.unitLabel}",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = ArabicFormatters.formatGregorianShort(LocalDate.ofEpochDay(log.dateEpochDay)) +
                                        if (log.surahNote.isNotBlank()) " • ${log.surahNote}" else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteLog(log) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف السجل")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoalDialog && goal != null) {
        QuranGoalEditorDialog(
            goal = goal,
            onDismiss = { showGoalDialog = false },
            onSave = {
                viewModel.saveGoal(it)
                showGoalDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuranGoalEditorDialog(
    goal: QuranGoalEntity,
    onDismiss: () -> Unit,
    onSave: (QuranGoalEntity) -> Unit
) {
    var goalType by remember { mutableStateOf(goal.goalType) }
    var targetText by remember { mutableStateOf(goal.targetAmount.toString()) }
    var customUnit by remember { mutableStateOf(goal.customUnitLabel) }
    var reminderEnabled by remember { mutableStateOf(goal.reminderEnabled) }
    var hour by remember { mutableIntStateOf(goal.reminderHour) }
    var minute by remember { mutableIntStateOf(goal.reminderMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل هدف ورد القرآن") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuranGoalType.entries.forEach { gt ->
                        FilterChip(
                            selected = goalType == gt,
                            onClick = { goalType = gt },
                            label = { Text(gt.labelAr) }
                        )
                    }
                }
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("المقدار اليومي") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (goalType == QuranGoalType.CUSTOM) {
                    OutlinedTextField(
                        value = customUnit,
                        onValueChange = { customUnit = it },
                        label = { Text("اسم الوحدة المخصصة (مثل: وجه / سورة)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير يومي بالورد")
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (reminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { hour = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 23) },
                            label = { Text("الساعة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = { minute = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("الدقيقة") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    goal.copy(
                        goalType = goalType,
                        targetAmount = (targetText.toDoubleOrNull() ?: 4.0).coerceAtLeast(0.5),
                        customUnitLabel = customUnit.ifBlank { "وحدة" },
                        reminderEnabled = reminderEnabled,
                        reminderHour = hour,
                        reminderMinute = minute
                    )
                )
            }) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
