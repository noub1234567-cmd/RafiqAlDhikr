package com.rafiqaldhikr.app.ui.screens.adhkar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
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
import com.rafiqaldhikr.app.core.model.AdhkarCategory
import com.rafiqaldhikr.app.core.model.NotificationSoundMode
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarEntryEntity
import com.rafiqaldhikr.app.data.local.db.entities.AdhkarReminderEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.theme.StatusCompletedColor

@Composable
fun AdhkarScreen(
    viewModel: AdhkarViewModel,
    onOpenProfileSwitcher: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showReminderDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<AdhkarEntryEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val currentReminder = state.remindersByCategory[state.selectedCategory]
        ?: AdhkarReminderEntity(
            profileId = state.activeProfile?.id ?: 1L,
            category = state.selectedCategory,
            hour = state.selectedCategory.defaultHour,
            minute = state.selectedCategory.defaultMinute
        )

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "الأذكار اليومية",
                activeProfile = state.activeProfile,
                onProfileClick = onOpenProfileSwitcher,
                actions = {
                    IconButton(enabled = state.activeProfile != null, onClick = { showReminderDialog = true }) {
                        Icon(Icons.Default.Alarm, contentDescription = "تذكير القسم")
                    }
                    IconButton(onClick = { viewModel.resetCategory() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "إعادة تعيين عداد اليوم")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ذكر مخصص")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AdhkarCategory.entries) { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        label = { Text(category.labelAr) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    val completedCount = state.entries.count { state.progressByEntryId[it.id]?.isCompleted == true }
                    RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(state.selectedCategory.labelAr, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = if (currentReminder.enabled) {
                                        "التذكير اليومي: ${ArabicFormatters.formatTime12Ar(currentReminder.hour, currentReminder.minute)}"
                                    } else {
                                        "التذكير اليومي غير مفعّل"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${ArabicFormatters.toArabicDigits(completedCount)} / ${ArabicFormatters.toArabicDigits(state.entries.size)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(state.entries, key = { it.id }) { entry ->
                    val progress = state.progressByEntryId[entry.id]
                    val currentCount = progress?.currentCount ?: 0
                    val isCompleted = progress?.isCompleted ?: false

                    RafiqCard(
                        containerColor = if (isCompleted) {
                            StatusCompletedColor.copy(alpha = 0.08f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                        onClick = { viewModel.increment(entry) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(entry.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Row {
                                IconButton(onClick = { editingEntry = entry }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", modifier = Modifier.size(18.dp))
                                }
                                if (entry.isCustom) {
                                    IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = entry.arabicText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "المصدر: ${entry.sourceReference}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { (currentCount.toFloat() / entry.targetCount.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.increment(entry) },
                                enabled = !isCompleted
                            ) {
                                Text(
                                    text = if (isCompleted) {
                                        "مكتمل (${ArabicFormatters.toArabicDigits(entry.targetCount)})"
                                    } else {
                                        "تسبيح: ${ArabicFormatters.toArabicDigits(currentCount)} / ${ArabicFormatters.toArabicDigits(entry.targetCount)}"
                                    }
                                )
                            }
                            OutlinedButton(onClick = { viewModel.toggleCompleted(entry) }) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isCompleted) "إلغاء الإتمام" else "تحديد كمكتمل")
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(64.dp)) }
            }
        }
    }

    if (showReminderDialog) {
        AdhkarReminderDialog(
            reminder = currentReminder,
            onDismiss = { showReminderDialog = false },
            onSave = {
                viewModel.saveReminder(it)
                showReminderDialog = false
            }
        )
    }

    val dialogProfileId = state.activeProfile?.id
    if (dialogProfileId != null && (showAddDialog || editingEntry != null)) {
        val profileId: Long = dialogProfileId
        AdhkarEditorDialog(
            profileId = profileId,
            category = state.selectedCategory,
            existing = editingEntry,
            onDismiss = {
                showAddDialog = false
                editingEntry = null
            },
            onSave = {
                viewModel.saveEntry(it)
                showAddDialog = false
                editingEntry = null
            }
        )
    }
}

@Composable
private fun AdhkarReminderDialog(
    reminder: AdhkarReminderEntity,
    onDismiss: () -> Unit,
    onSave: (AdhkarReminderEntity) -> Unit
) {
    var enabled by remember { mutableStateOf(reminder.enabled) }
    var hour by remember { mutableIntStateOf(reminder.hour) }
    var minute by remember { mutableIntStateOf(reminder.minute) }
    var soundMode by remember { mutableStateOf(reminder.soundMode) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تذكير ${reminder.category.labelAr}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("تفعيل التذكير اليومي")
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hour.toString(),
                        onValueChange = { hour = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 23) },
                        label = { Text("الساعة (٠-٢٣)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minute.toString(),
                        onValueChange = { minute = (it.filter(Char::isDigit).toIntOrNull() ?: 0).coerceIn(0, 59) },
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
        },
        confirmButton = {
            Button(onClick = {
                onSave(reminder.copy(enabled = enabled, hour = hour, minute = minute, soundMode = soundMode))
            }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun AdhkarEditorDialog(
    profileId: Long,
    category: AdhkarCategory,
    existing: AdhkarEntryEntity?,
    onDismiss: () -> Unit,
    onSave: (AdhkarEntryEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var text by remember { mutableStateOf(existing?.arabicText ?: "") }
    var source by remember { mutableStateOf(existing?.sourceReference ?: "إضافة شخصية موثقة") }
    var count by remember { mutableStateOf((existing?.targetCount ?: 1).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "إضافة ذكر جديد" else "تعديل الذكر") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("العنوان") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("نص الذكر") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("المرجع أو المصدر") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = count,
                    onValueChange = { count = it.filter(Char::isDigit) },
                    label = { Text("عدد التكرار") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && text.isNotBlank(),
                onClick = {
                    onSave(
                        (existing ?: AdhkarEntryEntity(
                            profileId = profileId,
                            category = category,
                            title = title.trim(),
                            arabicText = text.trim(),
                            targetCount = 1,
                            sourceReference = source.trim(),
                            isCustom = true
                        )).copy(
                            title = title.trim(),
                            arabicText = text.trim(),
                            sourceReference = source.trim(),
                            targetCount = (count.toIntOrNull() ?: 1).coerceAtLeast(1)
                        )
                    )
                }
            ) { Text("حفظ") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("إلغاء") } }
    )
}
