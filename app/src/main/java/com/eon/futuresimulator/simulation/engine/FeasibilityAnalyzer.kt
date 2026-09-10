package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.simulation.model.FeasibilityResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Feasibility Analyzer — turns "required minutes/day" vs "available minutes/day" into
 * an explainable Feasible / Challenging / Unrealistic label. Thresholds are fixed,
 * documented constants (never a black box) so the Explainability Panel can always say
 * exactly why a Scenario landed where it did.
 */
@Singleton
class FeasibilityAnalyzer @Inject constructor(
    private val timeBudgetEngine: TimeBudgetEngine,
) {
    companion object {
        const val CHALLENGING_THRESHOLD = 0.75 // utilization above this => Challenging
        const val UNREALISTIC_THRESHOLD = 1.0  // utilization above this => Unrealistic (needs more time than exists)
    }

    fun analyze(requiredMinutesPerDay: Double, availableMinutesPerDay: Double): FeasibilityResult {
        val safeAvailable = availableMinutesPerDay.coerceAtLeast(1.0)
        val ratio = requiredMinutesPerDay / safeAvailable
        val reasons = mutableListOf<String>()

        val level = when {
            ratio > UNRELIABLE_GUARD -> FeasibilityLevel.UNREALISTIC
            ratio > CHALLENGING_THRESHOLD -> FeasibilityLevel.CHALLENGING
            else -> FeasibilityLevel.FEASIBLE
        }

        when (level) {
            FeasibilityLevel.UNREALISTIC -> reasons += "Required time (%.0f min/day) exceeds available time (%.0f min/day) by %.0f%%."
                .format(requiredMinutesPerDay, safeAvailable, (ratio - 1.0) * 100)
            FeasibilityLevel.CHALLENGING -> reasons += "This scenario uses %.0f%% of your available daily time — little margin for missed days."
                .format(ratio * 100)
            FeasibilityLevel.FEASIBLE -> reasons += "This scenario uses %.0f%% of your available daily time, leaving reasonable slack."
                .format(ratio * 100)
        }

        return FeasibilityResult(
            level = level,
            requiredMinutesPerDay = requiredMinutesPerDay,
            availableMinutesPerDay = safeAvailable,
            utilizationRatio = ratio,
            reasons = reasons,
        )
    }

    private val UNRELIABLE_GUARD = UNREALISTIC_THRESHOLD
}
