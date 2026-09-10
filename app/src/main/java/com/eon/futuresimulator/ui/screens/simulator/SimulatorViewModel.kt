package com.eon.futuresimulator.ui.screens.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.eon.futuresimulator.ai.AIProvider
import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.ScenarioEntity
import com.eon.futuresimulator.domain.model.ScenarioType
import com.eon.futuresimulator.domain.model.SimulationRunStatus
import com.eon.futuresimulator.domain.repository.GoalRepository
import com.eon.futuresimulator.domain.repository.ScenarioRepository
import com.eon.futuresimulator.simulation.engine.BaselineStateBuilder
import com.eon.futuresimulator.simulation.engine.DeviceCapabilityManager
import com.eon.futuresimulator.simulation.engine.SensitivityAnalyzer
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import com.eon.futuresimulator.simulation.model.SensitivityResult
import com.eon.futuresimulator.simulation.model.SimulationResult
import com.eon.futuresimulator.workers.SimulationWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class SimulatorUiState(
    val scenario: ScenarioConfig = ScenarioConfig.baseline(),
    val iterations: Int = 200,
    val maxIterations: Int = 500,
    val isRunning: Boolean = false,
    val progress: Float = 0f,
    val result: SimulationResult? = null,
    val sensitivity: List<SensitivityResult> = emptyList(),
    val aiExplanation: String? = null,
    val availableGoals: List<Pair<String, String>> = emptyList(), // id to title
    val error: String? = null,
)

@HiltViewModel
class SimulatorViewModel @Inject constructor(
    private val workManager: WorkManager,
    private val scenarioRepository: ScenarioRepository,
    private val goalRepository: GoalRepository,
    private val baselineStateBuilder: BaselineStateBuilder,
    private val sensitivityAnalyzer: SensitivityAnalyzer,
    private val deviceCapabilityManager: DeviceCapabilityManager,
    private val aiProvider: AIProvider,
) : ViewModel() {

    private val json = Json { encodeDefaults = true; ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(SimulatorUiState())
    val state: StateFlow<SimulatorUiState> = _state.asStateFlow()

    private var currentWorkId: java.util.UUID? = null
    private var currentBaseline: BaselineState? = null

    init {
        val budget = deviceCapabilityManager.currentBudget()
        _state.update { it.copy(iterations = budget.recommendedIterations, maxIterations = budget.maxIterations) }
        viewModelScope.launch {
            goalRepository.observeGoals().collect { goals ->
                _state.update { it.copy(availableGoals = goals.map { g -> g.id to g.title }) }
            }
        }
    }

    fun updateScenario(transform: (ScenarioConfig) -> ScenarioConfig) {
        _state.update { it.copy(scenario = transform(it.scenario)) }
    }

    fun updateIterations(value: Int) {
        _state.update { it.copy(iterations = value.coerceIn(1, it.maxIterations)) }
    }

    fun applyPreset(type: ScenarioType) {
        val base = _state.value.scenario
        val preset = when (type) {
            ScenarioType.DAILY_STUDY_30MIN -> base.copy(name = "Daily Study 30min", scenarioType = type, dailyStudyMinutesDelta = 30)
            ScenarioType.DAILY_STUDY_2H -> base.copy(name = "Daily Study 2h", scenarioType = type, dailyStudyMinutesDelta = 120)
            ScenarioType.WORKOUT_4X_WEEK -> base.copy(name = "Workout 4x/week", scenarioType = type, weeklyWorkoutSessionsDelta = 4)
            ScenarioType.WORK_OVERTIME -> base.copy(name = "Work Overtime", scenarioType = type, workHoursPerDayDelta = 2.0, workDaysPerWeekDelta = 1)
            ScenarioType.SLEEP_8H -> base.copy(name = "Sleep 8h", scenarioType = type, sleepHoursTarget = 8.0)
            ScenarioType.REDUCE_DISTRACTIONS -> base.copy(name = "Reduce Distractions", scenarioType = type, distractionReductionMinutesPerDay = 30)
            ScenarioType.READ_20_PAGES -> base.copy(name = "Read 20 pages/day", scenarioType = type, readingPagesPerDayDelta = 20)
            ScenarioType.LEARN_PROGRAMMING_1H -> base.copy(name = "Learn Programming 1h/day", scenarioType = type, dailyStudyMinutesDelta = 60)
            ScenarioType.INCREASE_TASK_LOAD -> base.copy(name = "Increase Task Load", scenarioType = type, taskLoadMultiplier = 1.5)
            ScenarioType.FIXED_SCHEDULE -> base.copy(name = "Fixed Schedule (Baseline)", scenarioType = type)
            ScenarioType.CUSTOM -> base.copy(name = "Custom Scenario", scenarioType = type)
        }
        _state.update { it.copy(scenario = preset) }
    }

    fun runSimulation() {
        if (_state.value.isRunning) return
        viewModelScope.launch {
            _state.update { it.copy(isRunning = true, progress = 0f, error = null, result = null, aiExplanation = null) }
            runCatching {
                val scenario = _state.value.scenario
                val baseline = baselineStateBuilder.build(scenario.targetGoalId)
                currentBaseline = baseline

                val scenarioEntity = ScenarioEntity(
                    id = IdGenerator.newId(), name = scenario.name, description = scenario.interventions.joinToString(),
                    scenarioType = scenario.scenarioType, parametersJson = json.encodeToString(scenario),
                    horizonDays = scenario.horizonDays, randomSeed = scenario.randomSeed, baselineSnapshotId = null,
                    isBaseline = scenario.scenarioType == ScenarioType.FIXED_SCHEDULE,
                    createdAt = DateTimeUtils.nowEpochMillis(), updatedAt = DateTimeUtils.nowEpochMillis(),
                    source = DataSource.MANUAL, version = 1,
                )
                scenarioRepository.upsertScenario(scenarioEntity)

                val runEntity = SimulationWorker.newRunEntity(scenarioEntity.id, _state.value.iterations, scenario.randomSeed)
                scenarioRepository.upsertRun(runEntity)

                val request = OneTimeWorkRequestBuilder<SimulationWorker>()
                    .setInputData(
                        workDataOf(
                            SimulationWorker.KEY_RUN_ID to runEntity.id,
                            SimulationWorker.KEY_SCENARIO_JSON to json.encodeToString(scenario),
                            SimulationWorker.KEY_BASELINE_JSON to json.encodeToString(baseline),
                            SimulationWorker.KEY_ITERATIONS to _state.value.iterations,
                        ),
                    )
                    .build()
                currentWorkId = request.id
                workManager.enqueueUniqueWork("simulation_run", ExistingWorkPolicy.REPLACE, request)

                workManager.getWorkInfoByIdFlow(request.id).collect { info ->
                    if (info == null) return@collect
                    val progress = info.progress.getFloat(SimulationWorker.KEY_PROGRESS, _state.value.progress)
                    _state.update { it.copy(progress = progress) }

                    when (info.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            val run = scenarioRepository.getRun(runEntity.id)
                            val resultJson = run?.resultJson
                            val result = resultJson?.let { runCatching { json.decodeFromString(SimulationResult.serializer(), it) }.getOrNull() }
                            val sensitivity = sensitivityAnalyzer.analyze(scenario, baseline)
                            val explanation = result?.let { runCatching { aiProvider.explainScenario(it, sensitivity) }.getOrNull() }
                            _state.update { it.copy(isRunning = false, progress = 1f, result = result, sensitivity = sensitivity, aiExplanation = explanation) }
                            return@collect
                        }
                        WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                            _state.update { it.copy(isRunning = false, error = "Simulation ${info.state.name.lowercase()}.") }
                            return@collect
                        }
                        else -> Unit
                    }
                }
            }.onFailure { e ->
                _state.update { it.copy(isRunning = false, error = e.message ?: "Simulation failed.") }
            }
        }
    }

    fun cancelSimulation() {
        currentWorkId?.let { workManager.cancelWorkById(it) }
        _state.update { it.copy(isRunning = false) }
    }
}
