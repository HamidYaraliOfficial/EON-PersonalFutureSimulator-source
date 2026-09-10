package com.eon.futuresimulator

import com.eon.futuresimulator.simulation.engine.FeasibilityAnalyzer
import com.eon.futuresimulator.simulation.engine.RiskEngine
import com.eon.futuresimulator.simulation.engine.SimulationEngine
import com.eon.futuresimulator.simulation.engine.TimeBudgetEngine
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.GoalBaseline
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class SimulationEngineTest {

    private lateinit var engine: SimulationEngine

    private fun baseline(goalTarget: Double = 100.0) = BaselineState(
        referenceDateEpochMillis = 1_700_000_000_000L,
        availableMinutesPerWeekday = (1..7).associateWith { 180 },
        existingTaskBacklogMinutes = 60,
        sleepAvgMinutesPerNight = 420,
        studyAvgMinutesPerDay = 30,
        workAvgMinutesPerDay = 300,
        exerciseSessionsPerWeek = 2,
        goals = listOf(
            GoalBaseline(
                goalId = "g1", title = "Learn Kotlin", currentValue = 0.0, targetValue = goalTarget,
                unit = "%", deadlineEpochMillis = null, observedRatePerMinute = 0.02,
            ),
        ),
        habits = emptyList(),
        sampleSizeDays = 30,
    )

    @Before
    fun setUp() {
        val timeBudgetEngine = TimeBudgetEngine()
        engine = SimulationEngine(timeBudgetEngine, FeasibilityAnalyzer(timeBudgetEngine), RiskEngine())
    }

    @Test
    fun `same seed produces byte-identical deterministic timeline`() {
        val scenario = ScenarioConfig(name = "Test", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 30, randomSeed = 123L, targetGoalId = "g1")
        val base = baseline()

        val run1 = engine.simulateDeterministic(scenario, base)
        val run2 = engine.simulateDeterministic(scenario, base)

        assertThat(run1).isEqualTo(run2)
    }

    @Test
    fun `different seeds produce different trajectories`() {
        val base = baseline()
        val a = engine.simulateDeterministic(ScenarioConfig(name = "A", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 30, randomSeed = 1L, targetGoalId = "g1"), base)
        val b = engine.simulateDeterministic(ScenarioConfig(name = "B", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 30, randomSeed = 2L, targetGoalId = "g1"), base)

        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `timeline has exactly horizonDays points`() {
        val scenario = ScenarioConfig(name = "T", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 45, randomSeed = 5L, targetGoalId = "g1")
        val result = engine.simulateDeterministic(scenario, baseline())
        assertThat(result).hasSize(45)
    }

    @Test
    fun `monte carlo run produces confidence bands with p10 less or equal p50 less or equal p90`() = runTest {
        val scenario = ScenarioConfig(name = "MC", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 20, randomSeed = 7L, targetGoalId = "g1")
        val result = engine.runFull(scenario, baseline(), iterations = 25)
        val band = result.confidenceBands["goalProgressPercent"]!!
        band.forEach { point ->
            assertThat(point.p10).isAtMost(point.p50 + 1e-9)
            assertThat(point.p50).isAtMost(point.p90 + 1e-9)
        }
    }

    @Test
    fun `no goals still produces a valid horizon-length timeline`() {
        val emptyGoalsBaseline = baseline().copy(goals = emptyList())
        val scenario = ScenarioConfig(name = "NoGoal", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, horizonDays = 10, randomSeed = 9L)
        val result = engine.simulateDeterministic(scenario, emptyGoalsBaseline)
        assertThat(result).hasSize(10)
    }
}
