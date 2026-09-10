package com.eon.futuresimulator.habits

import com.eon.futuresimulator.data.database.entity.HabitCheckInEntity
import com.eon.futuresimulator.domain.model.CheckInStatus
import javax.inject.Inject
import javax.inject.Singleton

data class StreakInfo(
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Double,
    val lastCheckInDateEpochMillis: Long?,
)

/**
 * Habit System core — Daily/Weekly/Custom Schedule streak math, completion rate, and
 * recovery-aware "current streak" (a single SKIPPED day marked with a Recovery note
 * does not necessarily reset the streak — see [recoveryGraceDays]).
 */
@Singleton
class HabitStreakCalculator @Inject constructor() {

    fun calculate(checkIns: List<HabitCheckInEntity>, recoveryGraceDays: Int = 0): StreakInfo {
        if (checkIns.isEmpty()) return StreakInfo(0, 0, 0.0, null)
        val sorted = checkIns.sortedBy { it.dateEpochMillis }

        var longest = 0
        var running = 0
        var missedInARow = 0

        sorted.forEach { checkIn ->
            when (checkIn.status) {
                CheckInStatus.COMPLETED -> {
                    running += 1
                    missedInARow = 0
                }
                CheckInStatus.SKIPPED -> {
                    missedInARow += 1
                    if (missedInARow > recoveryGraceDays) {
                        running = 0
                    }
                }
                CheckInStatus.MISSED -> {
                    running = 0
                    missedInARow = 0
                }
            }
            if (running > longest) longest = running
        }

        // Current streak = trailing run of COMPLETED (allowing recoveryGraceDays SKIPPED) from the end.
        var current = 0
        var trailingMissed = 0
        for (checkIn in sorted.asReversed()) {
            when (checkIn.status) {
                CheckInStatus.COMPLETED -> { current++; trailingMissed = 0 }
                CheckInStatus.SKIPPED -> {
                    trailingMissed++
                    if (trailingMissed > recoveryGraceDays) break
                }
                CheckInStatus.MISSED -> break
            }
        }

        val completed = sorted.count { it.status == CheckInStatus.COMPLETED }
        val completionRate = completed.toDouble() / sorted.size

        return StreakInfo(
            currentStreak = current,
            longestStreak = longest,
            completionRate = completionRate,
            lastCheckInDateEpochMillis = sorted.last().dateEpochMillis,
        )
    }
}
