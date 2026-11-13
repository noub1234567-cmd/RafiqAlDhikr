package com.rafiqaldhikr.app.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.model.ActivityStatus
import com.rafiqaldhikr.app.data.local.datastore.DashboardConfig
import com.rafiqaldhikr.app.data.local.db.entities.ActivityLogEntity
import com.rafiqaldhikr.app.ui.components.EmptyStateCard
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar
import com.rafiqaldhikr.app.ui.components.RescheduleReminderDialog
import com.rafiqaldhikr.app.ui.components.SectionHeader
import com.rafiqaldhikr.app.ui.components.StatusBadge
import com.rafiqaldhikr.app.ui.navigation.NavRoute

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenProfileSwitcher: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var reschedulingLog by remember { mutableStateOf<ActivityLogEntity?>(null) }
    var showCustomizeDialog by remember { mutableStateOf(false) }

    val completedLogs = state.todayLogs.filter { it.status == ActivityStatus.COMPLETED }
    val pendingOrMissed = state.todayLogs.filter { it.status != ActivityStatus.COMPLETED }

    Column(modifier = Modifier.fillMaxSize()) {
        RafiqTopBar(
            title = "رفيقك في الذكر",
            activeProfile = state.activeProfile,
            onProfileClick = onOpenProfileSwitcher,
            actions = {
                IconButton(onClick = { showCustomizeDialog = true }) {
                    Icon(Icons.Default.Tune, contentDescription = "تخصيص الرئيسية")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Welcome & Hijri/Gregorian Dates Card
            item {
                RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
                    Text(
                        text = "${state.greeting}، ${state.activeProfile?.name ?: ""}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = state.hijriDateAr,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = state.gregorianDateAr,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "التاريخ الهجري محسوب فلكياً ويمكن تعديله من الإعدادات حسب رؤية الهلال.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Daily Progress Summary
            if (state.config.showProgressSummary) {
                item {
                    val total = state.todayLogs.size.coerceAtLeast(1)
                    val done = completedLogs.size
                    val progress = (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                    RafiqCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("تقدّمك اليوم", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${ArabicFormatters.toArabicDigits(done)} من ${ArabicFormatters.toArabicDigits(state.todayLogs.size)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الأذكار: ${ArabicFormatters.toArabicDigits(state.adhkarDoneToday)}/${ArabicFormatters.toArabicDigits(state.adhkarTotalCount)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "القرآن: ${ArabicFormatters.toArabicDigits(state.quranDoneToday.toInt())}/${ArabicFormatters.toArabicDigits(state.quranTargetToday.toInt())} ${state.quranUnitAr}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Quick Access
            if (state.config.showQuickAccess) {
                item {
                    SectionHeader(title = "الوصول السريع")
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickAccessChip("الأذكار", Icons.Default.AutoAwesome) { onNavigate(NavRoute.Adhkar.route) }
                        QuickAccessChip("ورد القرآن", Icons.Default.MenuBook) { onNavigate(NavRoute.Quran.route) }
                        QuickAccessChip("المسبحة", Icons.Default.Fingerprint) { onNavigate(NavRoute.Tasbeeh.route) }
                        QuickAccessChip("الحفظ", Icons.Default.Book) { onNavigate(NavRoute.Memorization.route) }
                        QuickAccessChip("دفتر الدعاء", Icons.Default.VolunteerActivism) { onNavigate(NavRoute.Dua.route) }
                        QuickAccessChip("الصيام", Icons.Default.CalendarMonth) { onNavigate(NavRoute.Fasting.route) }
                        QuickAccessChip("السجل", Icons.Default.History) { onNavigate(NavRoute.History.route) }
                    }
                }
            }

            // Daily Worship Suggestion
            if (state.config.showDailySuggestion) {
                item {
                    RafiqCard(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)) {
                        Text(
                            text = "اقتراح عبادة لطيف لليوم",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.dailySuggestion,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Pending & Missed Activities (with Reschedule & Complete)
            if (state.config.showPendingMissed) {
                item {
                    SectionHeader(
                        title = "عبادات بانتظار الإنجاز (${ArabicFormatters.toArabicDigits(pendingOrMissed.size)})",
                        actionLabel = "إدارة العبادات",
                        onActionClick = { onNavigate(NavRoute.WorshipActs.route) }
                    )
                }
                if (pendingOrMissed.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.CheckCircle,
                            title = "بارك الله لك في وقتك",
                            description = "جميع العبادات المجدولة لهذا الوقت مكتملة أو لا توجد مهام متأخرة حالياً."
                        )
                    }
                } else {
                    items(pendingOrMissed, key = { "p_${it.id}" }) { log ->
                        TodayLogCard(
                            log = log,
                            onComplete = { viewModel.markCompleted(log) },
                            onUndo = { viewModel.revertLog(log) },
                            onReschedule = { reschedulingLog = log }
                        )
                    }
                }
            }

            // Completed today
            if (state.config.showTodayActivities && completedLogs.isNotEmpty()) {
                item {
                    SectionHeader(title = "أُنجز اليوم بحمد الله (${ArabicFormatters.toArabicDigits(completedLogs.size)})")
                }
                items(completedLogs, key = { "c_${it.id}" }) { log ->
                    TodayLogCard(
                        log = log,
                        onComplete = {},
                        onUndo = { viewModel.revertLog(log) },
                        onReschedule = {}
                    )
                }
            }

            // Upcoming reminders
            if (state.config.showUpcomingReminders && state.upcomingReminders.isNotEmpty()) {
                item {
                    SectionHeader(title = "التذكيرات القادمة")
                    RafiqCard {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.upcomingReminders.forEach { (act, triggerMillis) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(act.title, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(
                                        text = ArabicFormatters.formatInstantDateTime(triggerMillis),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Weekly progress
            if (state.config.showWeeklyProgress) {
                item {
                    SectionHeader(
                        title = "تقدّم الأهداف الأسبوعية",
                        actionLabel = "الإحصائيات",
                        onActionClick = { onNavigate(NavRoute.Statistics.route) }
                    )
                    RafiqCard {
                        val wkProgress = (state.weeklyCompleted.toFloat() / state.weeklyTarget.toFloat()).coerceIn(0f, 1f)
                        Text(
                            text = "أتممت ${ArabicFormatters.toArabicDigits(state.weeklyCompleted)} من أصل ${ArabicFormatters.toArabicDigits(state.weeklyTarget)} عبادات مجدولة خلال آخر ٧ أيام.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { wkProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    reschedulingLog?.let { log ->
        RescheduleReminderDialog(
            activityTitle = log.activityTitleSnapshot,
            onDismiss = { reschedulingLog = null },
            onConfirmTimeMillis = { newMillis ->
                viewModel.rescheduleLog(log, newMillis)
                reschedulingLog = null
            }
        )
    }

    if (showCustomizeDialog) {
        DashboardCustomizeDialog(
            config = state.config,
            onDismiss = { showCustomizeDialog = false },
            onSave = {
                viewModel.updateDashboardConfig(it)
                showCustomizeDialog = false
            }
        )
    }
}

@Composable
private fun QuickAccessChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TodayLogCard(
    log: ActivityLogEntity,
    onComplete: () -> Unit,
    onUndo: () -> Unit,
    onReschedule: () -> Unit
) {
    RafiqCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.activityTitleSnapshot, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = log.categorySnapshot.labelAr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = log.status)
        }
        Spacer(modifier = Modifier.height(10.dp))
        if (log.status == ActivityStatus.COMPLETED) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.completedAtMillis?.let { "أُنجز: ${ArabicFormatters.formatInstantDateTime(it)}" } ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onUndo) { Text("تراجع") }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onComplete, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تم الإنجاز")
                }
                OutlinedButton(onClick = onReschedule, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تذكير لاحقاً")
                }
            }
        }
    }
}

@Composable
private fun DashboardCustomizeDialog(
    config: DashboardConfig,
    onDismiss: () -> Unit,
    onSave: (DashboardConfig) -> Unit
) {
    var draft by remember { mutableStateOf(config) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تخصيص الشاشة الرئيسية") },
        text = {
            Column {
                ToggleRow("ملخص التقدّم اليومي", draft.showProgressSummary) {
                    draft = draft.copy(showProgressSummary = it)
                }
                ToggleRow("اختصارات الوصول السريع", draft.showQuickAccess) {
                    draft = draft.copy(showQuickAccess = it)
                }
                ToggleRow("اقتراح العبادة اليومي", draft.showDailySuggestion) {
                    draft = draft.copy(showDailySuggestion = it)
                }
                ToggleRow("العبادات المعلّقة والفائتة", draft.showPendingMissed) {
                    draft = draft.copy(showPendingMissed = it)
                }
                ToggleRow("العبادات المكتملة اليوم", draft.showTodayActivities) {
                    draft = draft.copy(showTodayActivities = it)
                }
                ToggleRow("التذكيرات القادمة", draft.showUpcomingReminders) {
                    draft = draft.copy(showUpcomingReminders = it)
                }
                ToggleRow("التقدّم الأسبوعي", draft.showWeeklyProgress) {
                    draft = draft.copy(showWeeklyProgress = it)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(draft) }) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}
