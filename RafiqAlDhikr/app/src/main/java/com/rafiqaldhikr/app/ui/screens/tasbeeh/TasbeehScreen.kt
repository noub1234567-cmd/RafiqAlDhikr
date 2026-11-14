package com.rafiqaldhikr.app.ui.screens.tasbeeh

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.data.local.db.entities.TasbeehTypeEntity
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.SectionHeader
import java.time.LocalDate

@Composable
fun TasbeehScreen(
    viewModel: TasbeehViewModel,
    onBack: (() -> Unit)? = null,
    onOpenProfileSwitcher: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current
    var showResetConfirm by remember { mutableStateOf(false) }
    var showAddTypeDialog by remember { mutableStateOf(false) }

    val selectedType = state.types.firstOrNull { it.id == state.selectedTypeId }
    val currentCount = selectedType?.let { state.todayCounts[it.id] ?: 0 } ?: 0
    val target = (selectedType?.dailyTarget ?: 100).coerceAtLeast(1)
    val progress = (currentCount.toFloat() / target).coerceIn(0f, 1f)
    val todayTotalAll = state.todayCounts.values.sum()

    Scaffold(
        topBar = {
            RafiqTopBar(
                title = "المسبحة والاستغفار",
                activeProfile = if (onBack == null) state.activeProfile else null,
                onProfileClick = if (onBack == null) onOpenProfileSwitcher else null,
                onBackClick = onBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTypeDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة ذكر مخصص")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.types, key = { it.id }) { type ->
                        FilterChip(
                            selected = type.id == selectedType?.id,
                            onClick = { viewModel.selectType(type.id) },
                            label = { Text(type.title) }
                        )
                    }
                }
            }

            if (selectedType != null) {
                item {
                    RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = selectedType.phraseArabic,
                                style = MaterialTheme.typography.headlineMedium,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "الهدف اليومي: ${ArabicFormatters.toArabicDigits(target)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        if (state.hapticEnabled) {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                        viewModel.incrementSelected()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.size(210.dp),
                                    strokeWidth = 10.dp
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(176.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = ArabicFormatters.toArabicDigits(currentCount),
                                            style = MaterialTheme.typography.headlineLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            text = "اضغط للتسبيح",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(onClick = { showResetConfirm = true }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text("تصفير عداد اليوم")
                                }
                                if (!selectedType.isBuiltIn) {
                                    IconButton(onClick = { viewModel.deleteType(selectedType) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف الذكر المخصص")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                RafiqCard {
                    Text(
                        text = "مجموع الأذكار اليوم: ${ArabicFormatters.toArabicDigits(todayTotalAll)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { SectionHeader(title = "سجل الأيام السابقة") }

            val byDate = state.historyLogs.groupBy { it.dateEpochDay }.entries.sortedByDescending { it.key }.take(14)
            items(byDate.toList(), key = { it.key }) { (epochDay, dayLogs) ->
                val total = dayLogs.sumOf { it.count }
                RafiqCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = ArabicFormatters.formatGregorianShort(LocalDate.ofEpochDay(epochDay)),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${ArabicFormatters.toArabicDigits(total)} تسبيحة",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            title = { Text("تأكيد التصفير") },
            text = { Text("هل تريد تصفير عداد اليوم لـ «${selectedType?.title ?: ""}»؟") },
            confirmButton = {
                Button(onClick = {
                    viewModel.resetSelectedToday()
                    showResetConfirm = false
                }) { Text("نعم، تصفير") }
            },
            dismissButton = { TextButton(onClick = { showResetConfirm = false }) { Text("إلغاء") } }
        )
    }

    val dialogProfileId = state.activeProfile?.id
    if (dialogProfileId != null && (showAddTypeDialog)) {
        val profileId: Long = dialogProfileId
        var title by remember { mutableStateOf("") }
        var phrase by remember { mutableStateOf("") }
        var targetText by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { showAddTypeDialog = false },
            title = { Text("إضافة ذكر مخصص للمسبحة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("اسم مختصر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phrase,
                        onValueChange = { phrase = it },
                        label = { Text("نص الذكر") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = targetText,
                        onValueChange = { targetText = it.filter(Char::isDigit) },
                        label = { Text("الهدف اليومي") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    enabled = title.isNotBlank() && phrase.isNotBlank(),
                    onClick = {
                        viewModel.saveCustomType(
                            TasbeehTypeEntity(
                                profileId = profileId,
                                title = title.trim(),
                                phraseArabic = phrase.trim(),
                                dailyTarget = (targetText.toIntOrNull() ?: 100).coerceAtLeast(1),
                                isBuiltIn = false,
                                sortOrder = state.types.size
                            )
                        )
                        showAddTypeDialog = false
                    }
                ) { Text("إضافة") }
            },
            dismissButton = { TextButton(onClick = { showAddTypeDialog = false }) { Text("إلغاء") } }
        )
    }
}
