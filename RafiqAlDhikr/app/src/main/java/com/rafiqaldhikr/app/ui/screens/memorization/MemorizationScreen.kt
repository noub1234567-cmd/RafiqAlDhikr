package com.rafiqaldhikr.app.ui.screens.memorization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.rafiqaldhikr.app.core.model.QuranSurahs
import com.rafiqaldhikr.app.data.local.db.entities.MemorizationPlanEntity
import com.rafiqaldhikr.app.ui.components.EmptyStateCard
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.SectionHeader

@Composable
fun MemorizationScreen(
    viewModel: MemorizationViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showPlanEditor by remember { mutableStateOf(false) }
    var editingPlan by remember { mutableStateOf<MemorizationPlanEntity?>(null) }
    var sessionTarget by remember { mutableStateOf<Pair<MemorizationPlanEntity, Boolean>?>(null) }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "حفظ القرآن ومراجعته",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showPlanEditor = true }) {
                Icon(Icons.Default.Add, contentDescription = "خطة حفظ جديدة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SectionHeader(title = "خطط الحفظ والمراجعة") }

            if (state.plans.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Book,
                        title = "لا توجد خطط حفظ حالياً",
                        description = "أضف خطة لسورة محددة وحدد الآيات ومواعيد الحفظ والمراجعة المستقلة."
                    )
                }
            } else {
                items(state.plans, key = { it.id }) { plan ->
                    val surah = QuranSurahs.byNumber(plan.surahNumber)
                    val totalSpan = (plan.endAyah - plan.startAyah + 1).coerceAtLeast(1)
                    val doneSpan = (plan.currentAyah - plan.startAyah + 1).coerceIn(0, totalSpan)
                    val progress = doneSpan.toFloat() / totalSpan

                    RafiqCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(plan.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "سورة ${surah.nameAr} • الآيات ${ArabicFormatters.toArabicDigits(plan.startAyah)} - ${ArabicFormatters.toArabicDigits(plan.endAyah)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row {
                                IconButton(onClick = { editingPlan = plan }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل")
                                }
                                IconButton(onClick = { viewModel.deletePlan(plan) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().height(7.dp))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "المحفوظ حتى الآية ${ArabicFormatters.toArabicDigits(plan.currentAyah)} • مقدار الجلسة: ${ArabicFormatters.toArabicDigits(plan.versesPerSession)} آيات",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تذكير الحفظ: ${if (plan.memReminderEnabled) ArabicFormatters.formatTime12Ar(plan.memReminderHour, plan.memReminderMinute) else "غير مفعّل"} | تذكير المراجعة: ${if (plan.revReminderEnabled) ArabicFormatters.formatTime12Ar(plan.revReminderHour, plan.revReminderMinute) else "غير مفعّل"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { sessionTarget = plan to false },
                                modifier = Modifier.weight(1f)
                            ) { Text("تسجيل حفظ") }
                            OutlinedButton(
                                onClick = { sessionTarget = plan to true },
                                modifier = Modifier.weight(1f)
                            ) { Text("تسجيل مراجعة") }
                        }
                    }
                }
            }

            if (state.sessions.isNotEmpty()) {
                item { SectionHeader(title = "سجل جلسات الحفظ والمراجعة") }
                items(state.sessions, key = { "s_${it.id}" }) { s ->
                    val planTitle = state.plans.firstOrNull { it.id == s.planId }?.title ?: "خطة محفوظة"
                    RafiqCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${if (s.isRevision) "مراجعة" else "حفظ"}: $planTitle",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "من الآية ${ArabicFormatters.toArabicDigits(s.fromAyah)} إلى ${ArabicFormatters.toArabicDigits(s.toAyah)} • ${ArabicFormatters.formatInstantDateTime(s.completedAtMillis)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteSession(s) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف")
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }

    val dialogProfileId = state.activeProfile?.id
    if (dialogProfileId != null && (showPlanEditor || editingPlan != null)) {
        val profileId: Long = dialogProfileId
        PlanEditorDialog(
            profileId = profileId,
            existing = editingPlan,
            onDismiss = {
                showPlanEditor = false
                editingPlan = null
            },
            onSave = {
                viewModel.savePlan(it)
                showPlanEditor = false
                editingPlan = null
            }
        )
    }

    sessionTarget?.let { (plan, isRevision) ->
        RecordSessionDialog(
            plan = plan,
            isRevision = isRevision,
            onDismiss = { sessionTarget = null },
            onConfirm = { fromAyah, toAyah, notes ->
                viewModel.recordSession(plan, isRevision, fromAyah, toAyah, notes)
                sessionTarget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlanEditorDialog(
    profileId: Long,
    existing: MemorizationPlanEntity?,
    onDismiss: () -> Unit,
    onSave: (MemorizationPlanEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "خطة حفظ سورة") }
    var surahNumber by remember { mutableIntStateOf(existing?.surahNumber ?: 67) }
    var expanded by remember { mutableStateOf(false) }
    var startAyah by remember { mutableStateOf((existing?.startAyah ?: 1).toString()) }
    var endAyah by remember { mutableStateOf((existing?.endAyah ?: QuranSurahs.byNumber(surahNumber).ayahCount).toString()) }
    var versesPerSession by remember { mutableStateOf((existing?.versesPerSession ?: 5).toString()) }
    var memReminderEnabled by remember { mutableStateOf(existing?.memReminderEnabled ?: false) }
    var memHour by remember { mutableIntStateOf(existing?.memReminderHour ?: 7) }
    var memMin by remember { mutableIntStateOf(existing?.memReminderMinute ?: 0) }
    var revReminderEnabled by remember { mutableStateOf(existing?.revReminderEnabled ?: false) }
    var revHour by remember { mutableIntStateOf(existing?.revReminderHour ?: 18) }
    var revMin by remember { mutableIntStateOf(existing?.revReminderMinute ?: 0) }

    val selectedSurah = QuranSurahs.byNumber(surahNumber)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة خطة حفظ" else "تعديل الخطة") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم الخطة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = "${selectedSurah.number}. سورة ${selectedSurah.nameAr} (${selectedSurah.ayahCount} آية)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("السورة") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        QuranSurahs.all.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.number}. سورة ${s.nameAr} (${s.ayahCount} آية)") },
                                onClick = {
                                    surahNumber = s.number
                                    startAyah = "1"
                                    endAyah = s.ayahCount.toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startAyah,
                        onValueChange = { startAyah = it.filter(Char::isDigit) },
                        label = { Text("من الآية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endAyah,
                        onValueChange = { endAyah = it.filter(Char::isDigit) },
                        label = { Text("إلى الآية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = versesPerSession,
                    onValueChange = { versesPerSession = it.filter(Char::isDigit) },
                    label = { Text("عدد الآيات في الجلسة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير الحفظ المستقل")
                    Switch(checked = memReminderEnabled, onCheckedChange = { memReminderEnabled = it })
                }
                if (memReminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = memHour.toString(),
                            onValueChange = { memHour = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 23) },
                            label = { Text("ساعة الحفظ") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = memMin.toString(),
                            onValueChange = { memMin = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("دقيقة الحفظ") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير المراجعة المستقل")
                    Switch(checked = revReminderEnabled, onCheckedChange = { revReminderEnabled = it })
                }
                if (revReminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = revHour.toString(),
                            onValueChange = { revHour = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 23) },
                            label = { Text("ساعة المراجعة") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = revMin.toString(),
                            onValueChange = { revMin = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("دقيقة المراجعة") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val s = (startAyah.toIntOrNull() ?: 1).coerceIn(1, selectedSurah.ayahCount)
                    val e = (endAyah.toIntOrNull() ?: selectedSurah.ayahCount).coerceIn(s, selectedSurah.ayahCount)
                    onSave(
                        (existing ?: MemorizationPlanEntity(
                            profileId = profileId,
                            title = title.trim(),
                            surahNumber = surahNumber,
                            startAyah = s,
                            endAyah = e,
                            currentAyah = s - 1
                        )).copy(
                            title = title.trim(),
                            surahNumber = surahNumber,
                            startAyah = s,
                            endAyah = e,
                            currentAyah = existing?.currentAyah ?: (s - 1),
                            versesPerSession = (versesPerSession.toIntOrNull() ?: 5).coerceAtLeast(1),
                            memReminderEnabled = memReminderEnabled,
                            memReminderHour = memHour,
                            memReminderMinute = memMin,
                            revReminderEnabled = revReminderEnabled,
                            revReminderHour = revHour,
                            revReminderMinute = revMin
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun RecordSessionDialog(
    plan: MemorizationPlanEntity,
    isRevision: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    val defaultFrom = if (isRevision) plan.startAyah else (plan.currentAyah + 1).coerceIn(plan.startAyah, plan.endAyah)
    val defaultTo = if (isRevision) plan.currentAyah.coerceAtLeast(plan.startAyah) else (defaultFrom + plan.versesPerSession - 1).coerceAtMost(plan.endAyah)

    var fromText by remember { mutableStateOf(defaultFrom.toString()) }
    var toText by remember { mutableStateOf(defaultTo.toString()) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isRevision) "تسجيل جلسة مراجعة" else "تسجيل جلسة حفظ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromText,
                        onValueChange = { fromText = it.filter(Char::isDigit) },
                        label = { Text("من آية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = toText,
                        onValueChange = { toText = it.filter(Char::isDigit) },
                        label = { Text("إلى آية") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات (اختياري)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val f = (fromText.toIntOrNull() ?: defaultFrom).coerceIn(plan.startAyah, plan.endAyah)
                val t = (toText.toIntOrNull() ?: defaultTo).coerceIn(f, plan.endAyah)
                onConfirm(f, t, notes)
            }) { Text("تسجيل") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
