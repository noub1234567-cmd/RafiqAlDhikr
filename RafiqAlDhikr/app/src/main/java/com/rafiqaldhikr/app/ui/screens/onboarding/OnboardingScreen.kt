package com.rafiqaldhikr.app.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.rafiqaldhikr.app.core.health.Onboarding
import com.rafiqaldhikr.app.core.permissions.PermissionHelper
import com.rafiqaldhikr.app.core.permissions.PermissionStatusSnapshot
import com.rafiqaldhikr.app.notifications.AlarmState
import com.rafiqaldhikr.app.ui.components.RafiqCard

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    var status by remember { mutableStateOf(PermissionHelper.getStatus(context)) }

    LifecycleResumeEffect(Unit) {
        status = PermissionHelper.getStatus(context)
        onPauseOrDispose { }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        status = PermissionHelper.getStatus(context)
    }
    val (oemName, oemText) = remember { Onboarding.oemTips() }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("تهيئة رفيقك في الذكر", style = MaterialTheme.typography.headlineSmall)
            Text(
                "التذكيرات هي قلب التطبيق، وبعض الهواتف توقفها لتوفير البطارية. أربع خطوات قصيرة تضمن وصولها في وقتها.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StepCard(
                title = "١. الإشعارات",
                ok = status.notificationsGranted,
                text = "بدونها لن تصلك أي تذكيرات."
            ) {
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !status.notificationsGranted) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        PermissionHelper.safeStart(context, PermissionHelper.openNotificationSettingsIntent(context))
                    }
                }) { Text(if (status.notificationsGranted) "فتح إعدادات الإشعارات" else "السماح بالإشعارات") }
            }

            val exactNeeded = !AlarmState.useAlarmClock(context)
            StepCard(
                title = "٢. المنبّهات والتذكيرات الدقيقة",
                ok = status.exactAlarmAllowed,
                text = if (exactNeeded) "تضمن وصول التذكير في دقيقته." else
                    "اختياري: التطبيق يستخدم وضع «منبّه ساعة» الذي لا يحتاجها عادةً، لكن السماح بها أضمن."
            ) {
                OutlinedButton(onClick = {
                    PermissionHelper.safeStart(context, PermissionHelper.openExactAlarmSettingsIntent(context))
                }) { Text("فتح الإعداد") }
            }

            StepCard(
                title = "٣. البطارية",
                ok = status.ignoringBatteryOptimizations,
                text = "اسمح للتطبيق بالعمل دون قيود البطارية حتى لا يوقفه النظام في الخلفية."
            ) {
                OutlinedButton(onClick = {
                    PermissionHelper.safeStart(context, PermissionHelper.openBatteryOptimizationSettingsIntent(context))
                }) { Text("فتح إعداد البطارية") }
            }

            RafiqCard {
                Text("٤. خاص بـ $oemName", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(oemText, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "قد تختلف تسمية الخيارات قليلاً بين إصدارات النظام.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = {
                    PermissionHelper.safeStart(context, PermissionHelper.openAppDetailsIntent(context))
                }) { Text("فتح إعدادات التطبيق") }
            }

            Button(
                onClick = {
                    Onboarding.setDone(context, true)
                    onFinish()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("ابدأ") }
            Text(
                "يمكنك العودة إلى هذا الدليل من الإعدادات في أي وقت، وسينبّهك التطبيق في الشاشة الرئيسية إن تعطّل شيء.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StepCard(
    title: String,
    ok: Boolean,
    text: String,
    action: @Composable () -> Unit
) {
    RafiqCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                if (ok) "✓ جاهز" else "⚠ يحتاج إجراء",
                style = MaterialTheme.typography.labelLarge,
                color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        action()
    }
}
