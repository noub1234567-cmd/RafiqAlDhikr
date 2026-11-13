package com.rafiqaldhikr.app.ui.screens.fasting

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.calendar.HijriCalendarHelper
import com.rafiqaldhikr.app.core.model.FastingOccasion
import com.rafiqaldhikr.app.data.local.db.entities.FastingDayEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.SectionHeader
import com.rafiqaldhikr.app.ui.theme.StatusCompletedColor
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FastingScreen(
    viewModel: FastingViewModel,
    onOpenProfileSwitcher: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showOffsetDialog by remember { mutableStateOf(false) }

    val savedByEpoch = remember(state.fastingDays) { state.fastingDays.associateBy { it.dateEpochDay } }
    val ym = state.displayedMonth
    val firstDay = ym.atDay(1)
    val daysInMonth = ym.lengthOfMonth()
    // Saturday-first grid (Sat=0, Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6)
    val startOffset = (firstDay.dayOfWeek.value + 1) % 7

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(
            title = "الصيام والتقويم الهجري",
            activeProfile = state.activeProfile,
            onProfileClick = onOpenProfileSwitcher,
            actions = {
                IconButton(onClick = { showOffsetDialog = true }) {
                    Icon(Icons.Default.Tune, contentDescription = "ضبط التقويم الهجري")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                RafiqCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)) {
                    Text(
                        text = "تنبيه تقويمي: التاريخ الهجري المعروض محسوب فلكياً (مع تعديل يدوي حالي: ${ArabicFormatters.toArabicDigits(state.hijriOffsetDays)} يوم)، وقد يختلف يوماً حسب ثبوت رؤية الهلال في بلدك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(onClick = { showOffsetDialog = true }) {
                        Text("تصحيح التاريخ الهجري يدوياً")
                    }
                }
            }

            item {
                RafiqCard {
                    val midHijri = HijriCalendarHelper.toHijri(ym.atDay(15), state.hijriOffsetDays)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "الشهر السابق")
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${ArabicFormatters.formatGregorianShort(ym.atDay(1)).substringAfter(" ")}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "يوافق: ${midHijri.monthNameAr} ${ArabicFormatters.toArabicDigits(midHijri.year)} هـ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "الشهر التالي")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("سبت", "أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة").forEach { d ->
                            Text(
                                text = d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    val totalCells = startOffset + daysInMonth
                    val rows = (totalCells + 6) / 7
                    for (r in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0..6) {
                                val cellIdx = r * 7 + c
                                val dayNum = cellIdx - startOffset + 1
                                if (dayNum in 1..daysInMonth) {
                                    val date = ym.atDay(dayNum)
                                    val hijri = HijriCalendarHelper.toHijri(date, state.hijriOffsetDays)
                                    val recommended = HijriCalendarHelper.getRecommendedFastingOccasions(date, state.hijriOffsetDays)
                                    val saved = savedByEpoch[date.toEpochDay()]
                                    val isToday = date == LocalDate.now()

                                    val bg = when {
                                        saved?.isCompleted == true -> StatusCompletedColor.copy(alpha = 0.25f)
                                        saved?.isPlanned == true -> MaterialTheme.colorScheme.primaryContainer
                                        recommended.isNotEmpty() -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(bg)
                                            .then(
                                                if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                                else Modifier
                                            )
                                            .clickable { selectedDate = date }
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = ArabicFormatters.toArabicDigits(dayNum),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${ArabicFormatters.toArabicDigits(hijri.day)}هـ",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("• أيام ملوّنة فاتحة: الإثنين/الخميس أو البيض أو مناسبات الصيام", style = MaterialTheme.typography.labelMedium)
                        Text("• اضغط على أي يوم لجدولة الصيام أو تسجيل إتمامه", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            item { SectionHeader(title = "سجل وأيام الصيام المختارة") }

            items(state.fastingDays, key = { it.id }) { item ->
                val date = LocalDate.ofEpochDay(item.dateEpochDay)
                val hijri = HijriCalendarHelper.toHijri(date, state.hijriOffsetDays)
                RafiqCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.occasion.labelAr, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${hijri.formattedArabic()} • ${ArabicFormatters.formatGregorianShort(date)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = item.isCompleted,
                                onCheckedChange = { viewModel.markCompleted(item, it) }
                            )
                            IconButton(onClick = { viewModel.removeDay(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف")
                            }
                        }
                    }
                }
            }
        }
    }

    selectedDate?.let { date ->
        val existing = savedByEpoch[date.toEpochDay()]
        FastingDayDialog(
            date = date,
            hijriOffset = state.hijriOffsetDays,
            existing = existing,
            onDismiss = { selectedDate = null },
            onSave = { occ, planned, completed, remEnabled, hr, min, notes ->
                viewModel.saveFastingDay(date, occ, planned, completed, remEnabled, hr, min, notes)
                selectedDate = null
            }
        )
    }

    if (showOffsetDialog) {
        HijriOffsetDialog(
            currentOffset = state.hijriOffsetDays,
            onDismiss = { showOffsetDialog = false },
            onSelect = {
                viewModel.setHijriOffset(it)
                showOffsetDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FastingDayDialog(
    date: LocalDate,
    hijriOffset: Int,
    existing: FastingDayEntity?,
    onDismiss: () -> Unit,
    onSave: (FastingOccasion, Boolean, Boolean, Boolean, Int, Int, String) -> Unit
) {
    val hijri = HijriCalendarHelper.toHijri(date, hijriOffset)
    val recommended = HijriCalendarHelper.getRecommendedFastingOccasions(date, hijriOffset)
    val prohibited = HijriCalendarHelper.isEidProhibitedFastingDay(date, hijriOffset)

    var occasion by remember { mutableStateOf(existing?.occasion ?: recommended.firstOrNull() ?: FastingOccasion.VOLUNTARY) }
    var isPlanned by remember { mutableStateOf(existing?.isPlanned ?: true) }
    var isCompleted by remember { mutableStateOf(existing?.isCompleted ?: false) }
    var reminderEnabled by remember { mutableStateOf(existing?.reminderEnabled ?: false) }
    var hour by remember { mutableIntStateOf(existing?.reminderHour ?: 20) }
    var minute by remember { mutableIntStateOf(existing?.reminderMinute ?: 0) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(hijri.formattedArabic()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(ArabicFormatters.formatGregorianDate(date), style = MaterialTheme.typography.bodyMedium)
                if (prohibited) {
                    Text(
                        text = "ملاحظة: يوافق هذا اليوم يوم عيد أو أيام تشريق وفق التاريخ المعروض.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (recommended.isNotEmpty()) {
                    Text(
                        text = "مناسبات مقترحة لهذا اليوم: ${recommended.joinToString("، ") { it.labelAr }}",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FastingOccasion.entries.forEach { occ ->
                        FilterChip(
                            selected = occasion == occ,
                            onClick = { occasion = occ },
                            label = { Text(occ.labelAr) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("جدولة هذا اليوم للصيام")
                    Switch(checked = isPlanned, onCheckedChange = { isPlanned = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تم الصيام بحمد الله")
                    Switch(checked = isCompleted, onCheckedChange = { isCompleted = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تذكير في الليلة السابقة")
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }
                if (reminderEnabled) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { hour = (it.filter(Char::isDigit).toIntOrNull() ?: 20).coerceIn(0, 23) },
                            label = { Text("الساعة") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = { minute = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("الدقيقة") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظة") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(occasion, isPlanned, isCompleted, reminderEnabled, hour, minute, notes) }) {
                Text("حفظ")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}

@Composable
private fun HijriOffsetDialog(
    currentOffset: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل التاريخ الهجري") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("اختر فرق الأيام لتطابق رؤية الهلال المحلية في منطقتك:")
                (-2..2).forEach { offset ->
                    val label = when {
                        offset < 0 -> "${ArabicFormatters.toArabicDigits(offset)} يوم"
                        offset == 0 -> "بدون تعديل (٠)"
                        else -> "+${ArabicFormatters.toArabicDigits(offset)} يوم"
                    }
                    FilterChip(
                        selected = currentOffset == offset,
                        onClick = { onSelect(offset) },
                        label = { Text(label) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}
