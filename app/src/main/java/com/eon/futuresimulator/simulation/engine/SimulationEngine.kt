package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.simulation.model.*
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Simulation Engine — deterministic, seed-reproducible, and independent of any AI
 * provider (see [com.eon.futuresimulator.ai.AIProvider]: it only *explains* results
 * this engine already produced). Given the same [ScenarioConfig.randomSeed] and the
 * same [BaselineState], [simulateDeterministic] always returns byte-for-byte identical
 * output — verified by SimulationEngineTest#same seed produces same result.
 *
 * Two modes:
 *  - Deterministic single run: one seeded trajectory, used for the Simulation Timeline.
 *  - Monte Carlo mode ([runFull] with iterations > 1): many seeded trajectories
 *    (seed, seed+1, seed+2, ...), aggregated into P10/P50/P90 Confidence Bands.
 */
@Singleton
class SimulationEngine @Inject constructor(
    private val timeBudgetEngine: TimeBudgetEngine,
    private val feasibilityAnalyzer: FeasibilityAnalyzer,
    private val riskEngine: RiskEngine,
) {
    companion object {
        const val AVG_WORKOUT_SESSION_MINUTES = 45.0
        const val MINUTES_PER_READING_PAGE = 1.5
    }

    /** One seeded, reproducible trajectory across the full horizon. Pure function of (scenario, baseline). */
    fun simulateDeterministic(scenario: ScenarioConfig, baseline: BaselineState): List<TimelinePoint> {
        val random = Random(scenario.randomSeed)
        return simulateOneTrajectory(scenario, baseline, random)
    }

    private fun simulateOneTrajectory(scenario: ScenarioConfig, baseline: BaselineState, random: Random): List<TimelinePoint> {
        val startDate = DateTimeUtils.epochMillisToLocalDate(baseline.referenceDateEpochMillis)
        val primaryGoal = baseline.goals.firstOrNull { it.goalId == scenario.targetGoalId } ?: baseline.goals.firstOrNull()
        val avgAvailable = baseline.availableMinutesPerWeekday.values.average().takeIf { !it.isNaN() } ?: 120.0

        var cumulativeProgressUnits = primaryGoal?.currentValue ?: 0.0
        var backlogMinutes = baseline.existingTaskBacklogMinutes.toDouble()
        var recoveryDebtMinutes = 0.0
        var cumulativeEffortMinutes = 0
        var completedDayCount = 0

        val points = ArrayList<TimelinePoint>(scenario.horizonDays)

        for (d in 0 until scenario.horizonDays) {
            val date = startDate.plusDays(d.toLong())
            val weekday = date.dayOfWeek.value // 1..7, Mon..Sun
            val isWeekend = DateTimeUtils.isWeekend(date)
            val availableBase = (baseline.availableMinutesPerWeekday[weekday] ?: avgAvailable.toInt()).toDouble()
            val available = if (isWeekend) availableBase * scenario.weekendVariationFactor else availableBase

            val studyTarget = (baseline.studyAvgMinutesPerDay + scenario.dailyStudyMinutesDelta).coerceAtLeast(0)
            val workoutTarget = (baseline.exerciseSessionsPerWeek + scenario.weeklyWorkoutSessionsDelta) * AVG_WORKOUT_SESSION_MINUTES / 7.0
            val readingTarget = scenario.readingPagesPerDayDelta * MINUTES_PER_READING_PAGE
            val requiredToday = (studyTarget + workoutTarget + readingTarget - scenario.distractionReductionMinutesPerDay).coerceAtLeast(0.0)

            val missedDay = RandomDistribution.bernoulli(random, scenario.missRate)
            val baseReliability = if (isWeekend) scenario.habitReliability * scenario.weekendVariationFactor else scenario.habitReliability
            val completionFraction = when {
                missedDay -> 0.0
                else -> RandomDistribution.normal(random, baseReliability, 0.1, 0.0).coerceAtMost(1.2).coerceAtMost(1.0)
            }

            val recoveredMinutes = recoveryDebtMinutes * scenario.recoveryRate
            recoveryDebtMinutes -= recoveredMinutes

            val plannedEffort = requiredToday * completionFraction + recoveredMinutes
            val actualEffortMinutes = RandomDistribution.normal(
                random, plannedEffort, (plannedEffort * scenario.sessionDurationVariability).coerceAtLeast(0.01),
            )
            val shortfall = requiredToday - actualEffortMinutes
            if (shortfall > 0) recoveryDebtMinutes += shortfall
            if (completionFraction >= 0.5) completedDayCount++

            val leftoverCapacity = (available - requiredToday).coerceAtLeast(0.0)
            backlogMinutes = (backlogMinutes + (baseline.existingTaskBacklogMinutes / 30.0) * scenario.taskLoadMultiplier - leftoverCapacity)
                .coerceAtLeast(0.0)

            cumulativeEffortMinutes += actualEffortMinutes.toInt()

            val progressIncrement = when {
                primaryGoal?.observedRatePerMinute != null && primaryGoal.observedRatePerMinute > 0 ->
                    actualEffortMinutes * primaryGoal.observedRatePerMinute
                primaryGoal != null && requiredToday > 0 ->
                    (actualEffortMinutes / (requiredToday * scenario.horizonDays)) * (primaryGoal.targetValue - (primaryGoal.currentValue))
                else -> 0.0
            }
            cumulativeProgressUnits += progressIncrement

            val goalProgressPercent = if (primaryGoal != null && primaryGoal.targetValue > 0) {
                ((cumulativeProgressUnits / primaryGoal.targetValue) * 100.0).coerceIn(0.0, 130.0)
            } else {
                ((d + 1).toDouble() / scenario.horizonDays) * 100.0
            }

            points += TimelinePoint(
                dayIndex = d,
                dateEpochMillis = DateTimeUtils.localDateToEpochMillis(date),
                goalProgressPercent = goalProgressPercent,
                habitConsistencyPercent = (completedDayCount.toDouble() / (d + 1)) * 100.0,
                timeBudgetUsedMinutes = actualEffortMinutes.toInt(),
                timeBudgetAvailableMinutes = available.toInt(),
                taskBacklogMinutes = backlogMinutes.toInt(),
                cumulativeEffortMinutes = cumulativeEffortMinutes,
            )
        }
        return points
    }

    /**
     * Full analysis: deterministic timeline + (optional) Monte Carlo confidence bands +
     * Feasibility + Risk + Assumption disclosure + a goal-completion Range Estimate.
     * [iterations] <= 1 skips Monte Carlo and confidence bands collapse to the single run.
     */
    suspend fun runFull(
        scenario: ScenarioConfig,
        baseline: BaselineState,
        iterations: Int,
        onProgress: suspend (Float) -> Unit = {},
    ): SimulationResult {
        val deterministicTimeline = simulateDeterministic(scenario, baseline)

        val requiredPerDay = timeBudgetEngine.requiredMinutesPerDay(
            dailyStudyMinutesDelta = scenario.dailyStudyMinutesDelta,
            weeklyWorkoutSessionsDelta = scenario.weeklyWorkoutSessionsDelta,
            avgWorkoutSessionMinutes = AVG_WORKOUT_SESSION_MINUTES.toInt(),
            distractionReductionMinutesPerDay = scenario.distractionReductionMinutesPerDay,
            readingPagesPerDayDelta = scenario.readingPagesPerDayDelta,
            existingRequiredMinutesPerDay = baseline.studyAvgMinutesPerDay.toDouble(),
        )
        val availablePerDay = baseline.availableMinutesPerWeekday.values.average().takeIf { !it.isNaN() } ?: 120.0
        val feasibility = feasibilityAnalyzer.analyze(requiredPerDay, availablePerDay)
        val risks = riskEngine.evaluate(scenario, baseline, feasibility)

        val effectiveIterations = iterations.coerceAtLeast(1)
        val confidenceBands: Map<String, List<ConfidenceBandPoint>>
        val completionEstimate: RangeEstimate?

        if (effectiveIterations <= 1) {
            confidenceBands = mapOf(
                "goalProgressPercent" to deterministicTimeline.map { ConfidenceBandPoint(it.dayIndex, it.goalProgressPercent, it.goalProgressPercent, it.goalProgressPercent) },
            )
            completionEstimate = estimateCompletionWeeks(listOf(deterministicTimeline), scenario.horizonDays)
        } else {
            val allTrajectories = ArrayList<List<TimelinePoint>>(effectiveIterations)
            for (i in 0 until effectiveIterations) {
                val random = Random(scenario.randomSeed + i)
                allTrajectories += simulateOneTrajectory(scenario, baseline, random)
                if (i % 10 == 0) onProgress(i.toFloat() / effectiveIterations)
            }
            onProgress(1f)
            confidenceBands = buildConfidenceBands(allTrajectories, scenario.horizonDays)
            completionEstimate = estimateCompletionWeeks(allTrajectories, scenario.horizonDays)
        }

        val assumptions = buildAssumptions(scenario, baseline, feasibility)

        val summary = mapOf(
            "finalGoalProgressPercent" to (deterministicTimeline.lastOrNull()?.goalProgressPercent ?: 0.0),
            "finalHabitConsistencyPercent" to (deterministicTimeline.lastOrNull()?.habitConsistencyPercent ?: 0.0),
            "finalTaskBacklogMinutes" to (deterministicTimeline.lastOrNull()?.taskBacklogMinutes?.toDouble() ?: 0.0),
            "totalEffortMinutes" to (deterministicTimeline.lastOrNull()?.cumulativeEffortMinutes?.toDouble() ?: 0.0),
            "riskScore" to risks.sumOf { it.severity.ordinal + 1.0 },
        )

        return SimulationResult(
            scenarioName = scenario.name,
            randomSeed = scenario.randomSeed,
            iterations = effectiveIterations,
            horizonDays = scenario.horizonDays,
            timeline = deterministicTimeline,
            confidenceBands = confidenceBands,
            feasibility = feasibility,
            risks = risks,
            sensitivity = emptyList(), // populated separately by SensitivityAnalyzer on demand
            assumptions = assumptions,
            goalCompletionEstimate = completionEstimate,
            summaryMetrics = summary,
            generatedAtEpochMillis = DateTimeUtils.nowEpochMillis(),
        )
    }

    private fun buildConfidenceBands(trajectories: List<List<TimelinePoint>>, horizonDays: Int): Map<String, List<ConfidenceBandPoint>> {
        val progressBand = ArrayList<ConfidenceBandPoint>(horizonDays)
        for (d in 0 until horizonDays) {
            val values = trajectories.mapNotNull { it.getOrNull(d)?.goalProgressPercent }.sorted()
            if (values.isEmpty()) continue
            progressBand += ConfidenceBandPoint(
                dayIndex = d,
                p10 = RandomDistribution.percentile(values, 0.10),
                p50 = RandomDistribution.percentile(values, 0.50),
                p90 = RandomDistribution.percentile(values, 0.90),
            )
        }
        return mapOf("goalProgressPercent" to progressBand)
    }

    private fun estimateCompletionWeeks(trajectories: List<List<TimelinePoint>>, horizonDays: Int): RangeEstimate? {
        val weeksToComplete = trajectories.mapNotNull { traj ->
            val hitDay = traj.firstOrNull { it.goalProgressPercent >= 100.0 }?.dayIndex
            if (hitDay != null) return@mapNotNull hitDay / 7.0

            // Not reached within horizon — extrapolate from the trend of the second half.
            val half = traj.size / 2
            if (half < 2) return@mapNotNull null
            val startVal = traj[half].goalProgressPercent
            val endVal = traj.last().goalProgressPercent
            val ratePerDay = (endVal - startVal) / (traj.size - half)
            if (ratePerDay <= 0.0001) return@mapNotNull null
            val remaining = (100.0 - endVal).coerceAtLeast(0.0)
            val extraDays = remaining / ratePerDay
            (traj.size + extraDays) / 7.0
        }.sorted()
        if (weeksToComplete.isEmpty()) return null
        return RangeEstimate(
            lowWeeks = RandomDistribution.percentile(weeksToComplete, 0.10),
            medianWeeks = RandomDistribution.percentile(weeksToComplete, 0.50),
            highWeeks = RandomDistribution.percentile(weeksToComplete, 0.90),
        )
    }

    private fun buildAssumptions(scenario: ScenarioConfig, baseline: BaselineState, feasibility: FeasibilityResult): List<AssumptionItem> = listOf(
        AssumptionItem("habitReliability", "%.0f%%".format(scenario.habitReliability * 100), "Chance a scheduled day is actually completed, based on your history and the Assumption Editor."),
        AssumptionItem("missRate", "%.0f%%".format(scenario.missRate * 100), "Chance an entire day is missed (illness, travel, etc.)."),
        AssumptionItem("weekendVariationFactor", "%.0f%%".format(scenario.weekendVariationFactor * 100), "Weekends are modeled at this fraction of weekday reliability, per your Routine Analyzer pattern."),
        AssumptionItem("availableMinutesPerDay", "%.0f min".format(feasibility.availableMinutesPerDay), "Derived from Calendar + existing commitments, averaged across the week."),
        AssumptionItem("randomSeed", scenario.randomSeed.toString(), "Re-running this exact scenario with the same seed reproduces an identical result."),
    )
}
