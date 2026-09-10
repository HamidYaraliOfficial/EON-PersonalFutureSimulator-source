package com.eon.futuresimulator.goals

import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.data.database.entity.GoalMilestoneEntity
import com.eon.futuresimulator.data.database.entity.TaskEntity
import com.eon.futuresimulator.domain.model.Priority
import com.eon.futuresimulator.domain.model.TaskStatus
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class GoalBreakdownPlan(
    val milestones: List<GoalMilestoneEntity>,
    val weeklyTargetValue: Double,
    val suggestedTasks: List<TaskEntity>,
)

/**
 * Goal Breakdown Engine — turns one big [GoalEntity] into Milestones (evenly paced
 * toward the deadline) + a weekly target + a first batch of suggested Tasks. Everything
 * produced here is a *suggestion* the user reviews before it's saved (see
 * GoalsViewModel.applyBreakdown) — nothing is written automatically.
 */
@Singleton
class GoalBreakdownEngine @Inject constructor() {

    fun breakdown(goal: GoalEntity, milestoneCount: Int = 4, today: LocalDate = DateTimeUtils.todayLocalDate()): GoalBreakdownPlan {
        val deadline = goal.deadlineEpochMillis?.let { DateTimeUtils.epochMillisToLocalDate(it) }
            ?: today.plusWeeks(12) // no deadline set — default a 12-week horizon so a plan can still be shown
        val totalDays = DateTimeUtils.daysBetween(today, deadline).coerceAtLeast(milestoneCount.toLong())
        val remainingValue = (goal.targetValue - goal.currentValue).coerceAtLeast(0.0)

        val milestones = (1..milestoneCount).map { step ->
            val fraction = step.toDouble() / milestoneCount
            val dayOffset = (totalDays * fraction).toLong()
            GoalMilestoneEntity(
                id = IdGenerator.newId(),
                goalId = goal.id,
                title = "Milestone $step of $milestoneCount",
                targetValue = goal.currentValue + remainingValue * fraction,
                targetDateEpochMillis = DateTimeUtils.localDateToEpochMillis(today.plusDays(dayOffset)),
                achievedDateEpochMillis = null,
                isAchieved = false,
                orderIndex = step,
                createdAt = DateTimeUtils.nowEpochMillis(),
                updatedAt = DateTimeUtils.nowEpochMillis(),
                source = DataSource.DERIVED,
                version = 1,
            )
        }

        val totalWeeks = (totalDays / 7.0).coerceAtLeast(1.0)
        val weeklyTargetValue = remainingValue / totalWeeks

        val suggestedTasks = listOf(
            "Define the very first concrete step toward \"${goal.title}\"",
            "Block a recurring weekly session for \"${goal.title}\"",
            "Review progress against Milestone 1",
        ).map { title ->
            TaskEntity(
                id = IdGenerator.newId(),
                goalId = goal.id,
                title = title,
                description = "",
                durationMinutes = 30,
                priority = Priority.MEDIUM,
                deadlineEpochMillis = milestones.firstOrNull()?.targetDateEpochMillis,
                status = TaskStatus.TODO,
                scheduledDateEpochMillis = null,
                isRecurring = false,
                createdAt = DateTimeUtils.nowEpochMillis(),
                updatedAt = DateTimeUtils.nowEpochMillis(),
                source = DataSource.DERIVED,
                version = 1,
            )
        }

        return GoalBreakdownPlan(milestones, weeklyTargetValue, suggestedTasks)
    }
}
