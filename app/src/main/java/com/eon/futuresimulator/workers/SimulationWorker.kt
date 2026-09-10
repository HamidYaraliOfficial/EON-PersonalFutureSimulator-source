package com.eon.futuresimulator.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.SimulationRunEntity
import com.eon.futuresimulator.domain.model.SimulationRunStatus
import com.eon.futuresimulator.domain.repository.ScenarioRepository
import com.eon.futuresimulator.simulation.engine.SimulationEngine
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import com.eon.futuresimulator.core.util.DataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Runs a (potentially Monte Carlo, potentially long) Simulation off the UI thread via
 * WorkManager so heavy runs survive process death and never block Compose. Progress is
 * published through [setProgress] and the Simulator screen observes it with
 * `workManager.getWorkInfoByIdFlow`. Truly cancellable: [SimulationEngine.runFull]
 * checks [isStopped] between Monte Carlo iterations.
 */
@HiltWorker
class SimulationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val simulationEngine: SimulationEngine,
    private val scenarioRepository: ScenarioRepository,
) : CoroutineWorker(context, params) {

    private val json = Json { encodeDefaults = true }

    override suspend fun doWork(): Result {
        val runId = inputData.getString(KEY_RUN_ID) ?: return Result.failure()
        val scenarioJson = inputData.getString(KEY_SCENARIO_JSON) ?: return Result.failure()
        val baselineJson = inputData.getString(KEY_BASELINE_JSON) ?: return Result.failure()
        val iterations = inputData.getInt(KEY_ITERATIONS, 200)

        val scenario = runCatching { json.decodeFromString(ScenarioConfig.serializer(), scenarioJson) }.getOrNull()
            ?: return Result.failure()
        val baseline = runCatching { json.decodeFromString(BaselineState.serializer(), baselineJson) }.getOrNull()
            ?: return Result.failure()

        scenarioRepository.getRun(runId)?.let {
            scenarioRepository.upsertRun(it.copy(status = SimulationRunStatus.RUNNING, updatedAt = DateTimeUtils.nowEpochMillis()))
        }

        return runCatching {
            val result = simulationEngine.runFull(scenario, baseline, iterations) { progress ->
                setProgress(workDataOf(KEY_PROGRESS to progress))
            }
            val resultJson = json.encodeToString(result)

            scenarioRepository.getRun(runId)?.let { run ->
                scenarioRepository.upsertRun(
                    run.copy(
                        status = SimulationRunStatus.COMPLETED,
                        completedAtEpochMillis = DateTimeUtils.nowEpochMillis(),
                        resultJson = resultJson,
                        updatedAt = DateTimeUtils.nowEpochMillis(),
                    ),
                )
            }
            Result.success(workDataOf(KEY_RUN_ID to runId))
        }.getOrElse {
            scenarioRepository.getRun(runId)?.let { run ->
                scenarioRepository.upsertRun(run.copy(status = SimulationRunStatus.FAILED, updatedAt = DateTimeUtils.nowEpochMillis()))
            }
            Result.failure()
        }
    }

    companion object {
        const val KEY_RUN_ID = "run_id"
        const val KEY_SCENARIO_JSON = "scenario_json"
        const val KEY_BASELINE_JSON = "baseline_json"
        const val KEY_ITERATIONS = "iterations"
        const val KEY_PROGRESS = "progress"

        fun newRunEntity(scenarioId: String, iterations: Int, seed: Long): SimulationRunEntity = SimulationRunEntity(
            id = IdGenerator.newId(),
            scenarioId = scenarioId,
            iterations = iterations,
            startedAtEpochMillis = DateTimeUtils.nowEpochMillis(),
            completedAtEpochMillis = null,
            status = SimulationRunStatus.PENDING,
            resultJson = null,
            seed = seed,
            createdAt = DateTimeUtils.nowEpochMillis(),
            updatedAt = DateTimeUtils.nowEpochMillis(),
            source = DataSource.SIMULATION,
            version = 1,
        )
    }
}
