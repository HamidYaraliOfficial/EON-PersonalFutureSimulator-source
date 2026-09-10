package com.eon.futuresimulator.ai

import com.eon.futuresimulator.simulation.model.SensitivityResult
import com.eon.futuresimulator.simulation.model.SimulationResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optional Cloud LLM provider. Disabled unless the user turns on
 * UserPreferencesRepository.cloudAiConsent in the Privacy Center — the Data Source
 * Inspector always shows which provider produced a given explanation. Only the
 * minimal, already-computed Simulation summary is ever sent; raw personal records
 * (Goals/Habits/Tasks/Calendar rows) never leave the device through this path.
 *
 * This is a structural stub: wire in a real HTTPS client + API key from Android
 * Keystore (see security/KeystoreManager) before enabling it for real cloud calls.
 */
@Singleton
class CloudAIProvider @Inject constructor() : AIProvider {
    override val isCloudBased = true

    override suspend fun explainScenario(result: SimulationResult, sensitivity: List<SensitivityResult>): String {
        throw UnsupportedOperationException(
            "Cloud AI is not configured. Enable Cloud AI consent in the Privacy Center and provide an API " +
                "endpoint/key via security/KeystoreManager before using CloudAIProvider.",
        )
    }

    override suspend fun explainWhyMoreStable(a: SimulationResult, b: SimulationResult): String =
        throw UnsupportedOperationException("Cloud AI is not configured.")

    override suspend fun proposeActionsFor(result: SimulationResult): List<AIActionProposal> =
        throw UnsupportedOperationException("Cloud AI is not configured.")
}
