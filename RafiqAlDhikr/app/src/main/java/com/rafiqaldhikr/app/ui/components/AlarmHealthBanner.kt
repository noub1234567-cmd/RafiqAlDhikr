package com.rafiqaldhikr.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.rafiqaldhikr.app.core.health.AlarmHealth
import com.rafiqaldhikr.app.core.health.HealthIssue
import com.rafiqaldhikr.app.core.health.Onboarding
import com.rafiqaldhikr.app.core.permissions.PermissionHelper

/** Shown on the dashboard while something may stop reminders from arriving. Re-checks on every return to the app. */
@Composable
fun AlarmHealthBanner() {
    val context = LocalContext.current
    var issues by remember { mutableStateOf(emptyList<HealthIssue>()) }
    var batteryDismissed by remember { mutableStateOf(Onboarding.isBatteryHintDismissed(context)) }

    LifecycleResumeEffect(Unit) {
        issues = AlarmHealth.check(context)
        onPauseOrDispose { }
    }

    val shown = issues.filter { it.critical || !batteryDismissed }
    if (shown.isEmpty()) return

    val critical = shown.any { it.critical }
    RafiqCard(
        containerColor = if (critical) MaterialTheme.colorScheme.errorContainer
        else MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            if (critical) "قد لا تصلك التذكيرات" else "نصيحة لضمان وصول التذكيرات",
            style = MaterialTheme.typography.titleMedium
        )
        shown.forEach { issue ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(issue.titleAr, style = MaterialTheme.typography.titleSmall)
            Text(issue.detailAr, style = MaterialTheme.typography.bodyMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.Start)
            ) {
                OutlinedButton(onClick = {
                    PermissionHelper.safeStart(context, AlarmHealth.fixIntent(context, issue))
                }) { Text("إصلاح") }
                if (!issue.critical) {
                    TextButton(onClick = {
                        Onboarding.dismissBatteryHint(context)
                        batteryDismissed = true
                    }) { Text("لاحقاً") }
                }
            }
        }
    }
}
