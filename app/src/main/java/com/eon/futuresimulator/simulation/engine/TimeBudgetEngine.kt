package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.simulation.model.BaselineState
import javax.inject.Inject
import javax.inject.Singleton

data class DayTimeBudget(
    val weekday: Int, // 1=Mon .. 7=Sun
    val totalMinutes: Int = 24 * 60,
    val sleepMinutes: Int,
    val calendarBusyMinutes: Int,
    val existingCommitmentMinutes: Int,
) {
    val freeMinutes: Int get() = (totalMinutes - sleepMinutes - calendarBusyMinutes - existingCommitmentMinutes).coerceAtLeast(0)
}

/**
 * Time Budget Engine — computes how many minutes/day and minutes/week are genuinely
 * free once Sleep, Calendar events and existing Task/Habit commitments are subtracted.
 * The Feasibility Analyzer and What-If Engine both read from this.
 */
@Singleton
class TimeBudgetEngine @Inject constructor() {

    fun weeklyFreeMinutes(baseline: BaselineState): Int =
        baseline.availableMinutesPerWeekday.values.sum()

    fun dailyFreeMinutes(baseline: BaselineState, weekday: Int): Int =
        baseline.availableMinutesPerWeekday[weekday] ?: (baseline.availableMinutesPerWeekday.values.average().toInt())

    /** Required minutes/day implied by a scenario's interventions, before checking feasibility. */
    fun requiredMinutesPerDay(
        dailyStudyMinutesDelta: Int,
        weeklyWorkoutSessionsDelta: Int,
        avgWorkoutSessionMinutes: Int,
        distractionReductionMinutesPerDay: Int,
        readingPagesPerDayDelta: Int,
        minutesPerPage: Double = 1.5,
        existingRequiredMinutesPerDay: Double,
    ): Double {
        val workoutMinutesPerDay = (weeklyWorkoutSessionsDelta * avgWorkoutSessionMinutes) / 7.0
        val readingMinutesPerDay = readingPagesPerDayDelta * minutesPerPage
        // Distraction reduction *frees* time rather than consuming it.
        return (existingRequiredMinutesPerDay + dailyStudyMinutesDelta + workoutMinutesPerDay + readingMinutesPerDay - distractionReductionMinutesPerDay)
            .coerceAtLeast(0.0)
    }
}
