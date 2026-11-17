package com.rafiqaldhikr.app.ui.screens.planner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.planner.PlannerType
import com.rafiqaldhikr.app.core.planner.PlannerWeek
import com.rafiqaldhikr.app.data.local.db.entities.PlannerSlotEntity
import com.rafiqaldhikr.app.ui.components.ConfirmHost
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.rememberConfirm

private const val MIN_SCALE = 0.6f
private const val MAX_SCALE = 2.2f

private class EditorTarget(val row: Int, val day: Int, val slot: PlannerSlotEntity?)

@Composable
fun PlannerScreen(viewModel: PlannerViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val confirm = rememberConfirm()
    ConfirmHost(confirm)
    var scale by remember { mutableStateOf(1f) }
    var editor by remember { mutableStateOf<EditorTarget?>(null) }
    val week = state.week

    Scaffold(topBar = { RafiqTopBar(title = "جدول العبادات الأسبوعي", onBackClick = onBack) }) { padding ->
        if (week == null) {
            NewWeekCard(
                enabled = state.activeProfile != null,
                hasPrevious = state.hasPrevious,
                onCopy = viewModel::copyLast,
                onCreate = viewModel::createNew,
                modifier = Modifier.padding(padding)
            )
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Controls(
                    rows = week.rowCount,
                    autoRepeat = week.autoRepeat,
                    scale = scale,
                    onZoom = { scale = (scale * it).coerceIn(MIN_SCALE, MAX_SCALE) },
                    onAddRow = { viewModel.setRows(week.rowCount + 1) },
                    onRemoveRow = {
                        val last = week.rowCount - 1
                        if (state.slots.any { it.rowIndex == last }) {
                            confirm.ask("سيُحذف ما في الصف الأخير من خانات. هل تريد المتابعة؟") {
                                viewModel.setRows(week.rowCount - 1)
                            }
                        } else {
                            viewModel.setRows(week.rowCount - 1)
                        }
                    },
                    onAutoRepeat = viewModel::setAutoRepeat
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            // Pinch to zoom, only on this page; single-finger scrolling is untouched.
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                do {
                                    val event = awaitPointerEvent()
                                    if (event.changes.size >= 2) {
                                        val zoom = event.calculateZoom()
                                        if (zoom != 1f) {
                                            scale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                                            event.changes.forEach { it.consume() }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                ) {
                    PlannerGrid(
                        rows = week.rowCount,
                        slots = state.slots,
                        weekStartEpochDay = state.weekStart.toEpochDay(),
                        todayEpochDay = state.today.toEpochDay(),
                        scale = scale,
                        onCell = { r, d, s -> editor = EditorTarget(r, d, s) },
                        onToggle = viewModel::toggleDone
                    )
                }
            }
        }
    }

    val target = editor
    if (target != null && week != null) {
        val live = target.slot?.let { old -> state.slots.firstOrNull { it.id == old.id } }
        val occupied = state.slots
            .filter { it.rowIndex == target.row && it.id != target.slot?.id }
            .map { it.dayIndex }.toSet()
        SlotEditorDialog(
            row = target.row,
            day = target.day,
            slot = live,
            occupiedDays = occupied,
            onDismiss = { editor = null },
            onSave = { title, type, time, extra ->
                viewModel.saveSlot(live?.id, target.row, target.day, title, type, time, extra)
                editor = null
            },
            onDelete = live?.let { s -> { viewModel.deleteSlot(s.id); editor = null } },
            onToggleDone = live?.let { s -> { viewModel.toggleDone(s.id) } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewWeekCard(
    enabled: Boolean,
    hasPrevious: Boolean,
    onCopy: () -> Unit,
    onCreate: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(PlannerWeek.DEFAULT_ROWS.toString()) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RafiqCard {
            Text("أسبوع جديد", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                "جدولك لهذا الأسبوع فارغ. ابدأ من خطة الأسبوع الماضي أو أنشئ جدولاً جديداً.",
                style = MaterialTheme.typography.bodyMedium
            )
            if (hasPrevious) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onCopy, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                    Text("نسخ خطة الأسبوع الماضي")
                }
            }
        }
        RafiqCard {
            Text("ابدأ جدولاً جديداً", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text("اختر عدد الصفوف (عدد العبادات في اليوم). يمكنك تعديله لاحقاً.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3, 6, 10, 20).forEach { n ->
                    OutlinedButton(onClick = { text = n.toString() }) { Text(ArabicFormatters.toArabicDigits(n)) }
                }
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("عدد الصفوف (1–${PlannerWeek.MAX_ROWS})") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onCreate(text.toIntOrNull() ?: PlannerWeek.DEFAULT_ROWS) },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) { Text("إنشاء الجدول") }
        }
    }
}

@Composable
private fun Controls(
    rows: Int,
    autoRepeat: Boolean,
    scale: Float,
    onZoom: (Float) -> Unit,
    onAddRow: () -> Unit,
    onRemoveRow: () -> Unit,
    onAutoRepeat: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("الصفوف: ${ArabicFormatters.toArabicDigits(rows)}", style = MaterialTheme.typography.labelLarge)
            IconButton(onClick = onRemoveRow, enabled = rows > PlannerWeek.MIN_ROWS) {
                Icon(Icons.Default.Remove, contentDescription = "إنقاص صف")
            }
            IconButton(onClick = onAddRow, enabled = rows < PlannerWeek.MAX_ROWS) {
                Icon(Icons.Default.Add, contentDescription = "زيادة صف")
            }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { onZoom(0.85f) }, enabled = scale > MIN_SCALE) {
                Icon(Icons.Default.ZoomOut, contentDescription = "تصغير")
            }
            IconButton(onClick = { onZoom(1.18f) }, enabled = scale < MAX_SCALE) {
                Icon(Icons.Default.ZoomIn, contentDescription = "تكبير")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "تكرار الجدول تلقائياً كل أسبوع",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(checked = autoRepeat, onCheckedChange = onAutoRepeat)
        }
    }
}

@Composable
private fun PlannerGrid(
    rows: Int,
    slots: List<PlannerSlotEntity>,
    weekStartEpochDay: Long,
    todayEpochDay: Long,
    scale: Float,
    onCell: (Int, Int, PlannerSlotEntity?) -> Unit,
    onToggle: (Long) -> Unit
) {
    val cellW = (84f * scale).dp
    val cellH = (66f * scale).dp
    val headH = (46f * scale).dp
    val labelW = (26f * scale).dp
    val fontSp = (11f * scale).sp
    val byCell = slots.associateBy { it.rowIndex to it.dayIndex }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 6.dp)) {
            Row {
                Spacer(Modifier.width(labelW))
                for (d in 0 until PlannerWeek.DAYS) {
                    val isToday = weekStartEpochDay + d == todayEpochDay
                    val dom = java.time.LocalDate.ofEpochDay(weekStartEpochDay + d).dayOfMonth
                    Box(
                        modifier = Modifier
                            .padding(1.dp)
                            .size(cellW, headH)
                            .background(
                                if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                                RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                PlannerWeek.dayNamesAr[d],
                                fontSize = fontSp,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                ArabicFormatters.toArabicDigits(dom),
                                fontSize = fontSp,
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            for (r in 0 until rows) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        ArabicFormatters.toArabicDigits(r + 1),
                        fontSize = fontSp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(labelW)
                    )
                    for (d in 0 until PlannerWeek.DAYS) {
                        PlannerCell(
                            slot = byCell[r to d],
                            width = cellW,
                            height = cellH,
                            fontSp = fontSp,
                            iconSize = (18f * scale).dp,
                            onClick = { onCell(r, d, byCell[r to d]) },
                            onToggle = onToggle
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlannerCell(
    slot: PlannerSlotEntity?,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    fontSp: androidx.compose.ui.unit.TextUnit,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    onToggle: (Long) -> Unit
) {
    val done = slot != null && slot.doneAtMillis != 0L
    val bg = when {
        slot == null -> MaterialTheme.colorScheme.surfaceContainerLowest
        done -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    Box(
        modifier = Modifier
            .padding(1.dp)
            .size(width, height)
            .background(bg, RoundedCornerShape(6.dp))
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        if (slot != null) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (done) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = if (done) "تمّت" else "لم تتم",
                        tint = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(iconSize).clickable { onToggle(slot.id) }
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        ArabicFormatters.toArabicDigits(PlannerWeek.formatTime(slot.timeMinutes)),
                        fontSize = fontSp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    slot.title,
                    fontSize = fontSp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SlotEditorDialog(
    row: Int,
    day: Int,
    slot: PlannerSlotEntity?,
    occupiedDays: Set<Int>,
    onDismiss: () -> Unit,
    onSave: (title: String, type: String, timeMinutes: Int, extraDays: Set<Int>) -> Unit,
    onDelete: (() -> Unit)?,
    onToggleDone: (() -> Unit)?
) {
    var title by remember { mutableStateOf(slot?.title ?: "") }
    var type by remember { mutableStateOf(slot?.type ?: PlannerType.OTHER.name) }
    var hasAlarm by remember { mutableStateOf(slot?.let { it.timeMinutes >= 0 } ?: true) }
    var minutes by remember { mutableStateOf(slot?.timeMinutes?.takeIf { it >= 0 } ?: (5 * 60)) }
    var extraDays by remember { mutableStateOf(setOf<Int>()) }
    var showTime by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "${PlannerWeek.dayNamesAr[day]} · الصف ${ArabicFormatters.toArabicDigits(row + 1)}",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it.take(60) },
                    label = { Text("اسم العبادة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("النوع", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PlannerType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t.name,
                            onClick = { type = t.name },
                            label = { Text(t.labelAr) }
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("منبّه", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it })
                }
                if (hasAlarm) {
                    OutlinedButton(onClick = { showTime = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("الوقت: " + ArabicFormatters.toArabicDigits(PlannerWeek.formatTime(minutes)))
                    }
                }
                Text("تكرار في أيام أخرى (نسخ مستقلة في نفس الصف)", style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (d in 0 until PlannerWeek.DAYS) {
                        if (d == day) continue
                        FilterChip(
                            selected = d in extraDays,
                            onClick = { extraDays = if (d in extraDays) extraDays - d else extraDays + d },
                            enabled = d !in occupiedDays,
                            label = { Text(PlannerWeek.dayNamesAr[d]) }
                        )
                    }
                }
                if (onToggleDone != null && slot != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { onToggleDone() }
                    ) {
                        Checkbox(checked = slot.doneAtMillis != 0L, onCheckedChange = { onToggleDone() })
                        Text("تمّت")
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("حذف هذه الخانة") }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = { onSave(title, type, if (hasAlarm) minutes else PlannerWeek.NO_TIME, extraDays) }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )

    if (showTime) {
        val timeState = rememberTimePickerState(
            initialHour = minutes / 60,
            initialMinute = minutes % 60,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("وقت المنبّه") },
            text = { TimePicker(state = timeState, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(onClick = {
                    minutes = timeState.hour * 60 + timeState.minute
                    showTime = false
                }) { Text("تم") }
            },
            dismissButton = { TextButton(onClick = { showTime = false }) { Text("إلغاء") } }
        )
    }
}
