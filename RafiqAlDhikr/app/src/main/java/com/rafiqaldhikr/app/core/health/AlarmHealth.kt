package com.rafiqaldhikr.app.core.health

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.rafiqaldhikr.app.core.permissions.PermissionHelper
import com.rafiqaldhikr.app.notifications.AlarmState
import com.rafiqaldhikr.app.notifications.NotificationHelper

/** Things that can silently stop reminders from reaching the user. */
enum class HealthIssue(val titleAr: String, val detailAr: String, val critical: Boolean) {
    NOTIFICATIONS_OFF(
        "الإشعارات معطّلة",
        "لن يصلك أي تذكير. فعّل إشعارات التطبيق من إعدادات الهاتف.",
        true
    ),
    CHANNEL_BLOCKED(
        "قناة التذكيرات محظورة",
        "حظرتَ قناة التذكيرات في إعدادات الإشعارات، فتصل بلا صوت أو لا تظهر.",
        true
    ),
    EXACT_ALARMS_OFF(
        "المنبّهات الدقيقة غير مسموحة",
        "قد تتأخر التذكيرات عن وقتها. اسمح للتطبيق بالمنبّهات والتذكيرات.",
        true
    ),
    BATTERY_OPTIMIZED(
        "تحسين البطارية قد يؤخّر التذكيرات",
        "اسمح للتطبيق بالعمل دون قيود البطارية ليصلك التذكير في وقته.",
        false
    )
}

object AlarmHealth {

    fun check(context: Context): List<HealthIssue> {
        val issues = mutableListOf<HealthIssue>()
        val status = PermissionHelper.getStatus(context)

        if (!status.notificationsGranted || !NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            issues += HealthIssue.NOTIFICATIONS_OFF
        } else if (anyChannelBlocked(context)) {
            issues += HealthIssue.CHANNEL_BLOCKED
        }
        // In "alarm clock" mode the exact-alarm permission is not needed.
        if (!status.exactAlarmAllowed && !AlarmState.useAlarmClock(context)) {
            issues += HealthIssue.EXACT_ALARMS_OFF
        }
        if (!status.ignoringBatteryOptimizations) {
            issues += HealthIssue.BATTERY_OPTIMIZED
        }
        return issues
    }

    private fun anyChannelBlocked(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return listOf(NotificationHelper.CHANNEL_NORMAL, NotificationHelper.CHANNEL_GENTLE).any {
            nm.getNotificationChannel(it)?.importance == NotificationManager.IMPORTANCE_NONE
        }
    }

    /** The settings screen that fixes [issue]. */
    fun fixIntent(context: Context, issue: HealthIssue): Intent = when (issue) {
        HealthIssue.NOTIFICATIONS_OFF, HealthIssue.CHANNEL_BLOCKED ->
            PermissionHelper.openNotificationSettingsIntent(context)
        HealthIssue.EXACT_ALARMS_OFF -> PermissionHelper.openExactAlarmSettingsIntent(context)
        HealthIssue.BATTERY_OPTIMIZED -> PermissionHelper.openBatteryOptimizationSettingsIntent(context)
    }
}
