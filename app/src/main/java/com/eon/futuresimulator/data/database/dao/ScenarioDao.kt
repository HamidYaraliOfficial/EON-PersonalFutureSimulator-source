package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.*
import com.eon.futuresimulator.domain.model.SimulationRunStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ScenarioEntity>>

    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun getById(id: String): ScenarioEntity?

    @Query("SELECT * FROM scenarios WHERE isBaseline = 1 LIMIT 1")
    suspend fun getBaseline(): ScenarioEntity?

    @Query("SELECT * FROM scenarios")
    suspend fun getAllOnce(): List<ScenarioEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ScenarioEntity)

    @Query("DELETE FROM scenarios WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SimulationRunDao {
    @Query("SELECT * FROM simulation_runs WHERE scenarioId = :scenarioId ORDER BY startedAtEpochMillis DESC")
    fun observeForScenario(scenarioId: String): Flow<List<SimulationRunEntity>>

    @Query("SELECT * FROM simulation_runs ORDER BY startedAtEpochMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<SimulationRunEntity>>

    @Query("SELECT * FROM simulation_runs WHERE id = :id")
    suspend fun getById(id: String): SimulationRunEntity?

    @Query("SELECT * FROM simulation_runs WHERE id = :id")
    fun observeById(id: String): Flow<SimulationRunEntity?>

    @Query("SELECT * FROM simulation_runs")
    suspend fun getAllOnce(): List<SimulationRunEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SimulationRunEntity)

    @Query("UPDATE simulation_runs SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: SimulationRunStatus, updatedAt: Long)

    @Query("DELETE FROM simulation_runs WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface AssumptionDao {
    @Query("SELECT * FROM assumptions WHERE simulationRunId = :runId")
    fun observeForRun(runId: String): Flow<List<AssumptionEntity>>

    @Query("SELECT * FROM assumptions WHERE simulationRunId = :runId")
    suspend fun getForRunOnce(runId: String): List<AssumptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<AssumptionEntity>)
}

@Dao
interface ForecastDao {
    @Query("SELECT * FROM forecasts WHERE goalId = :goalId ORDER BY createdAt DESC")
    fun observeForGoal(goalId: String): Flow<List<ForecastEntity>>

    @Query("SELECT * FROM forecasts WHERE simulationRunId = :runId")
    suspend fun getForRunOnce(runId: String): List<ForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ForecastEntity>)

    @Query("SELECT * FROM forecasts")
    suspend fun getAllOnce(): List<ForecastEntity>
}

@Dao
interface RecommendationDao {
    @Query("SELECT * FROM recommendations WHERE status = 'NEW' ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<RecommendationEntity>>

    @Query("SELECT * FROM recommendations ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<RecommendationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecommendationEntity)

    @Query("SELECT * FROM recommendations")
    suspend fun getAllOnce(): List<RecommendationEntity>
}
