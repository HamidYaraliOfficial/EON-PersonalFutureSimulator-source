package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.domain.repository.*
import com.eon.futuresimulator.habits.HabitStreakCalculator
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.GoalBaseline
import com.eon.futuresimulator.simulation.model.HabitBaseline
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the Simulation Engine's real Starting State from the user's actual Room data —
 * this is the seam where "the app's real data" becomes "the engine's input"; nothing
 * about a Scenario run is mocked or hand-typed by a screen.
 */
@Singleton
class BaselineStateBuilder @Inject constructor(
    private val goalRepository: GoalRepository,
    private val habitRepository: HabitRepository,
    private val routineRepository: RoutineRepository,
    private val taskRepository: TaskRepository,
    private val progressRepository: ProgressRepository,
    private val habitStreakCalculator: HabitStreakCalculator,
) {
    suspend fun build(targetGoalId: String? = null): BaselineState {
        val goals = goalRepository.getAllGoalsOnce()
        val habits = habitRepository.getAllHabitsOnce()
        val routines = routineRepository.getAllOnce()
        val backlog = taskRepository.getBacklogOnce()

        val availableByWeekday = (1..7).associateWith { weekday ->
            routines.firstOrNull { it.dayOfWeekMask == (1 shl (weekday - 1)) }?.freeMinutes ?: DEFAULT_FREE_MINUTES
        }

        val goalBaselines = goals.map { goal ->
            val history = progressRepository.getProgressOnce(goal.id).sortedBy { it.dateEpochMillis }
            val rate = if (history.size >= 2) {
                val first = history.first(); val last = history.last()
                val days = ((last.dateEpochMillis - first.dateEpochMillis) / 86_400_000.0).coerceAtLeast(1.0)
                val deltaValue = last.value - first.value
                // Convert "value change per day" into "value change per minute of effort" using a
                // conservative assumed 45 effort-minutes/day if no explicit effort tracking exists.
                (deltaValue / days / 45.0).takeIf { it.isFinite() && it > 0 }
            } else null
            GoalBaseline(
                goalId = goal.id, title = goal.title, currentValue = goal.currentValue,
                targetValue = goal.targetValue, unit = goal.unit,
                deadlineEpochMillis = goal.deadlineEpochMillis, observedRatePerMinute = rate,
            )
        }

        val habitBaselines = habits.map { habit ->
            val checkIns = habitRepository.getCheckInsOnce(habit.id)
            val streak = habitStreakCalculator.calculate(checkIns)
            HabitBaseline(
                habitId = habit.id, title = habit.title, targetDaysPerWeek = habit.targetDaysPerWeek,
                completionRate = streak.completionRate, currentStreak = streak.currentStreak,
                averageSessionMinutes = DEFAULT_SESSION_MINUTES,
            )
        }

        val effectiveTarget = targetGoalId ?: goals.firstOrNull { it.currentValue < it.targetValue }?.id

        return BaselineState(
            referenceDateEpochMillis = DateTimeUtils.nowEpochMillis(),
            availableMinutesPerWeekday = availableByWeekday,
            existingTaskBacklogMinutes = backlog.sumOf { it.durationMinutes },
            sleepAvgMinutesPerNight = routines.map { it.sleepMinutes }.filter { it > 0 }.average().takeIf { !it.isNaN() }?.toInt() ?: 420,
            studyAvgMinutesPerDay = routines.map { it.studyMinutes }.filter { it > 0 }.average().takeIf { !it.isNaN() }?.toInt() ?: 30,
            workAvgMinutesPerDay = routines.map { it.workMinutes }.filter { it > 0 }.average().takeIf { !it.isNaN() }?.toInt() ?: 300,
            exerciseSessionsPerWeek = habits.count { it.title.contains("workout", true) || it.title.contains("exercise", true) }
                .coerceAtLeast(if (habitBaselines.isEmpty()) 0 else 1),
            goals = goalBaselines.map { it.copy(currentValue = if (it.goalId == effectiveTarget) it.currentValue else it.currentValue) },
            habits = habitBaselines,
            sampleSizeDays = routines.sumOf { it.sampleSize },
        )
    }

    companion object {
        const val DEFAULT_FREE_MINUTES = 150
        const val DEFAULT_SESSION_MINUTES = 30
    }
}
