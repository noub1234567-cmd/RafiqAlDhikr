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
import com.rafiqaldhikr.app.core.tasbeeh.TasbeehPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Home-screen counter: one tap adds one to the dhikr last used in the tasbeeh screen. */
class TasbeehWidgetProvider : AppWidgetProvider() {

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

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INCREMENT) {
            super.onReceive(context, intent)
            return
        }
        val app = context.applicationContext
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val container = (app as RafiqApplication).container
                val profile = container.profileRepository.getActiveProfileSnapshot()
                val type = profile?.let { resolveType(app, it.id) }
                if (profile != null && type != null) {
                    container.tasbeehRepository.increment(profile.id, type.id)
                }
                val manager = AppWidgetManager.getInstance(app)
                render(app, manager, manager.getAppWidgetIds(ComponentName(app, TasbeehWidgetProvider::class.java)))
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_INCREMENT = "com.rafiqaldhikr.app.ACTION_WIDGET_TASBEEH_INCREMENT"

        fun refresh(context: Context) {
            val app = context.applicationContext
            val manager = AppWidgetManager.getInstance(app)
            val ids = manager.getAppWidgetIds(ComponentName(app, TasbeehWidgetProvider::class.java))
            if (ids.isEmpty()) return
            CoroutineScope(Dispatchers.IO).launch { runCatching { render(app, manager, ids) } }
        }

        private suspend fun resolveType(context: Context, profileId: Long) =
            (context.applicationContext as RafiqApplication).container.database.tasbeehDao()
                .getTypesByProfile(profileId).let { types ->
                    val last = TasbeehPrefs.lastTypeId(context, profileId)
                    types.firstOrNull { it.id == last } ?: types.firstOrNull()
                }

        private suspend fun render(context: Context, manager: AppWidgetManager, ids: IntArray) {
            val views = RemoteViews(context.packageName, R.layout.widget_tasbeeh)
            var title = "المسبحة"
            var count = 0
            runCatching {
                val container = (context.applicationContext as RafiqApplication).container
                val profile = container.profileRepository.getActiveProfileSnapshot()
                val type = profile?.let { resolveType(context, it.id) }
                if (profile != null && type != null) {
                    title = type.title
                    count = container.tasbeehRepository.observeTodayLogs(profile.id).first()
                        .firstOrNull { it.tasbeehTypeId == type.id }?.count ?: 0
                }
            }
            views.setTextViewText(R.id.tasbeeh_title, title)
            views.setTextViewText(R.id.tasbeeh_count, count.toString())

            val plus = PendingIntent.getBroadcast(
                context, 1,
                Intent(context, TasbeehWidgetProvider::class.java).setAction(ACTION_INCREMENT),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_plus, plus)
            val open = PendingIntent.getActivity(
                context, 2,
                Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.tasbeeh_title, open)
            views.setOnClickPendingIntent(R.id.tasbeeh_count, open)
            manager.updateAppWidget(ids, views)
        }
    }
}
