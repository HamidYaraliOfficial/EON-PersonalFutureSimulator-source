package com.eon.futuresimulator.export

import com.eon.futuresimulator.data.database.dao.*
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed interface ImportResult {
    data class Success(val importedEntityCount: Int) : ImportResult
    data class SchemaMismatch(val fileVersion: Int, val supportedVersion: Int) : ImportResult
    data object IntegrityCheckFailed : ImportResult
    data class Failed(val message: String) : ImportResult
}

/** Companion to [DataExportManager] — validates schema version + checksum before writing anything. */
@Singleton
class DataImportManager @Inject constructor(
    private val goalDao: GoalDao, private val taskDao: TaskDao, private val habitDao: HabitDao,
    private val routineDao: RoutineDao, private val studySessionDao: StudySessionDao,
    private val workSessionDao: WorkSessionDao, private val sleepRecordDao: SleepRecordDao,
    private val activityRecordDao: ActivityRecordDao, private val financialInputDao: FinancialInputDao,
    private val energyMoodDao: EnergyMoodDao, private val progressMetricDao: ProgressMetricDao,
    private val scenarioDao: ScenarioDao, private val simulationRunDao: SimulationRunDao,
    private val forecastDao: ForecastDao, private val recommendationDao: RecommendationDao,
    private val journalDao: JournalDao, private val snapshotDao: SnapshotDao, private val reviewRecordDao: ReviewRecordDao,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun importFromJsonString(raw: String): ImportResult = runCatching {
        val bundle = json.decodeFromString(EonExportBundle.serializer(), raw)

        if (bundle.schemaVersion != EonExportBundle.CURRENT_SCHEMA_VERSION) {
            return ImportResult.SchemaMismatch(bundle.schemaVersion, EonExportBundle.CURRENT_SCHEMA_VERSION)
        }

        val expectedChecksum = bundle.checksumSha256
        val recomputed = sha256(json.encodeToString(EonExportBundle.serializer(), bundle.copy(checksumSha256 = "")))
        if (expectedChecksum.isNotBlank() && expectedChecksum != recomputed) {
            return ImportResult.IntegrityCheckFailed
        }

        var count = 0
        goalDao.upsertAll(bundle.goals); count += bundle.goals.size
        goalDao.upsertMilestones(bundle.milestones); count += bundle.milestones.size
        taskDao.upsertAll(bundle.tasks); count += bundle.tasks.size
        bundle.habits.forEach { habitDao.upsert(it) }; count += bundle.habits.size
        bundle.habitCheckIns.forEach { habitDao.upsertCheckIn(it) }; count += bundle.habitCheckIns.size
        routineDao.upsertAll(bundle.routines); count += bundle.routines.size
        bundle.studySessions.forEach { studySessionDao.upsert(it) }; count += bundle.studySessions.size
        bundle.workSessions.forEach { workSessionDao.upsert(it) }; count += bundle.workSessions.size
        bundle.sleepRecords.forEach { sleepRecordDao.upsert(it) }; count += bundle.sleepRecords.size
        bundle.activityRecords.forEach { activityRecordDao.upsert(it) }; count += bundle.activityRecords.size
        bundle.financialInputs.forEach { financialInputDao.upsert(it) }; count += bundle.financialInputs.size
        bundle.energyLevels.forEach { energyMoodDao.upsertEnergy(it) }; count += bundle.energyLevels.size
        bundle.moods.forEach { energyMoodDao.upsertMood(it) }; count += bundle.moods.size
        bundle.progressMetrics.forEach { progressMetricDao.upsert(it) }; count += bundle.progressMetrics.size
        bundle.scenarios.forEach { scenarioDao.upsert(it) }; count += bundle.scenarios.size
        bundle.simulationRuns.forEach { simulationRunDao.upsert(it) }; count += bundle.simulationRuns.size
        forecastDao.upsertAll(bundle.forecasts); count += bundle.forecasts.size
        bundle.recommendations.forEach { recommendationDao.upsert(it) }; count += bundle.recommendations.size
        bundle.journalEntries.forEach { journalDao.upsert(it) }; count += bundle.journalEntries.size
        bundle.snapshots.forEach { snapshotDao.upsert(it) }; count += bundle.snapshots.size
        reviewRecordDao.upsertAll(bundle.reviewRecords); count += bundle.reviewRecords.size

        ImportResult.Success(count)
    }.getOrElse { ImportResult.Failed(it.message ?: "Unknown import error") }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
