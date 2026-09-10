package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.domain.model.RecommendationStatus
import com.eon.futuresimulator.domain.model.ScenarioType
import com.eon.futuresimulator.domain.model.SimulationRunStatus
import com.eon.futuresimulator.core.util.DataSource

/**
 * A Scenario is a *named, versioned configuration* — not a result. [parametersJson] is
 * the serialized [com.eon.futuresimulator.simulation.model.ScenarioConfig]; the engine
 * re-derives every output each time it runs so results are always reproducible.
 */
@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val scenarioType: ScenarioType,
    val parametersJson: String,
    val horizonDays: Int,
    val randomSeed: Long,
    val baselineSnapshotId: String?,
    val isBaseline: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "simulation_runs", indices = [Index("scenarioId")])
data class SimulationRunEntity(
    @PrimaryKey val id: String,
    val scenarioId: String,
    val iterations: Int,
    val startedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val status: SimulationRunStatus,
    /** Serialized [com.eon.futuresimulator.simulation.model.SimulationResult], null until COMPLETED. */
    val resultJson: String?,
    val seed: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "assumptions", indices = [Index("simulationRunId")])
data class AssumptionEntity(
    @PrimaryKey val id: String,
    val simulationRunId: String,
    val key: String,
    val value: String,
    val explanation: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "forecasts", indices = [Index("goalId"), Index("simulationRunId")])
data class ForecastEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val simulationRunId: String,
    val metricName: String,
    val p10: Double,
    val p50: Double,
    val p90: Double,
    val unit: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "recommendations")
data class RecommendationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val explanation: String,
    val relatedGoalId: String?,
    val expectedImpact: String,
    val assumption: String,
    val status: RecommendationStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
