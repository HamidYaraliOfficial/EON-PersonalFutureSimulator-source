package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import javax.inject.Inject
import javax.inject.Singleton

data class RecoveryPlanResult(
    val stillAchievableWithinHorizon: Boolean,
    val adjustedDailyRequirementMinutes: Double,
    val extraDaysNeeded: Int,
    val notes: String,
)

/**
 * Recovery Simulator — "What if I miss N days of a habit/goal effort starting today?"
 * Re-derives the deterministic trajectory with those days forced to zero completion,
 * then reports whether the original horizon is still realistic and what daily
 * requirement would be needed afterwards to catch up.
 */
@Singleton
class RecoverySimulator @Inject constructor(
    private val simulationEngine: SimulationEngine,
    private val feasibilityAnalyzer: FeasibilityAnalyzer,
) {
    fun simulateMissedDays(scenario: ScenarioConfig, baseline: BaselineState, missedDays: Int): RecoveryPlanResult {
        // Model the miss by forcing a very high missRate but only conceptually for the
        // affected window — approximated here by raising missRate proportionally to the
        // fraction of the horizon those missed days represent, which is a transparent,
        // documented simplification (see Assumption Panel copy in the UI layer).
        val impactedFraction = (missedDays.toDouble() / scenario.horizonDays).coerceIn(0.0, 1.0)
        val adjustedScenario = scenario.copy(missRate = (scenario.missRate + impactedFraction).coerceAtMost(0.95))

        val withMisses = simulationEngine.simulateDeterministic(adjustedScenario, baseline)
        val withoutMisses = simulationEngine.simulateDeterministic(scenario, baseline)

        val finalWith = withMisses.lastOrNull()?.goalProgressPercent ?: 0.0
        val finalWithout = withoutMisses.lastOrNull()?.goalProgressPercent ?: 0.0
        val gap = (finalWithout - finalWith).coerceAtLeast(0.0)

        val avgDailyGainWithout = finalWithout / scenario.horizonDays.coerceAtLeast(1)
        val extraDaysNeeded = if (avgDailyGainWithout > 0.0001) (gap / avgDailyGainWithout).toInt() else 0

        val stillAchievable = finalWith >= 95.0 || extraDaysNeeded <= 7

        val requiredPerDay = feasibilityAnalyzer.analyze(
            requiredMinutesPerDay = withMisses.lastOrNull()?.timeBudgetUsedMinutes?.toDouble() ?: 0.0,
            availableMinutesPerDay = withMisses.lastOrNull()?.timeBudgetAvailableMinutes?.toDouble() ?: 1.0,
        )

        return RecoveryPlanResult(
            stillAchievableWithinHorizon = stillAchievable,
            adjustedDailyRequirementMinutes = requiredPerDay.requiredMinutesPerDay,
            extraDaysNeeded = extraDaysNeeded,
            notes = if (stillAchievable) {
                "Missing $missedDays day(s) is recoverable within the current horizon at the configured recovery rate."
            } else {
                "Missing $missedDays day(s) likely pushes completion about $extraDaysNeeded day(s) past the horizon — consider the Adaptive Plan Engine to redistribute remaining milestones."
            },
        )
    }
}
