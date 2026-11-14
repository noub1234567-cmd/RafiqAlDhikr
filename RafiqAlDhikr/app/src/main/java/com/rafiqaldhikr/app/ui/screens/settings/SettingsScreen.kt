package com.rafiqaldhikr.app.ui.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rafiqaldhikr.app.core.calendar.ArabicFormatters
import com.rafiqaldhikr.app.core.model.ThemeMode
import com.rafiqaldhikr.app.core.permissions.PermissionHelper
import com.rafiqaldhikr.app.ui.components.RafiqCard
import com.rafiqaldhikr.app.ui.components.RafiqTopBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permStatus by remember { mutableStateOf(PermissionHelper.getStatus(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permStatus = PermissionHelper.getStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permStatus = PermissionHelper.getStatus(context)
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) viewModel.exportBackup(uri)
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) viewModel.inspectBackupForRestore(uri)
    }

    Scaffold(
        topBar = {
            RafiqTopBar(title = "الإعدادات والنسخ الاحتياطي", onBackClick = onBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            state.statusMessage?.let { msg ->
                item {
                    RafiqCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(msg, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { viewModel.clearMessage() }) { Text("إخفاء") }
                    }
                }
            }

            // Theme
            item {
                RafiqCard {
                    Text("المظهر", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                label = { Text(mode.labelAr) }
                            )
                        }
                    }
                }
            }

            // Hijri offset & Haptic
            item {
                RafiqCard {
                    Text("التقويم الهجري والمسبحة", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ضبط التقويم الهجري: ${ArabicFormatters.toArabicDigits(state.settings.hijriOffsetDays)} يوم",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (-2..2).forEach { off ->
                            FilterChip(
                                selected = state.settings.hijriOffsetDays == off,
                                onClick = { viewModel.setHijriOffset(off) },
                                label = { Text(ArabicFormatters.toArabicDigits(off)) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("اهتزاز لطيف عند التسبيح")
                        Switch(
                            checked = state.settings.tasbeehHapticEnabled,
                            onCheckedChange = { viewModel.setTasbeehHaptic(it) }
                        )
                    }
                }
            }

            // Notifications & Battery Optimization
            item {
                RafiqCard {
                    Text("صلاحيات الإشعارات والبطارية", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• إذن الإشعارات: ${if (permStatus.notificationsGranted) "مفعّل" else "غير مفعّل"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• جدولة المنبهات الدقيقة: ${if (permStatus.exactAlarmAllowed) "مسموح" else "مقيّد (سيستخدم النظام جدولة تقريبية)"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "• تحسين البطارية: ${if (permStatus.ignoringBatteryOptimizations) "مستثنى" else "خاضع لقيود النظام"}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ملاحظة تقنية: قد تفرض بعض الشركات المصنّعة للهواتف قيوداً إضافية على التطبيقات في الخلفية؛ لذا يُنصح باستثناء التطبيق من توفير البطارية عند الرغبة في دقة التنبيهات، دون أن يضمن نظام أندرويد ذلك في جميع الأجهزة.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!permStatus.notificationsGranted) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        PermissionHelper.safeStart(context, PermissionHelper.openNotificationSettingsIntent(context))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("منح إذن الإشعارات") }
                        }
                        OutlinedButton(
                            onClick = { PermissionHelper.safeStart(context, PermissionHelper.openNotificationSettingsIntent(context)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("إعدادات إشعارات التطبيق") }
                        if (!permStatus.exactAlarmAllowed) {
                            OutlinedButton(
                                onClick = { PermissionHelper.safeStart(context, PermissionHelper.openExactAlarmSettingsIntent(context)) },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("السماح بالتنبيهات الدقيقة") }
                        }
                        OutlinedButton(
                            onClick = { PermissionHelper.safeStart(context, PermissionHelper.openBatteryOptimizationSettingsIntent(context)) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("إعدادات تحسين البطارية") }
                    }
                }
            }

            // Local Backup & Restore
            item {
                RafiqCard {
                    Text("النسخ الاحتياطي والاستعادة المحلية", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "احفظ نسخة احتياطية من جميع الملفات الشخصية والأذكار والأدعية والسجلات في ملف JSON محلي على جهازك دون أي خوادم خارجية.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { exportLauncher.launch("rafiq_al_dhikr_backup.json") },
                            modifier = Modifier.weight(1f)
                        ) { Text("تصدير نسخة") }
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                            modifier = Modifier.weight(1f)
                        ) { Text("استعادة نسخة") }
                    }
                }
            }

            // Privacy statement
            item {
                RafiqCard {
                    Text("الخصوصية والعمل دون إنترنت", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "التطبيق يعمل محلياً بالكامل ولا يطلب إذن الإنترنت أو الموقع أو الكاميرا أو الميكروفون، ولا يرسل أي بيانات خارج جهازك.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    state.pendingRestoreBackup?.let { backup ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRestoreConfirmation() },
            title = { Text("تأكيد استعادة النسخة الاحتياطية") },
            text = {
                Text(
                    "تم التحقق من سلامة الملف.\n" +
                        "• عدد الملفات الشخصية: ${ArabicFormatters.toArabicDigits(backup.payload.profiles.size)}\n" +
                        "• تاريخ التصدير: ${ArabicFormatters.formatInstantDateTime(backup.exportedAtMillis)}\n\n" +
                        "تنبيه: ستستبدل هذه العملية البيانات الحالية على الجهاز ببيانات النسخة المختارة."
                )
            },
            confirmButton = {
                Button(onClick = { viewModel.confirmRestore() }) { Text("نعم، استعادة") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissRestoreConfirmation() }) { Text("إلغاء") }
            }
        )
    }
}
