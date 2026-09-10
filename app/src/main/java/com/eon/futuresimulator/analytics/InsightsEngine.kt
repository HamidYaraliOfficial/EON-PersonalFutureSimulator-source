package com.eon.futuresimulator.analytics

import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.data.database.entity.HabitCheckInEntity
import com.eon.futuresimulator.data.database.entity.ProgressMetricEntity
import com.eon.futuresimulator.domain.model.CheckInStatus
import javax.inject.Inject
import javax.inject.Singleton

data class Insight(val title: String, val detail: String, val confidence: Double)

/**
 * Insights Engine — finds *observable* trends in the user's own historical data.
 * Every insight requires [MIN_SAMPLES] data points; below that threshold EON says
 * nothing rather than guessing. Insights are always phrased as Observations
 * ("has been", "so far") not predictions.
 */
@Singleton
class InsightsEngine @Inject constructor() {
    companion object { const val MIN_SAMPLES = 10 }

    fun weekendVsWeekdayCompletion(checkIns: List<HabitCheckInEntity>): Insight? {
        if (checkIns.size < MIN_SAMPLES) return null
        val weekday = checkIns.filter { !DateTimeUtils.isWeekend(DateTimeUtils.epochMillisToLocalDate(it.dateEpochMillis)) }
        val weekend = checkIns.filter { DateTimeUtils.isWeekend(DateTimeUtils.epochMillisToLocalDate(it.dateEpochMillis)) }
        if (weekday.size < 4 || weekend.size < 4) return null

        val weekdayRate = weekday.count { it.status == CheckInStatus.COMPLETED }.toDouble() / weekday.size
        val weekendRate = weekend.count { it.status == CheckInStatus.COMPLETED }.toDouble() / weekend.size
        val gap = weekdayRate - weekendRate
        if (kotlin.math.abs(gap) < 0.1) return null // not a meaningful pattern

        val confidence = (checkIns.size.toDouble() / (checkIns.size + 20)).coerceIn(0.0, 0.9)
        return if (gap > 0) {
            Insight(
                title = "Weekend completion tends to be lower",
                detail = "Your completion rate has been about %.0f%% on weekdays vs %.0f%% on weekends, based on %d check-ins.".format(weekdayRate * 100, weekendRate * 100, checkIns.size),
                confidence = confidence,
            )
        } else {
            Insight(
                title = "Weekends have been more consistent",
                detail = "Your completion rate has been about %.0f%% on weekends vs %.0f%% on weekdays, based on %d check-ins.".format(weekendRate * 100, weekdayRate * 100, checkIns.size),
                confidence = confidence,
            )
        }
    }

    fun progressTrend(history: List<ProgressMetricEntity>): Insight? {
        if (history.size < MIN_SAMPLES) return null
        val sorted = history.sortedBy { it.dateEpochMillis }
        val firstHalf = sorted.take(sorted.size / 2).map { it.value }.average()
        val secondHalf = sorted.takeLast(sorted.size / 2).map { it.value }.average()
        if (secondHalf.isNaN() || firstHalf.isNaN()) return null
        val change = secondHalf - firstHalf
        if (kotlin.math.abs(change) < 0.01) return null

        val confidence = (history.size.toDouble() / (history.size + 20)).coerceIn(0.0, 0.9)
        return Insight(
            title = if (change > 0) "Progress has been accelerating" else "Progress pace has been slowing",
            detail = "The average value in the second half of your logged history (%.1f) is %s than the first half (%.1f).".format(
                secondHalf, if (change > 0) "higher" else "lower", firstHalf,
            ),
            confidence = confidence,
        )
    }
}
