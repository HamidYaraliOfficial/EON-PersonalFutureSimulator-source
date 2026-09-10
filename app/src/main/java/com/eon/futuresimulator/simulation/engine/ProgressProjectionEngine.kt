package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.data.database.entity.ProgressMetricEntity
import com.eon.futuresimulator.simulation.model.RangeEstimate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Progress Projection Engine — fits a simple linear rate to the user's own historical
 * [ProgressMetricEntity] points and projects forward as a Range, never a single number.
 * Requires at least [MIN_DATA_POINTS] observations; otherwise returns null rather than
 * fabricating a projection from insufficient data.
 */
@Singleton
class ProgressProjectionEngine @Inject constructor() {
    companion object { const val MIN_DATA_POINTS = 4 }

    fun projectWeeksToTarget(history: List<ProgressMetricEntity>, targetValue: Double): RangeEstimate? {
        if (history.size < MIN_DATA_POINTS) return null
        val sorted = history.sortedBy { it.dateEpochMillis }
        val first = sorted.first()
        val last = sorted.last()
        val elapsedDays = max(1.0, (last.dateEpochMillis - first.dateEpochMillis) / 86_400_000.0)
        val totalDelta = last.value - first.value
        if (totalDelta <= 0) return null // no observed positive trend — nothing honest to project

        val avgRatePerDay = totalDelta / elapsedDays

        // Use the residual spread of individual day-over-day rates as a simple, explainable
        // uncertainty measure rather than a fixed +/-X% guess.
        val dayRates = sorted.zipWithNext().mapNotNull { (a, b) ->
            val days = (b.dateEpochMillis - a.dateEpochMillis) / 86_400_000.0
            if (days <= 0) null else (b.value - a.value) / days
        }
        val meanRate = dayRates.average().takeIf { !it.isNaN() } ?: avgRatePerDay
        val variance = dayRates.map { (it - meanRate) * (it - meanRate) }.average().takeIf { !it.isNaN() } ?: 0.0
        val stdDev = kotlin.math.sqrt(variance)

        val remaining = (targetValue - last.value).coerceAtLeast(0.0)
        val optimisticRate = (meanRate + stdDev).coerceAtLeast(0.0001)
        val pessimisticRate = (meanRate - stdDev).coerceAtLeast(0.0001)

        val lowWeeks = (remaining / optimisticRate) / 7.0
        val highWeeks = (remaining / pessimisticRate) / 7.0
        val medianWeeks = (remaining / meanRate.coerceAtLeast(0.0001)) / 7.0

        return RangeEstimate(
            lowWeeks = lowWeeks.coerceAtMost(highWeeks),
            medianWeeks = medianWeeks,
            highWeeks = highWeeks.coerceAtLeast(lowWeeks),
        )
    }
}
