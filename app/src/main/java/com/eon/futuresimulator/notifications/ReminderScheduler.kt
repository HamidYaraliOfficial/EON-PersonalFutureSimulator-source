package com.eon.futuresimulator.notifications

import androidx.work.*
import com.eon.futuresimulator.workers.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules Habit / Goal / Simulation-review reminders through WorkManager so they
 * survive process death and reboots (WorkManager persists its queue to disk).
 */
@Singleton
class ReminderScheduler @Inject constructor(private val workManager: WorkManager) {

    fun scheduleDailyHabitReminder(habitId: String, hourOfDay: Int, minute: Int) {
        val delay = ReminderWorker.millisUntilNextTimeOfDay(hourOfDay, minute)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_HABIT_ID to habitId, ReminderWorker.KEY_KIND to ReminderWorker.KIND_HABIT))
            .addTag("reminder_habit_$habitId")
            .build()
        workManager.enqueueUniqueWork("habit_reminder_$habitId", ExistingWorkPolicy.REPLACE, request)
    }

    fun scheduleGoalDeadlineReminder(goalId: String, triggerAtEpochMillis: Long) {
        val delay = (triggerAtEpochMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_HABIT_ID to goalId, ReminderWorker.KEY_KIND to ReminderWorker.KIND_GOAL_DEADLINE))
            .addTag("reminder_goal_$goalId")
            .build()
        workManager.enqueueUniqueWork("goal_deadline_$goalId", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(tag: String) = workManager.cancelAllWorkByTag(tag)
}
