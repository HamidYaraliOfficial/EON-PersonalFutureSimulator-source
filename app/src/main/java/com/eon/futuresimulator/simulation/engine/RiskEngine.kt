package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.domain.model.RiskSeverity
import com.eon.futuresimulator.domain.model.RiskType
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.FeasibilityResult
import com.eon.futuresimulator.simulation.model.RiskFinding
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Risk Engine — a transparent rule set (not a black-box model). Every rule below is a
 * plain if/then a user could audit; each finding always carries a Reason and a
 * Suggested Mitigation so the Recommendation Engine can act on it.
 */
@Singleton
class RiskEngine @Inject constructor() {

    fun evaluate(
        scenario: ScenarioConfig,
        baseline: BaselineState,
        feasibility: FeasibilityResult,
    ): List<RiskFinding> {
        val findings = mutableListOf<RiskFinding>()

        // 1) Overcommitment — utilization ratio too high
        if (feasibility.level != FeasibilityLevel.FEASIBLE) {
            findings += RiskFinding(
                type = RiskType.OVERCOMMITMENT,
                severity = if (feasibility.level == FeasibilityLevel.UNREALISTIC) RiskSeverity.CRITICAL else RiskSeverity.HIGH,
                reason = "Scenario requires %.0f min/day against %.0f min/day available (%.0f%% utilization).".format(
                    feasibility.requiredMinutesPerDay, feasibility.availableMinutesPerDay, feasibility.utilizationRatio * 100,
                ),
                suggestedMitigation = "Reduce one intervention (e.g. study minutes or workout frequency) or extend the horizon.",
            )
        }

        // 2) Deadline collision — any goal deadline sooner than horizon allows given required effort
        baseline.goals.forEach { goal ->
            val deadline = goal.deadlineEpochMillis ?: return@forEach
            val daysLeft = (deadline - baseline.referenceDateEpochMillis) / 86_400_000L
            if (daysLeft in 0..scenario.horizonDays && goal.observedRatePerMinute != null && goal.observedRatePerMinute > 0) {
                val remaining = (goal.targetValue - goal.currentValue).coerceAtLeast(0.0)
                val minutesNeeded = remaining / goal.observedRatePerMinute
                val minutesAvailable = daysLeft * feasibility.availableMinutesPerDay
                if (minutesNeeded > minutesAvailable) {
                    findings += RiskFinding(
                        type = RiskType.DEADLINE_COLLISION,
                        severity = RiskSeverity.HIGH,
                        reason = "\"${goal.title}\" deadline is in $daysLeft days but the current pace needs about ${(minutesNeeded / 60).toInt()}h more.",
                        suggestedMitigation = "Move the deadline, increase daily effort for this goal, or lower its target.",
                    )
                }
            }
        }

        // 3) Habit overload — too many active habits scheduled on the same days
        val totalWeeklyHabitSlots = baseline.habits.sumOf { it.targetDaysPerWeek }
        if (totalWeeklyHabitSlots > 7 * baseline.habits.size.coerceAtLeast(1) && baseline.habits.size > 4) {
            findings += RiskFinding(
                type = RiskType.HABIT_OVERLOAD,
                severity = RiskSeverity.MEDIUM,
                reason = "${baseline.habits.size} active habits are scheduled with $totalWeeklyHabitSlots total weekly check-ins.",
                suggestedMitigation = "Consider pausing a lower-priority habit while this scenario is active.",
            )
        }

        // 4) Insufficient time — available time itself is thin regardless of scenario
        val avgAvailable = baseline.availableMinutesPerWeekday.values.average()
        if (avgAvailable < 90) {
            findings += RiskFinding(
                type = RiskType.INSUFFICIENT_TIME,
                severity = RiskSeverity.MEDIUM,
                reason = "Your baseline free time averages only %.0f min/day across the week.".format(avgAvailable),
                suggestedMitigation = "Look for a low-value existing commitment to trim before adding new ones.",
            )
        }

        // 5) Low consistency — habits with completion rate under 50%
        baseline.habits.filter { it.completionRate < 0.5 }.forEach { habit ->
            findings += RiskFinding(
                type = RiskType.LOW_CONSISTENCY,
                severity = RiskSeverity.MEDIUM,
                reason = "\"${habit.title}\" has a %.0f%% completion rate historically.".format(habit.completionRate * 100),
                suggestedMitigation = "Lower the weekly target or move the reminder to a higher-energy time of day.",
            )
        }

        // 6) Task backlog — existing unfinished tasks already exceed a few days of capacity
        if (baseline.existingTaskBacklogMinutes > avgAvailable * 5) {
            findings += RiskFinding(
                type = RiskType.TASK_BACKLOG,
                severity = RiskSeverity.MEDIUM,
                reason = "Existing task backlog is about ${(baseline.existingTaskBacklogMinutes / 60)}h — more than 5 average days of free time.",
                suggestedMitigation = "Clear or reschedule backlog tasks before adding new commitments in this scenario.",
            )
        }

        return findings
    }
}
