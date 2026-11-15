package com.rafiqaldhikr.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.rafiqaldhikr.app.MainActivity
import com.rafiqaldhikr.app.R
import com.rafiqaldhikr.app.RafiqApplication
import com.rafiqaldhikr.app.core.prayer.Prayer
import com.rafiqaldhikr.app.core.prayer.PrayerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Home-screen widget: next prayer and today's adhkar progress. Tapping it opens the app. */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                render(context.applicationContext, manager, ids)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        /** Re-draws every placed widget (call after the data changes). */
        fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, TodayWidgetProvider::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch {
                runCatching { render(app, manager, ids) }
            }
        }

        private suspend fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_today)

            views.setTextViewText(R.id.widget_prayer, nextPrayerText(context))

            var done = 0
            var total = 0
            runCatching {
                val app = context.applicationContext as RafiqApplication
                val profile = app.container.profileRepository.getActiveProfileSnapshot()
                if (profile != null) {
                    val dao = app.container.database.adhkarDao()
                    val entryIds = dao.getEntriesByProfile(profile.id).map { it.id }.toSet()
                    val progress = dao.observeProgressForDay(profile.id, LocalDate.now().toEpochDay()).first()
                    total = entryIds.size
                    done = progress.count { it.isCompleted && it.adhkarEntryId in entryIds }
                }
            }
            views.setTextViewText(
                R.id.widget_adhkar,
                if (total == 0) "الأذكار: لا توجد بيانات بعد" else "أذكار اليوم: $done من $total"
            )
            views.setProgressBar(R.id.widget_progress, 100, if (total == 0) 0 else done * 100 / total, false)

            val open = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, open)
            manager.updateAppWidget(ids, views)
        }

        private fun nextPrayerText(context: Context): String {
            val city = PrayerSettings.city(context)
            val now = java.time.ZonedDateTime.now(java.time.ZoneId.of(city.zoneId))
            val order = listOf(Prayer.FAJR, Prayer.DHUHR, Prayer.ASR, Prayer.MAGHRIB, Prayer.ISHA)
            for (offset in 0L..1L) {
                val times = PrayerSettings.timesFor(context, now.toLocalDate().plusDays(offset)) ?: continue
                val next = order.map { it to times.getValue(it) }.firstOrNull { it.second.isAfter(now) }
                if (next != null) {
                    val t = next.second
                    return "الصلاة القادمة: ${next.first.labelAr} %02d:%02d".format(t.hour, t.minute)
                }
            }
            return "مواقيت الصلاة غير متاحة"
        }
    }
}
