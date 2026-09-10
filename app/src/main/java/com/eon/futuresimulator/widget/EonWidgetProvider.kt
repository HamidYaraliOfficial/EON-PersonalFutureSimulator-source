package com.eon.futuresimulator.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.eon.futuresimulator.MainActivity
import com.eon.futuresimulator.R

/**
 * Home Screen Widget — Goal Progress, Streak and a "What If?" quick action that deep
 * links straight into the Scenario Builder. Data shown here is refreshed by
 * WidgetUpdateHelper (called after any Goal/Habit change and periodically via the
 * widget's own updatePeriodMillis).
 */
class EonWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { id -> updateWidget(context, appWidgetManager, id) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WHAT_IF) {
            val launch = Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_DEEP_LINK, MainActivity.DEEP_LINK_WHAT_IF)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launch)
        }
    }

    private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_eon)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPending = PendingIntent.getActivity(
            context, widgetId, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_goal_title, openAppPending)

        val whatIfIntent = Intent(context, EonWidgetProvider::class.java).apply { action = ACTION_WHAT_IF }
        val whatIfPending = PendingIntent.getBroadcast(
            context, widgetId, whatIfIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        views.setOnClickPendingIntent(R.id.widget_what_if, whatIfPending)

        manager.updateAppWidget(widgetId, views)
    }

    companion object {
        const val ACTION_WHAT_IF = "com.eon.futuresimulator.widget.ACTION_WHAT_IF"

        /** Call from anywhere data changes (e.g. after a habit check-in) to refresh all widget instances. */
        fun requestUpdate(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(android.content.ComponentName(context, EonWidgetProvider::class.java))
            val intent = Intent(context, EonWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
