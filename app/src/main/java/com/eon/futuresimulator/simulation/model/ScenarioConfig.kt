package com.eon.futuresimulator.simulation.model

import com.eon.futuresimulator.domain.model.ScenarioType
import kotlinx.serialization.Serializable

/**
 * A Scenario is a *configuration*, never a result. Every field here is an explicit,
 * user-visible lever — nothing about "the future" is baked in implicitly. All deltas
 * are relative to the user's derived Baseline (see [BaselineState]) unless the field
 * name says "Target" (an absolute value).
 */
@Serializable
data class ScenarioConfig(
    val name: String,
    val scenarioType: ScenarioType,
    val horizonDays: Int = 90,
    val randomSeed: Long = 42L,
    val targetGoalId: String? = null,

    // --- Interventions (what the user says they'll change) ---
    val dailyStudyMinutesDelta: Int = 0,
    val weeklyWorkoutSessionsDelta: Int = 0,
    val sleepHoursTarget: Double? = null,
    val workHoursPerDayDelta: Double = 0.0,
    val workDaysPerWeekDelta: Int = 0,
    val distractionReductionMinutesPerDay: Int = 0,
    val readingPagesPerDayDelta: Int = 0,
    val taskLoadMultiplier: Double = 1.0,

    // --- Uncertainty Engine parameters (all editable in the Assumption Editor) ---
    /** Probability, per scheduled day, that a habit/session actually happens. 0..1 */
    val habitReliability: Double = 0.8,
    /** Probability a given day is missed entirely (illness, travel, etc). 0..1 */
    val missRate: Double = 0.1,
    /** How much of a missed day's target gets recovered on the following day. 0..1 */
    val recoveryRate: Double = 0.5,
    /** Stddev of session duration as a fraction of its mean — feeds the Normal distribution. */
    val sessionDurationVariability: Double = 0.2,
    /** Extra completion-probability variance on weekends (Routine Analyzer observation). */
    val weekendVariationFactor: Double = 0.85,

    val constraints: List<String> = emptyList(),
    val interventions: List<String> = emptyList(),
) {
    companion object {
        /** The always-present, zero-intervention comparison point. */
        fun baseline(horizonDays: Int = 90, seed: Long = 1L) = ScenarioConfig(
            name = "Baseline",
            scenarioType = ScenarioType.FIXED_SCHEDULE,
            horizonDays = horizonDays,
            randomSeed = seed,
        )
    }
}

/** The user's current, real, derived state — the Simulation Engine's Starting State. */
@Serializable
data class BaselineState(
    val referenceDateEpochMillis: Long,
    /** Minutes genuinely free per weekday (1=Mon..7=Sun), after Calendar + existing commitments. */
    val availableMinutesPerWeekday: Map<Int, Int>,
    val existingTaskBacklogMinutes: Int,
    val sleepAvgMinutesPerNight: Int,
    val studyAvgMinutesPerDay: Int,
    val workAvgMinutesPerDay: Int,
    val exerciseSessionsPerWeek: Int,
    val goals: List<GoalBaseline>,
    val habits: List<HabitBaseline>,
    val sampleSizeDays: Int,
)

@Serializable
data class GoalBaseline(
    val goalId: String,
    val title: String,
    val currentValue: Double,
    val targetValue: Double,
    val unit: String,
    val deadlineEpochMillis: Long?,
    /** Units of progress achieved per "unit of effort minute" observed historically, or null if unknown. */
    val observedRatePerMinute: Double?,
)

@Serializable
data class HabitBaseline(
    val habitId: String,
    val title: String,
    val targetDaysPerWeek: Int,
    val completionRate: Double,
    val currentStreak: Int,
    val averageSessionMinutes: Int,
)
