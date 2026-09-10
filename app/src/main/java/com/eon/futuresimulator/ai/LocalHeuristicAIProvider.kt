package com.eon.futuresimulator.ai

import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.simulation.model.SensitivityResult
import com.eon.futuresimulator.simulation.model.SimulationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The default AI Advisor: fully on-device, template-based, zero network calls. It reads
 * numbers the Simulation/Sensitivity/Risk engines already computed and turns them into
 * plain-language explanations — it never invents a number of its own.
 */
@Singleton
class LocalHeuristicAIProvider @Inject constructor() : AIProvider {
    override val isCloudBased = false

    override suspend fun explainScenario(result: SimulationResult, sensitivity: List<SensitivityResult>): String {
        val top = sensitivity.firstOrNull()
        val feasibilityNote = when (result.feasibility.level) {
            FeasibilityLevel.FEASIBLE -> "This scenario fits comfortably within your available time."
            FeasibilityLevel.CHALLENGING -> "This scenario leaves little slack in your schedule — small disruptions could push it off track."
            FeasibilityLevel.UNREALISTIC -> "This scenario currently asks for more time per day than you have available."
        }
        val completionNote = result.goalCompletionEstimate?.let {
            "Across %d simulated trajectories, completion lands between %.1f and %.1f weeks (median ~%.1f)."
                .format(result.iterations, it.lowWeeks, it.highWeeks, it.medianWeeks)
        } ?: "There wasn't enough of a trend to project a completion range for this scenario."
        val sensitivityNote = top?.let { "\"${it.variableName}\" has the largest effect on the outcome — ${it.directionNote.lowercase()}." }
            ?: ""

        return listOf(feasibilityNote, completionNote, sensitivityNote).filter { it.isNotBlank() }.joinToString(" ")
    }

    override suspend fun explainWhyMoreStable(a: SimulationResult, b: SimulationResult): String {
        val aSpread = spread(a)
        val bSpread = spread(b)
        val aIsMoreStable = aSpread <= bSpread
        val stableName = if (aIsMoreStable) a.scenarioName else b.scenarioName
        val otherName = if (aIsMoreStable) b.scenarioName else a.scenarioName
        val stableSpread = if (aIsMoreStable) aSpread else bSpread
        val otherSpread = if (aIsMoreStable) bSpread else aSpread

        return "\"$stableName\" is more stable than \"$otherName\": its P10-P90 confidence range is %.1f percentage points wide, vs %.1f for \"$otherName\", meaning outcomes vary less across simulated trajectories."
            .format(stableSpread, otherSpread)
    }

    private fun spread(result: SimulationResult): Double {
        val band = result.confidenceBands["goalProgressPercent"] ?: return 0.0
        val last = band.lastOrNull() ?: return 0.0
        return last.p90 - last.p10
    }

    override suspend fun proposeActionsFor(result: SimulationResult): List<AIActionProposal> {
        val proposals = mutableListOf<AIActionProposal>()
        if (result.feasibility.level != FeasibilityLevel.FEASIBLE) {
            proposals += AIActionProposal(
                id = IdGenerator.newId(),
                summary = "Add a review reminder in 7 days",
                detail = "\"${result.scenarioName}\" is currently ${result.feasibility.level.name.lowercase()}. A check-in reminder helps catch drift early.",
                actionType = AIActionType.CREATE_REMINDER,
            )
        }
        result.risks.firstOrNull()?.let { risk ->
            proposals += AIActionProposal(
                id = IdGenerator.newId(),
                summary = "Create a task: ${risk.suggestedMitigation}",
                detail = risk.reason,
                actionType = AIActionType.CREATE_TASK,
            )
        }
        return proposals
    }
}
