package com.eon.futuresimulator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.eon.futuresimulator.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class EonNotificationChannel(val id: String, val label: String) {
    HABIT_REMINDER("eon_habit_reminders", "Habit reminders"),
    GOAL_DEADLINE("eon_goal_deadlines", "Goal deadlines"),
    SIMULATION_REVIEW("eon_simulation_review", "Simulation review"),
    RISK_ALERT("eon_risk_alerts", "Risk alerts"),
}

/**
 * Notification & Reminder System — one channel per category so the user can mute Habit
 * reminders without losing Risk alerts, etc. (Settings screen deep-links into channel
 * settings for exactly this.)
 */
@Singleton
class NotificationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        EonNotificationChannel.entries.forEach { channel ->
            manager.createNotificationChannel(
                NotificationChannel(channel.id, channel.label, NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
    }

    fun notify(channel: EonNotificationChannel, notificationId: Int, title: String, body: String) {
        ensureChannels()
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(notificationId, notification) }
    }
}
