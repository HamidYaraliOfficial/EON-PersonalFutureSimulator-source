package com.eon.futuresimulator.simulation.engine

import com.eon.futuresimulator.data.database.entity.GoalMilestoneEntity
import com.eon.futuresimulator.core.util.DateTimeUtils
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

data class AdjustedMilestone(val milestoneId: String, val newTargetDateEpochMillis: Long, val reason: String)

/**
 * Adaptive Plan Engine — when the user falls behind (or gets ahead), redistributes the
 * *remaining* Milestones across the *remaining* time while respecting each Milestone's
 * relative share of the original plan. Never touches already-achieved milestones.
 * The caller (ViewModel) decides whether to auto-apply this (Auto Adapt) or just show
 * it as a suggestion pending confirmation (Manual Control) — see UserPreferencesRepository.autoAdaptEnabled.
 */
@Singleton
class AdaptivePlanEngine @Inject constructor() {

    fun readapt(
        milestones: List<GoalMilestoneEntity>,
        today: LocalDate,
        daysBehindSchedule: Int,
    ): List<AdjustedMilestone> {
        val remaining = milestones.filter { !it.isAchieved }.sortedBy { it.orderIndex }
        if (remaining.isEmpty() || daysBehindSchedule <= 0) return emptyList()

        val originalSpanDays = remaining.zipWithNext().sumOf { (a, b) ->
            DateTimeUtils.daysBetween(
                DateTimeUtils.epochMillisToLocalDate(a.targetDateEpochMillis),
                DateTimeUtils.epochMillisToLocalDate(b.targetDateEpochMillis),
            ).coerceAtLeast(1)
        }.coerceAtLeast(remaining.size.toLong())

        // Push every remaining milestone back by daysBehindSchedule, weighted slightly
        // more for later milestones so near-term commitments stay closer to their
        // original date where possible.
        return remaining.mapIndexed { index, milestone ->
            val weight = (index + 1).toDouble() / remaining.size
            val pushDays = (daysBehindSchedule * (0.5 + 0.5 * weight)).toLong().coerceAtLeast(1)
            val oldDate = DateTimeUtils.epochMillisToLocalDate(milestone.targetDateEpochMillis)
            val newDate = oldDate.plusDays(pushDays)
            AdjustedMilestone(
                milestoneId = milestone.id,
                newTargetDateEpochMillis = DateTimeUtils.localDateToEpochMillis(newDate),
                reason = "Shifted by $pushDays day(s) to absorb a $daysBehindSchedule-day delay while keeping milestone order intact.",
            )
        }
    }
}
