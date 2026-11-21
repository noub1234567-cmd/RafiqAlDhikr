package com.rafiqaldhikr.app.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.memorization.MemoPlanMath
import java.time.LocalDate

/** Arabic weekday names indexed Monday = 0 … Sunday = 6, as in the study-days mask. */
private val WEEKDAYS = listOf("الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت", "الأحد")

/** Display order of the week for Arabic readers: Saturday first. */
private val WEEK_ORDER = listOf(5, 6, 0, 1, 2, 3, 4)

fun weekdayName(epochDay: Long): String = WEEKDAYS[MemoPlanMath.weekdayIndex(epochDay)]

/** "السبت 2026-11-21". */
fun dayLabel(epochDay: Long): String = weekdayName(epochDay) + " " + LocalDate.ofEpochDay(epochDay)

/** Seven chips for the study weekdays; at least one stays selected. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyDayChips(mask: Int, onChange: (Int) -> Unit) {
    val current = MemoPlanMath.normalizeMask(mask)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WEEK_ORDER.forEach { i ->
            val on = (current shr i) and 1 == 1
            FilterChip(
                selected = on,
                onClick = {
                    val next = if (on) current and (1 shl i).inv() else current or (1 shl i)
                    if (next != 0) onChange(next)
                },
                label = { Text(WEEKDAYS[i]) }
            )
        }
    }
}

/** A button showing a date that opens a date picker. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateChoice(label: String, epochDay: Long, minEpochDay: Long? = null, onPick: (Long) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { open = true }) { Text("$label: ${dayLabel(epochDay)}") }
    if (open) {
        val state = rememberDatePickerState(initialSelectedDateMillis = epochDay * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { ms ->
                        val d = Math.floorDiv(ms, 86_400_000L)
                        onPick(if (minEpochDay != null && d < minEpochDay) minEpochDay else d)
                    }
                    open = false
                }) { Text("تم") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("إلغاء") } }
        ) { DatePicker(state = state) }
    }
}
