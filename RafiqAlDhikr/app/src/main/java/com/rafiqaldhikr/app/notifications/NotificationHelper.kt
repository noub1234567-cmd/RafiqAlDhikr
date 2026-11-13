package com.rafiqaldhikr.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rafiqaldhikr.app.MainActivity
import com.rafiqaldhikr.app.R
import com.rafiqaldhikr.app.core.model.NotificationSoundMode

object NotificationHelper {

    const val CHANNEL_NORMAL = "rafiq_reminders_normal"
    const val CHANNEL_GENTLE = "rafiq_reminders_gentle"

    const val ACTION_COMPLETE = "com.rafiqaldhikr.app.ACTION_COMPLETE"
    const val ACTION_SNOOZE_15 = "com.rafiqaldhikr.app.ACTION_SNOOZE_15"
    const val ACTION_REMIND_60 = "com.rafiqaldhikr.app.ACTION_REMIND_60"

    const val EXTRA_LOG_ID = "extra_log_id"
    const val EXTRA_ACTIVITY_ID = "extra_activity_id"
    const val EXTRA_PROFILE_ID = "extra_profile_id"
    const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_BODY = "extra_body"
    const val EXTRA_SOUND_MODE = "extra_sound_mode"
    const val EXTRA_KIND = "extra_kind"
    const val EXTRA_TARGET_ID = "extra_target_id"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val normalChannel = NotificationChannel(
            CHANNEL_NORMAL,
            context.getString(R.string.notification_channel_general_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_general_desc)
            enableVibration(true)
        }

        val gentleUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val gentleChannel = NotificationChannel(
            CHANNEL_GENTLE,
            context.getString(R.string.notification_channel_gentle_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.notification_channel_gentle_desc)
            setSound(
                gentleUri,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            enableVibration(false)
        }

        manager.createNotificationChannel(normalChannel)
        manager.createNotificationChannel(gentleChannel)
    }

    fun showReminderNotification(
        context: Context,
        notificationId: Int,
        title: String,
        body: String,
        soundMode: NotificationSoundMode,
        activityId: Long = -1L,
        logId: Long = -1L,
        profileId: Long = -1L
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_PROFILE_ID, profileId)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (soundMode == NotificationSoundMode.GENTLE_SOUND) {
            CHANNEL_GENTLE
        } else {
            CHANNEL_NORMAL
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Snooze 15 min action (never marks completed!)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_15
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            putExtra(EXTRA_PROFILE_ID, profileId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_SOUND_MODE, soundMode.name)
        }
        val snoozePending = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 1,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(0, "غفوة ١٥ د", snoozePending)

        // Remind later 1 hour action (never marks completed!)
        val remindLaterIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_REMIND_60
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_LOG_ID, logId)
            putExtra(EXTRA_ACTIVITY_ID, activityId)
            putExtra(EXTRA_PROFILE_ID, profileId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_SOUND_MODE, soundMode.name)
        }
        val remindLaterPending = PendingIntent.getBroadcast(
            context,
            notificationId * 10 + 2,
            remindLaterIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(0, "تذكير بعد ساعة", remindLaterPending)

        if (activityId > 0L && logId > 0L) {
            val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_COMPLETE
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(EXTRA_LOG_ID, logId)
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_PROFILE_ID, profileId)
            }
            val completePending = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 3,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(0, "تم الإنجاز", completePending)
        }

        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}
