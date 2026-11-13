package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.model.ActivityCategory
import com.rafiqaldhikr.app.core.model.GoalPeriod
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.core.model.ScheduleType
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivityEditorDialog(
    profileId: Long,
    existing: WorshipActivityEntity? = null,
    defaultCategory: ActivityCategory = ActivityCategory.CUSTOM,
    onDismiss: () -> Unit,
    onSave: (WorshipActivityEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var category by remember { mutableStateOf(existing?.category ?: defaultCategory) }
    var goalPeriod by remember { mutableStateOf(existing?.goalPeriod ?: GoalPeriod.DAILY) }
    var scheduleType by remember { mutableStateOf(existing?.scheduleType ?: ScheduleType.DAILY) }
    var weekdaysMask by remember { mutableIntStateOf(existing?.weekdaysMask ?: 0b1111111) }
    var intervalDays by remember { mutableStateOf((existing?.customIntervalDays ?: 2).toString()) }
    var oneTimeDaysOffset by remember { mutableStateOf("0") }
    var reminderEnabled by remember { mutableStateOf(existing?.reminderEnabled ?: true) }
    var hour by remember { mutableIntStateOf(existing?.reminderHour ?: 9) }
    var minute by remember { mutableIntStateOf(existing?.reminderMinute ?: 0) }
    var soundMode by remember { mutableStateOf(existing?.soundMode ?: NotificationSoundMode.NORMAL) }
    var targetReps by remember { mutableStateOf((existing?.targetRepetitions ?: 1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "إضافة عبادة أو هدف مخصص" else "تعديل العبادة",
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
                    onValueChange = { title = it },
                    label = { Text("اسم العبادة أو الهدف") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف مختصر (اختياري)") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("التصنيف", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActivityCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.labelAr) }
                        )
                    }
                }

                Text("نوع الهدف", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoalPeriod.entries.forEach { gp ->
                        FilterChip(
                            selected = goalPeriod == gp,
                            onClick = { goalPeriod = gp },
                            label = { Text(gp.labelAr) }
                        )
                    }
                }

                Text("الجدول الزمني", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ScheduleType.entries.forEach { st ->
                        FilterChip(
                            selected = scheduleType == st,
                            onClick = { scheduleType = st },
                            label = { Text(st.labelAr) }
                        )
                    }
                }

                if (scheduleType == ScheduleType.SPECIFIC_WEEKDAYS) {
                    Text("أيام الأسبوع", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        DayOfWeek.entries.forEach { day ->
                            val bit = 1 shl (day.value - 1)
                            val checked = (weekdaysMask and bit) != 0
                            FilterChip(
                                selected = checked,
                                onClick = {
                                    weekdaysMask = if (checked) weekdaysMask and bit.inv() else weekdaysMask or bit
                                },
                                label = { Text(ArabicFormatters.weekdayNameAr(day)) }
                            )
                        }
                    }
                }

                if (scheduleType == ScheduleType.CUSTOM_INTERVAL) {
                    OutlinedTextField(
                        value = intervalDays,
                        onValueChange = { intervalDays = it.filter(Char::isDigit) },
                        label = { Text("التكرار كل (عدد الأيام)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (scheduleType == ScheduleType.ONE_TIME) {
                    OutlinedTextField(
                        value = oneTimeDaysOffset,
                        onValueChange = { oneTimeDaysOffset = it.filter(Char::isDigit) },
                        label = { Text("بعد كم يوم من اليوم؟ (٠ = اليوم)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = targetReps,
                    onValueChange = { targetReps = it.filter(Char::isDigit) },
                    label = { Text("الهدف التكراري") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل التذكير", style = MaterialTheme.typography.titleMedium)
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                }

                if (reminderEnabled) {
                    Text(
                        text = "وقت التذكير: ${ArabicFormatters.formatTime12Ar(hour, minute)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = {
                                val v = it.filter(Char::isDigit).toIntOrNull()
                                if (v != null) hour = v.coerceIn(0, 23) else if (it.isEmpty()) hour = 0
                            },
                            label = { Text("الساعة (٠-٢٣)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = {
                                val v = it.filter(Char::isDigit).toIntOrNull()
                                if (v != null) minute = v.coerceIn(0, 59) else if (it.isEmpty()) minute = 0
                            },
                            label = { Text("الدقيقة (٠-٥٩)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NotificationSoundMode.entries.forEach { mode ->
                            FilterChip(
                                selected = soundMode == mode,
                                onClick = { soundMode = mode },
                                label = { Text(mode.labelAr) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val oneTimeEpoch = if (scheduleType == ScheduleType.ONE_TIME) {
                        existing?.oneTimeDateEpochDay ?: (todayEpoch + (oneTimeDaysOffset.toLongOrNull() ?: 0L))
                    } else null
                    onSave(
                        (existing ?: WorshipActivityEntity(
                            profileId = profileId,
                            title = title.trim(),
                            anchorEpochDay = todayEpoch
                        )).copy(
                            title = title.trim(),
                            description = description.trim(),
                            category = category,
                            goalPeriod = goalPeriod,
                            scheduleType = scheduleType,
                            weekdaysMask = if (weekdaysMask == 0) 0b1111111 else weekdaysMask,
                            oneTimeDateEpochDay = oneTimeEpoch,
                            customIntervalDays = (intervalDays.toIntOrNull() ?: 1).coerceAtLeast(1),
                            reminderEnabled = reminderEnabled,
                            reminderHour = hour.coerceIn(0, 23),
                            reminderMinute = minute.coerceIn(0, 59),
                            soundMode = soundMode,
                            targetRepetitions = (targetReps.toIntOrNull() ?: 1).coerceAtLeast(1)
                        )
                    )
                }
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
