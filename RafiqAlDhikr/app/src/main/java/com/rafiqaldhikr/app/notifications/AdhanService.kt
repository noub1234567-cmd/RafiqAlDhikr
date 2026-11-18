package com.rafiqaldhikr.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.rafiqaldhikr.app.R
import com.rafiqaldhikr.app.core.prayer.PrayerSettings

/** Plays the adhan recording once, on the alarm volume, as a short foreground service with a stop button. */
class AdhanService : Service() {
    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        startInForeground()
        if (!startPlayback()) stopSelf()
        return START_NOT_STICKY
    }

    private fun startInForeground() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PLAYING, "تشغيل الأذان", NotificationManager.IMPORTANCE_LOW).apply {
                setSound(null, null)
                enableVibration(false)
            }
        )
        val stop = PendingIntent.getService(
            this, 0, Intent(this, AdhanService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_PLAYING)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("الأذان")
            .setContentText("يُرفع الأذان الآن")
            .setOngoing(true)
            .setSilent(true)
            .addAction(0, "إيقاف", stop)
            .build()
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
    }

    private fun startPlayback(): Boolean = runCatching {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs).build()
        focusRequest = request
        audio.requestAudioFocus(request)
        val mp = MediaPlayer()
        player = mp
        resources.openRawResourceFd(R.raw.adhan).use { afd ->
            mp.setAudioAttributes(attrs)
            mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        }
        mp.setOnCompletionListener { stopSelf() }
        mp.setOnErrorListener { _, _, _ -> stopSelf(); true }
        mp.prepare()
        mp.start()
        true
    }.getOrDefault(false)

    override fun onDestroy() {
        runCatching { player?.release() }
        player = null
        focusRequest?.let { runCatching { (getSystemService(Context.AUDIO_SERVICE) as AudioManager).abandonAudioFocusRequest(it) } }
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.rafiqaldhikr.app.ACTION_STOP_ADHAN"
        private const val CHANNEL_PLAYING = "rafiq_adhan_playing"
        private const val NOTIFICATION_ID = -2001

        /** Starts the adhan; false if the system refused to start the service. */
        fun start(context: Context): Boolean = runCatching {
            ContextCompat.startForegroundService(context, Intent(context, AdhanService::class.java))
            true
        }.getOrDefault(false)

        /**
         * Whether the adhan may sound now: switched on, the reminder fires at the prayer time (not ahead of it),
         * and the phone is neither silent/vibrating nor in do-not-disturb.
         */
        fun shouldPlay(context: Context): Boolean {
            if (!PrayerSettings.adhanSoundEnabled(context)) return false
            if (PrayerSettings.adhanLeadMinutes(context) != 0) return false
            val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return false
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL ||
                nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        }
    }
}
