package com.eon.futuresimulator

import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.simulation.engine.FeasibilityAnalyzer
import com.eon.futuresimulator.simulation.engine.RiskEngine
import com.eon.futuresimulator.simulation.engine.TimeBudgetEngine
import com.eon.futuresimulator.simulation.model.BaselineState
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FeasibilityAndRiskTest {

    private val timeBudgetEngine = TimeBudgetEngine()
    private val feasibilityAnalyzer = FeasibilityAnalyzer(timeBudgetEngine)
    private val riskEngine = RiskEngine()

    @Test
    fun `required time well under available is Feasible`() {
        val result = feasibilityAnalyzer.analyze(requiredMinutesPerDay = 60.0, availableMinutesPerDay = 200.0)
        assertThat(result.level).isEqualTo(FeasibilityLevel.FEASIBLE)
    }

    @Test
    fun `required time near available is Challenging`() {
        val result = feasibilityAnalyzer.analyze(requiredMinutesPerDay = 160.0, availableMinutesPerDay = 200.0)
        assertThat(result.level).isEqualTo(FeasibilityLevel.CHALLENGING)
    }

    @Test
    fun `required time exceeding available is Unrealistic`() {
        val result = feasibilityAnalyzer.analyze(requiredMinutesPerDay = 400.0, availableMinutesPerDay = 200.0)
        assertThat(result.level).isEqualTo(FeasibilityLevel.UNREALISTIC)
    }

    @Test
    fun `overcommitted scenario produces an Overcommitment risk finding`() {
        val baseline = BaselineState(
            referenceDateEpochMillis = 0L, availableMinutesPerWeekday = (1..7).associateWith { 60 },
            existingTaskBacklogMinutes = 0, sleepAvgMinutesPerNight = 420, studyAvgMinutesPerDay = 30,
            workAvgMinutesPerDay = 480, exerciseSessionsPerWeek = 0, goals = emptyList(), habits = emptyList(),
            sampleSizeDays = 0,
        )
        val scenario = ScenarioConfig(name = "Overload", scenarioType = com.eon.futuresimulator.domain.model.ScenarioType.CUSTOM, dailyStudyMinutesDelta = 300)
        val feasibility = feasibilityAnalyzer.analyze(requiredMinutesPerDay = 330.0, availableMinutesPerDay = 60.0)

        val risks = riskEngine.evaluate(scenario, baseline, feasibility)

        assertThat(risks.any { it.type == com.eon.futuresimulator.domain.model.RiskType.OVERCOMMITMENT }).isTrue()
    }
}
