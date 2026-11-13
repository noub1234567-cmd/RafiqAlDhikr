package com.rafiqaldhikr.app.ui.screens.worship

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.data.local.db.entities.WorshipActivityEntity
import com.rafiqaldhikr.app.ui.components.ActivityEditorDialog
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.SectionHeader

@Composable
fun WorshipActsScreen(
    viewModel: WorshipActsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingActivity by remember { mutableStateOf<WorshipActivityEntity?>(null) }

    val builtIn = state.activities.filter { it.isOptionalBuiltIn }
    val custom = state.activities.filter { !it.isOptionalBuiltIn }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "العبادات والنوافل والأهداف",
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عبادة مخصصة")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(
                    title = "عبادات وأهداف مخصصة (${ArabicFormatters.toArabicDigits(custom.size)})",
                    subtitle = "أضف أي عبادة أو هدف يومي أو أسبوعي يناسب جدولك."
                )
            }

            if (custom.isEmpty()) {
                item {
                    RafiqCard {
                        Text(
                            text = "لم تضف عبادات مخصصة بعد. اضغط على زر (+) لإضافة عبادة أو هدف جديد بتذكير مخصص.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(custom, key = { it.id }) { act ->
                    ActivityItemCard(
                        activity = act,
                        onToggle = { viewModel.toggleEnabled(act, it) },
                        onEdit = { editingActivity = act },
                        onDelete = { viewModel.deleteActivity(act) },
                        onCompleteToday = { viewModel.markCompletedToday(act) }
                    )
                }
            }

            item {
                SectionHeader(
                    title = "نوافل وسنن اختيارية",
                    subtitle = "فعّل فقط ما يناسبك وعدّل أوقاتها بحرية."
                )
            }

            items(builtIn, key = { it.id }) { act ->
                ActivityItemCard(
                    activity = act,
                    onToggle = { viewModel.toggleEnabled(act, it) },
                    onEdit = { editingActivity = act },
                    onDelete = null,
                    onCompleteToday = { viewModel.markCompletedToday(act) }
                )
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }

    if (showCreateDialog || editingActivity != null) {
        val profileId = state.activeProfile?.id ?: 1L
        ActivityEditorDialog(
            profileId = profileId,
            existing = editingActivity,
            onDismiss = {
                showCreateDialog = false
                editingActivity = null
            },
            onSave = {
                viewModel.saveActivity(it)
                showCreateDialog = false
                editingActivity = null
            }
        )
    }
}

@Composable
private fun ActivityItemCard(
    activity: WorshipActivityEntity,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
    onCompleteToday: () -> Unit
) {
    RafiqCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${activity.category.labelAr} • ${activity.goalPeriod.labelAr} • ${activity.scheduleType.labelAr}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Switch(checked = activity.isEnabled, onCheckedChange = onToggle)
        }
        if (activity.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (activity.reminderEnabled) {
                "التذكير: ${ArabicFormatters.formatTime12Ar(activity.reminderHour, activity.reminderMinute)} (${activity.soundMode.labelAr})"
            } else {
                "التذكير: غير مفعّل"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = onCompleteToday, enabled = activity.isEnabled) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إنجاز اليوم")
            }
            OutlinedButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("تعديل")
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف")
                }
            }
        }
    }
}
