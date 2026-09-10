package com.eon.futuresimulator.recommendations

import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.RecommendationEntity
import com.eon.futuresimulator.domain.model.RecommendationStatus
import com.eon.futuresimulator.simulation.model.SensitivityResult
import com.eon.futuresimulator.simulation.model.SimulationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Recommendation Engine — every suggestion is derived directly from an already-computed
 * Sensitivity/Simulation result and always carries its Assumption + Context in plain
 * text (never a bare "you should..." with no reasoning shown).
 */
@Singleton
class RecommendationEngine @Inject constructor() {

    fun fromSensitivity(result: SimulationResult, sensitivity: List<SensitivityResult>, relatedGoalId: String?): RecommendationEntity? {
        val top = sensitivity.firstOrNull() ?: return null
        return RecommendationEntity(
            id = IdGenerator.newId(),
            title = "Focus on \"${top.variableName}\" for the biggest effect",
            explanation = "${top.directionNote}. Across the tested +/-20% range, this variable moved final Goal Progress by about ${"%.1f".format(top.impactScore)} percentage points — more than any other lever in \"${result.scenarioName}\".",
            relatedGoalId = relatedGoalId,
            expectedImpact = "~${"%.1f".format(top.impactScore)} pp change in Goal Progress",
            assumption = "Based on a Sensitivity Analysis of ${sensitivity.size} variables using the deterministic engine with seed ${result.randomSeed}.",
            status = RecommendationStatus.NEW,
            createdAt = DateTimeUtils.nowEpochMillis(),
            updatedAt = DateTimeUtils.nowEpochMillis(),
            source = DataSource.DERIVED,
            version = 1,
        )
    }

    fun fromFeasibility(result: SimulationResult, relatedGoalId: String?): RecommendationEntity? {
        if (result.feasibility.level.name == "FEASIBLE") return null
        val overBy = (result.feasibility.requiredMinutesPerDay - result.feasibility.availableMinutesPerDay).coerceAtLeast(0.0)
        return RecommendationEntity(
            id = IdGenerator.newId(),
            title = "Trim about ${overBy.toInt()} min/day to make this feasible",
            explanation = "\"${result.scenarioName}\" currently requires ${result.feasibility.requiredMinutesPerDay.toInt()} min/day against ${result.feasibility.availableMinutesPerDay.toInt()} min/day available.",
            relatedGoalId = relatedGoalId,
            expectedImpact = "Moves this scenario toward Feasible",
            assumption = "Available time is derived from your Calendar + Routine averages; actual daily variation is not included here.",
            status = RecommendationStatus.NEW,
            createdAt = DateTimeUtils.nowEpochMillis(),
            updatedAt = DateTimeUtils.nowEpochMillis(),
            source = DataSource.DERIVED,
            version = 1,
        )
    }
}
