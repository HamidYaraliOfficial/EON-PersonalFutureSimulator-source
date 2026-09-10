package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.simulation.model.ScenarioComparisonRow
import com.eon.futuresimulator.simulation.model.SimulationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scenario Comparison Engine — flattens N [SimulationResult]s into rows for the
 * Comparison Matrix / cards UI, so Baseline, Conservative, Balanced, Aggressive (or any
 * custom set) can be read side by side.
 */
@Singleton
class ScenarioComparisonEngine @Inject constructor() {

    fun compare(results: List<SimulationResult>): List<ScenarioComparisonRow> = results.map { result ->
        val last = result.timeline.lastOrNull()
        val weeklyEffort = (last?.cumulativeEffortMinutes?.toDouble() ?: 0.0) / (result.horizonDays / 7.0).coerceAtLeast(1.0)
        val avgUsage = if (result.timeline.isNotEmpty()) {
            result.timeline.map { it.timeBudgetUsedMinutes.toDouble() / it.timeBudgetAvailableMinutes.coerceAtLeast(1) }.average() * 100.0
        } else 0.0

        ScenarioComparisonRow(
            scenarioName = result.scenarioName,
            completionWeeksMedian = result.goalCompletionEstimate?.medianWeeks,
            effortMinutesPerWeek = weeklyEffort,
            riskScore = result.summaryMetrics["riskScore"] ?: 0.0,
            feasibility = result.feasibility.level,
            consistencyPercent = last?.habitConsistencyPercent ?: 0.0,
            resourceUsagePercent = avgUsage,
        )
    }
}
