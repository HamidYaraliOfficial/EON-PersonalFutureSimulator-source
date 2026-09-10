package com.eon.futuresimulator.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eon.futuresimulator.domain.repository.HabitRepository
import com.eon.futuresimulator.notifications.EonNotificationChannel
import com.eon.futuresimulator.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDateTime
import java.time.ZoneId

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val habitRepository: HabitRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val kind = inputData.getString(KEY_KIND) ?: KIND_HABIT
        val entityId = inputData.getString(KEY_HABIT_ID) ?: return Result.failure()

        when (kind) {
            KIND_HABIT -> {
                val habit = habitRepository.getAllHabitsOnce().firstOrNull { it.id == entityId } ?: return Result.success()
                notificationHelper.notify(
                    EonNotificationChannel.HABIT_REMINDER, entityId.hashCode(),
                    "Quick check-in: ${habit.title}", "Tap to mark today complete, skipped, or missed.",
                )
            }
            KIND_GOAL_DEADLINE -> {
                notificationHelper.notify(
                    EonNotificationChannel.GOAL_DEADLINE, entityId.hashCode(),
                    "Goal deadline approaching", "Open EON to review progress and, if needed, run a Recovery simulation.",
                )
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_HABIT_ID = "entity_id"
        const val KEY_KIND = "kind"
        const val KIND_HABIT = "habit"
        const val KIND_GOAL_DEADLINE = "goal_deadline"

        fun millisUntilNextTimeOfDay(hourOfDay: Int, minute: Int): Long {
            val now = LocalDateTime.now(ZoneId.systemDefault())
            var target = now.withHour(hourOfDay).withMinute(minute).withSecond(0).withNano(0)
            if (target.isBefore(now)) target = target.plusDays(1)
            return java.time.Duration.between(now, target).toMillis()
        }
    }
}
