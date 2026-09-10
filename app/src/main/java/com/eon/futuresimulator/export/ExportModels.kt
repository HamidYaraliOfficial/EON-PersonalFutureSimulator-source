package com.eon.futuresimulator.export

import com.eon.futuresimulator.data.database.entity.*
import kotlinx.serialization.Serializable

/**
 * Versioned Export/Import payload. [schemaVersion] must be bumped whenever a field is
 * added/removed so DataImportManager can detect and reject (or migrate) older/newer
 * files instead of silently corrupting data.
 */
@Serializable
data class EonExportBundle(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAtEpochMillis: Long,
    val checksumSha256: String = "",
    val goals: List<GoalEntity>,
    val milestones: List<GoalMilestoneEntity>,
    val tasks: List<TaskEntity>,
    val habits: List<HabitEntity>,
    val habitCheckIns: List<HabitCheckInEntity>,
    val routines: List<RoutineEntity>,
    val studySessions: List<StudySessionEntity>,
    val workSessions: List<WorkSessionEntity>,
    val sleepRecords: List<SleepRecordEntity>,
    val activityRecords: List<ActivityRecordEntity>,
    val financialInputs: List<FinancialInputEntity>,
    val energyLevels: List<EnergyLevelEntity>,
    val moods: List<MoodEntity>,
    val streaks: List<StreakEntity>,
    val progressMetrics: List<ProgressMetricEntity>,
    val scenarios: List<ScenarioEntity>,
    val simulationRuns: List<SimulationRunEntity>,
    val assumptions: List<AssumptionEntity>,
    val forecasts: List<ForecastEntity>,
    val recommendations: List<RecommendationEntity>,
    val journalEntries: List<JournalEntryEntity>,
    val snapshots: List<SnapshotEntity>,
    val reviewRecords: List<ReviewRecordEntity>,
) {
    companion object { const val CURRENT_SCHEMA_VERSION = 1 }
}
