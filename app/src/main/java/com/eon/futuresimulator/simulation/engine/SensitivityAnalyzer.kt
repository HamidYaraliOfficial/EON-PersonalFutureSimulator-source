package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import com.eon.futuresimulator.simulation.model.SensitivityResult
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sensitivity Analysis Engine — perturbs each numeric lever by +/-20% (one at a time,
 * holding everything else fixed), re-runs the *deterministic* engine for speed, and
 * ranks variables by how much the final Goal Progress moved. Used for the Sensitivity
 * Chart / tornado diagram.
 */
@Singleton
class SensitivityAnalyzer @Inject constructor(
    private val simulationEngine: SimulationEngine,
) {
    private data class Lever(
        val name: String,
        val get: (ScenarioConfig) -> Double,
        val withValue: (ScenarioConfig, Double) -> ScenarioConfig,
    )

    private val levers = listOf(
        Lever("Daily study minutes", { it.dailyStudyMinutesDelta.toDouble() }, { s, v -> s.copy(dailyStudyMinutesDelta = v.toInt()) }),
        Lever("Weekly workout sessions", { it.weeklyWorkoutSessionsDelta.toDouble() }, { s, v -> s.copy(weeklyWorkoutSessionsDelta = v.toInt()) }),
        Lever("Habit reliability", { it.habitReliability }, { s, v -> s.copy(habitReliability = v.coerceIn(0.0, 1.0)) }),
        Lever("Miss rate", { it.missRate }, { s, v -> s.copy(missRate = v.coerceIn(0.0, 1.0)) }),
        Lever("Recovery rate", { it.recoveryRate }, { s, v -> s.copy(recoveryRate = v.coerceIn(0.0, 1.0)) }),
        Lever("Distraction reduction (min/day)", { it.distractionReductionMinutesPerDay.toDouble() }, { s, v -> s.copy(distractionReductionMinutesPerDay = v.toInt()) }),
        Lever("Reading pages/day", { it.readingPagesPerDayDelta.toDouble() }, { s, v -> s.copy(readingPagesPerDayDelta = v.toInt()) }),
    )

    fun analyze(scenario: ScenarioConfig, baseline: BaselineState): List<SensitivityResult> {
        val baseFinal = simulationEngine.simulateDeterministic(scenario, baseline).lastOrNull()?.goalProgressPercent ?: 0.0

        return levers.map { lever ->
            val original = lever.get(scenario)
            val delta = if (abs(original) < 1e-6) 1.0 else abs(original) * 0.2

            val upScenario = lever.withValue(scenario, original + delta)
            val downScenario = lever.withValue(scenario, original - delta)

            val upFinal = simulationEngine.simulateDeterministic(upScenario, baseline).lastOrNull()?.goalProgressPercent ?: baseFinal
            val downFinal = simulationEngine.simulateDeterministic(downScenario, baseline).lastOrNull()?.goalProgressPercent ?: baseFinal

            val impact = (abs(upFinal - baseFinal) + abs(baseFinal - downFinal)) / 2.0
            val direction = if (upFinal >= downFinal) "Increasing this raises Goal Progress" else "Decreasing this raises Goal Progress"

            SensitivityResult(variableName = lever.name, impactScore = impact, directionNote = direction)
        }.sortedByDescending { it.impactScore }
    }
}
