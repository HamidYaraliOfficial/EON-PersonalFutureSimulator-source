package com.eon.futuresimulator.simulation.model

import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.domain.model.RiskSeverity
import com.eon.futuresimulator.domain.model.RiskType
import kotlinx.serialization.Serializable

/** One point on the deterministic Simulation Timeline (single seeded run, no Monte Carlo spread). */
@Serializable
data class TimelinePoint(
    val dayIndex: Int,
    val dateEpochMillis: Long,
    val goalProgressPercent: Double,
    val habitConsistencyPercent: Double,
    val timeBudgetUsedMinutes: Int,
    val timeBudgetAvailableMinutes: Int,
    val taskBacklogMinutes: Int,
    val cumulativeEffortMinutes: Int,
)

/** Monte Carlo output for one metric at one point in time: P10/P50/P90 spread. */
@Serializable
data class ConfidenceBandPoint(
    val dayIndex: Int,
    val p10: Double,
    val p50: Double,
    val p90: Double,
)

@Serializable
data class FeasibilityResult(
    val level: FeasibilityLevel,
    val requiredMinutesPerDay: Double,
    val availableMinutesPerDay: Double,
    val utilizationRatio: Double,
    val reasons: List<String>,
)

@Serializable
data class RiskFinding(
    val type: RiskType,
    val severity: RiskSeverity,
    val reason: String,
    val suggestedMitigation: String,
)

@Serializable
data class SensitivityResult(
    val variableName: String,
    /** Absolute change in the target metric when this variable is perturbed by +/-20%. Higher = more influential. */
    val impactScore: Double,
    val directionNote: String,
)

@Serializable
data class RangeEstimate(
    val lowWeeks: Double,
    val medianWeeks: Double,
    val highWeeks: Double,
    val confidencePercent: Int = 80,
)

@Serializable
data class AssumptionItem(
    val key: String,
    val value: String,
    val explanation: String,
)

/**
 * Full output of one Simulation Run. Every number here is explicitly an Estimate /
 * Projection produced under the [assumptions] listed — never presented as fact.
 */
@Serializable
data class SimulationResult(
    val scenarioName: String,
    val randomSeed: Long,
    val iterations: Int,
    val horizonDays: Int,
    val timeline: List<TimelinePoint>,
    val confidenceBands: Map<String, List<ConfidenceBandPoint>>,
    val feasibility: FeasibilityResult,
    val risks: List<RiskFinding>,
    val sensitivity: List<SensitivityResult>,
    val assumptions: List<AssumptionItem>,
    val goalCompletionEstimate: RangeEstimate?,
    val summaryMetrics: Map<String, Double>,
    val generatedAtEpochMillis: Long,
)

/** Side-by-side comparison of N scenario results, used by the Comparison Matrix UI. */
@Serializable
data class ScenarioComparisonRow(
    val scenarioName: String,
    val completionWeeksMedian: Double?,
    val effortMinutesPerWeek: Double,
    val riskScore: Double,
    val feasibility: FeasibilityLevel,
    val consistencyPercent: Double,
    val resourceUsagePercent: Double,
)
