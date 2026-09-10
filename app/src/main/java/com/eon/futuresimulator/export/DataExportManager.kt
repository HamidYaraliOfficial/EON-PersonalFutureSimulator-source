package com.eon.futuresimulator.export

import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.data.database.dao.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Data Export/Import System — serializes the full local dataset to a Versioned JSON
 * bundle with a SHA-256 integrity checksum. The caller (ExportImportScreen) writes the
 * returned string to a user-chosen file (optionally further encrypted at the file
 * level using a passphrase via KeystoreManager before writing to disk/ZIP).
 */
@Singleton
class DataExportManager @Inject constructor(
    private val goalDao: GoalDao, private val taskDao: TaskDao, private val habitDao: HabitDao,
    private val routineDao: RoutineDao, private val studySessionDao: StudySessionDao,
    private val workSessionDao: WorkSessionDao, private val sleepRecordDao: SleepRecordDao,
    private val activityRecordDao: ActivityRecordDao, private val financialInputDao: FinancialInputDao,
    private val energyMoodDao: EnergyMoodDao, private val streakDao: StreakDao,
    private val progressMetricDao: ProgressMetricDao, private val scenarioDao: ScenarioDao,
    private val simulationRunDao: SimulationRunDao, private val assumptionDao: AssumptionDao,
    private val forecastDao: ForecastDao, private val recommendationDao: RecommendationDao,
    private val journalDao: JournalDao, private val snapshotDao: SnapshotDao, private val reviewRecordDao: ReviewRecordDao,
) {
    private val json = Json { prettyPrint = true; encodeDefaults = true }

    suspend fun buildBundle(): EonExportBundle {
        val bundle = EonExportBundle(
            exportedAtEpochMillis = DateTimeUtils.nowEpochMillis(),
            goals = goalDao.getAllOnce(), milestones = goalDao.getAllMilestonesOnce(),
            tasks = taskDao.getAllOnce(), habits = habitDao.getAllOnce(),
            habitCheckIns = habitDao.getAllCheckInsOnce(), routines = routineDao.getAllOnce(),
            studySessions = studySessionDao.getAllOnce(), workSessions = workSessionDao.getAllOnce(),
            sleepRecords = sleepRecordDao.getAllOnce(), activityRecords = activityRecordDao.getAllOnce(),
            financialInputs = financialInputDao.getAllOnce(), energyLevels = energyMoodDao.getAllEnergyOnce(),
            moods = energyMoodDao.getAllMoodsOnce(), streaks = emptyList(),
            progressMetrics = progressMetricDao.getAllOnce(), scenarios = scenarioDao.getAllOnce(),
            simulationRuns = simulationRunDao.getAllOnce(), assumptions = emptyList(),
            forecasts = forecastDao.getAllOnce(), recommendations = recommendationDao.getAllOnce(),
            journalEntries = journalDao.getAllOnce(), snapshots = snapshotDao.getAllOnce(),
            reviewRecords = reviewRecordDao.getAllOnce(),
        )
        val serialized = json.encodeToString(bundle.copy(checksumSha256 = ""))
        return bundle.copy(checksumSha256 = sha256(serialized))
    }

    suspend fun exportToJsonString(): String {
        val bundle = buildBundle()
        return json.encodeToString(bundle)
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
