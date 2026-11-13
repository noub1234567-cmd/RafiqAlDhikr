package com.rafiqaldhikr.app.ui.screens.history

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.ui.components.EmptyStateCard
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.RescheduleReminderDialog
import com.rafiqaldhikr.app.ui.components.StatusBadge

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var rescheduleTarget by remember { mutableStateOf<ActivityLogEntity?>(null) }

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "سجل العبادات المنجزة والفائتة",
                onBackClick = onBack
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedStatus == null,
                        onClick = { viewModel.selectFilter(null) },
                        label = { Text("الكل") }
                    )
                }
                items(ActivityStatus.entries) { status ->
                    FilterChip(
                        selected = state.selectedStatus == status,
                        onClick = { viewModel.selectFilter(status) },
                        label = { Text(status.labelAr) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.logs.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.History,
                            title = "لا توجد سجلات مطابقة",
                            description = "ستظهر هنا العبادات المكتملة والفائتة والمُعاد جدولتها مع أوقات التذكير والإنجاز."
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
                                    Text(log.activityTitleSnapshot, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "موعد التذكير: ${ArabicFormatters.formatInstantDateTime(log.scheduledAtMillis)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    log.completedAtMillis?.let { doneAt ->
                                        Text(
                                            text = "تاريخ الإتمام: ${ArabicFormatters.formatInstantDateTime(doneAt)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (log.status == ActivityStatus.RESCHEDULED && log.rescheduledToMillis != null) {
                                        Text(
                                            text = "أُعيد جدولته إلى: ${ArabicFormatters.formatInstantDateTime(log.rescheduledToMillis)} (لم يُنجز بعد)",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                StatusBadge(status = log.status)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (log.status == ActivityStatus.COMPLETED) {
                                        TextButton(onClick = { viewModel.undoCompletion(log) }) {
                                            Text("تراجع عن الإتمام")
                                        }
                                    } else {
                                        Button(onClick = { viewModel.markCompleted(log) }) {
                                            Text("تم الإنجاز")
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        OutlinedButton(onClick = { rescheduleTarget = log }) {
                                            Text("تذكير لاحقاً")
                                        }
                                    }
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
    }

    rescheduleTarget?.let { log ->
        RescheduleReminderDialog(
            activityTitle = log.activityTitleSnapshot,
            onDismiss = { rescheduleTarget = null },
            onConfirmTimeMillis = { newMillis ->
                viewModel.reschedule(log, newMillis)
                rescheduleTarget = null
            }
        )
    }
}
