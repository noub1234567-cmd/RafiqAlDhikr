package com.rafiqaldhikr.app.ui.screens.seasons

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.AlertDialog
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
import com.rafiqaldhikr.app.core.model.SeasonalCatalog
import com.rafiqaldhikr.app.core.model.SeasonalWorshipItem
import com.rafiqaldhikr.app.data.local.db.entities.SeasonalSelectionEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

@Composable
fun SeasonsScreen(
    viewModel: SeasonsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val profileId = state.activeProfile?.id ?: 1L
    var reminderTarget by remember { mutableStateOf<Pair<SeasonalWorshipItem, SeasonalSelectionEntity>?>(null) }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "المواسم والنفحات الإسلامية",
                onBackClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(SeasonalCatalog.seasons, key = { it.id }) { season ->
                val sel = state.selectionsById[season.id] ?: SeasonalSelectionEntity(
                    profileId = profileId,
                    seasonId = season.id
                )
                RafiqCard(
                    containerColor = if (sel.isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLow
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(season.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = season.timeDescription,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Switch(
                            checked = sel.isSelected,
                            onCheckedChange = { checked ->
                                viewModel.saveSelection(sel.copy(isSelected = checked))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(season.guidanceText, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    season.suggestedActivities.forEach { act ->
                        Text(
                            text = "• $act",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (sel.isSelected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (sel.reminderEnabled) {
                                    "التذكير: ${ArabicFormatters.formatTime12Ar(sel.reminderHour, sel.reminderMinute)}"
                                } else {
                                    "التذكير غير مفعّل"
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                            OutlinedButton(onClick = { reminderTarget = season to sel }) {
                                Text("ضبط التذكير")
                            }
                        }
                    }
                }
            }
        }
    }

    reminderTarget?.let { (season, sel) ->
        var enabled by remember { mutableStateOf(sel.reminderEnabled) }
        var hour by remember { mutableIntStateOf(sel.reminderHour) }
        var minute by remember { mutableIntStateOf(sel.reminderMinute) }

        AlertDialog(
            onDismissRequest = { reminderTarget = null },
            title = { Text("تذكير: ${season.title}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل التذكير اليومي للموسم")
                        Switch(checked = enabled, onCheckedChange = { enabled = it })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = hour.toString(),
                            onValueChange = { hour = (it.filter(Char::isDigit).toIntOrNull() ?: 9).coerceIn(0, 23) },
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
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveSelection(sel.copy(reminderEnabled = enabled, reminderHour = hour, reminderMinute = minute))
                    reminderTarget = null
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { reminderTarget = null }) { Text("إلغاء") } }
        )
    }
}
