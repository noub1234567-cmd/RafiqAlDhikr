package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RescheduleReminderDialog(
    activityTitle: String,
    onDismiss: () -> Unit,
    onConfirmTimeMillis: (Long) -> Unit
) {
    val nowTime = LocalTime.now().plusMinutes(30)
    val timeState = rememberTimePickerState(
        initialHour = nowTime.hour,
        initialMinute = nowTime.minute,
        is24Hour = false
    )
    var forTomorrow by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "إعادة جدولة التذكير",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "اختر وقتاً جديداً للتذكير بـ «$activityTitle». لن يُحتسب النشاط مكتملاً حتى تضغط على زر الإنجاز بنفسك.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 60, 120).forEach { minutes ->
                        OutlinedButton(
                            onClick = {
                                onConfirmTimeMillis(System.currentTimeMillis() + minutes * 60_000L)
                            }
                        ) {
                            Text("بعد ${ArabicFormatters.toArabicDigits(minutes)} د")
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !forTomorrow,
                        onClick = { forTomorrow = false },
                        label = { Text("اليوم") }
                    )
                    FilterChip(
                        selected = forTomorrow,
                        onClick = { forTomorrow = true },
                        label = { Text("غداً") }
                    )
                }

                TimePicker(
                    state = timeState,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val baseDate = if (forTomorrow) LocalDate.now().plusDays(1) else LocalDate.now()
                    val picked = ZonedDateTime.of(
                        baseDate,
                        LocalTime.of(timeState.hour, timeState.minute),
                        ZoneId.systemDefault()
                    ).toInstant().toEpochMilli()
                    val finalMillis = if (picked <= System.currentTimeMillis() && !forTomorrow) {
                        ZonedDateTime.of(
                            baseDate.plusDays(1),
                            LocalTime.of(timeState.hour, timeState.minute),
                            ZoneId.systemDefault()
                        ).toInstant().toEpochMilli()
                    } else {
                        picked
                    }
                    onConfirmTimeMillis(finalMillis)
                }
            ) {
                Text("حفظ الموعد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
