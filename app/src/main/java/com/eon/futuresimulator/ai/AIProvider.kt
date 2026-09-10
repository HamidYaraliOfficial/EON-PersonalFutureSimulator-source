package com.eon.futuresimulator.ai

import com.eon.futuresimulator.simulation.model.SensitivityResult
import com.eon.futuresimulator.simulation.model.SimulationResult

/** One proposed AI action awaiting user Preview + Confirmation before anything real changes. */
data class AIActionProposal(
    val id: String,
    val summary: String,
    val detail: String,
    val actionType: AIActionType,
)

enum class AIActionType { CREATE_TASK, CREATE_REMINDER, MODIFY_HABIT, MODIFY_CALENDAR_LINK, RUN_SCENARIO }

/**
 * AI Future Advisor — Provider Interface. The Simulation Engine works completely
 * without any implementation of this interface (see SimulationEngine — zero references
 * to AIProvider). [LocalHeuristicAIProvider] is the always-available, on-device,
 * template-based default; [CloudAIProvider] is an optional, consent-gated alternative.
 * Every AI-proposed change to Task/Calendar/Habit surfaces as an [AIActionProposal] —
 * nothing is applied without an explicit user confirmation in the ViewModel layer.
 */
interface AIProvider {
    val isCloudBased: Boolean

    suspend fun explainScenario(result: SimulationResult, sensitivity: List<SensitivityResult>): String

    suspend fun explainWhyMoreStable(a: SimulationResult, b: SimulationResult): String

    suspend fun proposeActionsFor(result: SimulationResult): List<AIActionProposal>
}
