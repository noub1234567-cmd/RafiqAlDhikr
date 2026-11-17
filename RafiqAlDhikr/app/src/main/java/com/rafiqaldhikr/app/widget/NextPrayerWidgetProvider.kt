package com.rafiqaldhikr.app.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.SystemClock
import android.widget.RemoteViews
import com.rafiqaldhikr.app.MainActivity
import com.rafiqaldhikr.app.R
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import java.time.Instant
import java.time.ZoneId

/**
 * Home-screen widget with the next prayer, a live countdown (a system Chronometer, so it ticks without wakeups)
 * and the five times of the day. It redraws itself just after every prayer time.
 */
class NextPrayerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        render(context.applicationContext, manager, ids)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> refresh(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.rafiqaldhikr.app.NEXT_PRAYER_REFRESH"
        private const val REFRESH_REQUEST_CODE = 7421

        fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, NextPrayerWidgetProvider::class.java))
            if (ids.isEmpty()) return
            render(app, manager, ids)
        }

        private fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_next_prayer)
            val now = System.currentTimeMillis()
            val next = PrayerSettings.nextPrayer(context, now)

            if (next == null) {
                views.setTextViewText(R.id.np_name, "مواقيت الصلاة غير متاحة")
                views.setTextViewText(R.id.np_time, "")
                views.setChronometer(R.id.np_countdown, SystemClock.elapsedRealtime(), null, false)
            } else {
                val (prayer, at) = next
                val zone = ZoneId.of(PrayerSettings.city(context).zoneId)
                val time = Instant.ofEpochMilli(at).atZone(zone)
                views.setTextViewText(R.id.np_name, prayer.labelAr)
                views.setTextViewText(R.id.np_time, "%02d:%02d".format(time.hour, time.minute))
                views.setChronometerCountDown(R.id.np_countdown, true)
                views.setChronometer(R.id.np_countdown, SystemClock.elapsedRealtime() + (at - now), null, true)
                scheduleRefresh(context, at + 1_000)

                val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
                PrayerSettings.timesFor(context, today)?.let { times ->
                    val cells = listOf(
                        Prayer.FAJR to R.id.np_fajr, Prayer.DHUHR to R.id.np_dhuhr, Prayer.ASR to R.id.np_asr,
                        Prayer.MAGHRIB to R.id.np_maghrib, Prayer.ISHA to R.id.np_isha
                    )
                    for ((p, id) in cells) {
                        val t = times.getValue(p)
                        views.setTextViewText(id, "${p.labelAr}\n%02d:%02d".format(t.hour, t.minute))
                        views.setTextColor(id, if (p == prayer) Color.parseColor("#0E4D3A") else Color.parseColor("#2B2B2B"))
                    }
                }
            }

            views.setOnClickPendingIntent(
                R.id.np_root,
                PendingIntent.getActivity(
                    context, 1,
                    Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
            manager.updateAppWidget(ids, views)
        }

        /** A non-exact alarm is enough here (no permission, may slip by a few minutes; the periodic update covers that). */
        private fun scheduleRefresh(context: Context, atMillis: Long) {
            val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NextPrayerWidgetProvider::class.java).setAction(ACTION_REFRESH)
            val pending = PendingIntent.getBroadcast(
                context, REFRESH_REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            runCatching { am.set(AlarmManager.RTC, atMillis, pending) }
        }
    }
}
