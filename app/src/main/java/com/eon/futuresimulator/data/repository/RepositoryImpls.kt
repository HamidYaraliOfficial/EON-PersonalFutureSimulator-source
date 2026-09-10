package com.eon.futuresimulator.data.repository

import com.eon.futuresimulator.data.database.dao.*
import com.eon.futuresimulator.data.database.entity.*
import com.eon.futuresimulator.domain.repository.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(private val dao: GoalDao) : GoalRepository {
    override fun observeGoals(): Flow<List<GoalEntity>> = dao.observeAll()
    override fun observeGoal(id: String): Flow<GoalEntity?> = dao.observeById(id)
    override suspend fun getGoal(id: String): GoalEntity? = dao.getById(id)
    override suspend fun getAllGoalsOnce(): List<GoalEntity> = dao.getAllOnce()
    override suspend fun upsertGoal(goal: GoalEntity) = dao.upsert(goal)
    override suspend fun deleteGoal(id: String) = dao.deleteById(id)
    override fun observeMilestones(goalId: String): Flow<List<GoalMilestoneEntity>> = dao.observeMilestones(goalId)
    override suspend fun getMilestonesOnce(goalId: String): List<GoalMilestoneEntity> = dao.getMilestonesOnce(goalId)
    override suspend fun getAllMilestonesOnce(): List<GoalMilestoneEntity> = dao.getAllMilestonesOnce()
    override suspend fun upsertMilestones(milestones: List<GoalMilestoneEntity>) = dao.upsertMilestones(milestones)
    override suspend fun upsertMilestone(milestone: GoalMilestoneEntity) = dao.upsertMilestone(milestone)
}

@Singleton
class TaskRepositoryImpl @Inject constructor(private val dao: TaskDao) : TaskRepository {
    override fun observeTasks(): Flow<List<TaskEntity>> = dao.observeAll()
    override fun observeTasksForGoal(goalId: String): Flow<List<TaskEntity>> = dao.observeForGoal(goalId)
    override suspend fun getAllTasksOnce(): List<TaskEntity> = dao.getAllOnce()
    override suspend fun getBacklogOnce(): List<TaskEntity> = dao.getBacklogOnce()
    override suspend fun upsertTask(task: TaskEntity) = dao.upsert(task)
    override suspend fun upsertTasks(tasks: List<TaskEntity>) = dao.upsertAll(tasks)
    override suspend fun deleteTask(id: String) = dao.deleteById(id)
}

@Singleton
class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val streakDao: StreakDao,
) : HabitRepository {
    override fun observeActiveHabits(): Flow<List<HabitEntity>> = habitDao.observeActive()
    override fun observeAllHabits(): Flow<List<HabitEntity>> = habitDao.observeAll()
    override suspend fun getAllHabitsOnce(): List<HabitEntity> = habitDao.getAllOnce()
    override suspend fun upsertHabit(habit: HabitEntity) = habitDao.upsert(habit)
    override suspend fun deleteHabit(id: String) = habitDao.deleteById(id)
    override fun observeCheckIns(habitId: String): Flow<List<HabitCheckInEntity>> = habitDao.observeCheckIns(habitId)
    override suspend fun getCheckInsOnce(habitId: String): List<HabitCheckInEntity> = habitDao.getCheckInsOnce(habitId)
    override suspend fun getAllCheckInsOnce(): List<HabitCheckInEntity> = habitDao.getAllCheckInsOnce()
    override suspend fun upsertCheckIn(checkIn: HabitCheckInEntity) = habitDao.upsertCheckIn(checkIn)
    override fun observeStreak(habitId: String): Flow<StreakEntity?> = streakDao.observeForHabit(habitId)
    override suspend fun upsertStreak(streak: StreakEntity) = streakDao.upsert(streak)
}

@Singleton
class CalendarRepositoryImpl @Inject constructor(private val dao: CalendarEventDao) : CalendarRepository {
    override fun observeEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<CalendarEventEntity>> = dao.observeInRange(fromMillis, toMillis)
    override suspend fun cacheEvents(events: List<CalendarEventEntity>) {
        dao.clearSyncedCache()
        dao.upsertAll(events)
    }
    override suspend fun getAllOnce(): List<CalendarEventEntity> = dao.getAllOnce()
}

@Singleton
class RoutineRepositoryImpl @Inject constructor(private val dao: RoutineDao) : RoutineRepository {
    override fun observeRoutines(): Flow<List<RoutineEntity>> = dao.observeAll()
    override suspend fun getAllOnce(): List<RoutineEntity> = dao.getAllOnce()
    override suspend fun replaceAll(routines: List<RoutineEntity>) = dao.upsertAll(routines)
}

@Singleton
class LifeDataRepositoryImpl @Inject constructor(
    private val studyDao: StudySessionDao,
    private val workDao: WorkSessionDao,
    private val sleepDao: SleepRecordDao,
    private val activityDao: ActivityRecordDao,
    private val financialDao: FinancialInputDao,
    private val energyMoodDao: EnergyMoodDao,
) : LifeDataRepository {
    override fun observeStudySessions(): Flow<List<StudySessionEntity>> = studyDao.observeAll()
    override fun observeWorkSessions(): Flow<List<WorkSessionEntity>> = workDao.observeAll()
    override fun observeSleepRecords(): Flow<List<SleepRecordEntity>> = sleepDao.observeAll()
    override fun observeActivityRecords(): Flow<List<ActivityRecordEntity>> = activityDao.observeAll()
    override fun observeFinancialInputs(): Flow<List<FinancialInputEntity>> = financialDao.observeAll()
    override fun observeEnergyLevels(): Flow<List<EnergyLevelEntity>> = energyMoodDao.observeEnergy()
    override fun observeMoods(): Flow<List<MoodEntity>> = energyMoodDao.observeMoods()
    override suspend fun upsertStudySession(entity: StudySessionEntity) = studyDao.upsert(entity)
    override suspend fun upsertWorkSession(entity: WorkSessionEntity) = workDao.upsert(entity)
    override suspend fun upsertSleepRecord(entity: SleepRecordEntity) = sleepDao.upsert(entity)
    override suspend fun upsertActivityRecord(entity: ActivityRecordEntity) = activityDao.upsert(entity)
    override suspend fun upsertFinancialInput(entity: FinancialInputEntity) = financialDao.upsert(entity)
    override suspend fun upsertEnergyLevel(entity: EnergyLevelEntity) = energyMoodDao.upsertEnergy(entity)
    override suspend fun upsertMood(entity: MoodEntity) = energyMoodDao.upsertMood(entity)
    override suspend fun getAllOnce(): LifeDataSnapshot = LifeDataSnapshot(
        study = studyDao.getAllOnce(), work = workDao.getAllOnce(), sleep = sleepDao.getAllOnce(),
        activity = activityDao.getAllOnce(), financial = financialDao.getAllOnce(),
        energy = energyMoodDao.getAllEnergyOnce(), mood = energyMoodDao.getAllMoodsOnce(),
    )
}

@Singleton
class ProgressRepositoryImpl @Inject constructor(private val dao: ProgressMetricDao) : ProgressRepository {
    override fun observeProgress(goalId: String): Flow<List<ProgressMetricEntity>> = dao.observeForGoal(goalId)
    override suspend fun getProgressOnce(goalId: String): List<ProgressMetricEntity> = dao.getForGoalOnce(goalId)
    override suspend fun upsertProgress(entity: ProgressMetricEntity) = dao.upsert(entity)
    override suspend fun getAllOnce(): List<ProgressMetricEntity> = dao.getAllOnce()
}

@Singleton
class ScenarioRepositoryImpl @Inject constructor(
    private val scenarioDao: ScenarioDao,
    private val runDao: SimulationRunDao,
    private val assumptionDao: AssumptionDao,
    private val forecastDao: ForecastDao,
    private val recommendationDao: RecommendationDao,
) : ScenarioRepository {
    override fun observeScenarios(): Flow<List<ScenarioEntity>> = scenarioDao.observeAll()
    override suspend fun getScenario(id: String): ScenarioEntity? = scenarioDao.getById(id)
    override suspend fun getBaseline(): ScenarioEntity? = scenarioDao.getBaseline()
    override suspend fun getAllOnce(): List<ScenarioEntity> = scenarioDao.getAllOnce()
    override suspend fun upsertScenario(entity: ScenarioEntity) = scenarioDao.upsert(entity)
    override suspend fun deleteScenario(id: String) = scenarioDao.deleteById(id)

    override fun observeRuns(scenarioId: String): Flow<List<SimulationRunEntity>> = runDao.observeForScenario(scenarioId)
    override fun observeRecentRuns(limit: Int): Flow<List<SimulationRunEntity>> = runDao.observeRecent(limit)
    override fun observeRun(runId: String): Flow<SimulationRunEntity?> = runDao.observeById(runId)
    override suspend fun getRun(runId: String): SimulationRunEntity? = runDao.getById(runId)
    override suspend fun upsertRun(entity: SimulationRunEntity) = runDao.upsert(entity)

    override suspend fun upsertAssumptions(entities: List<AssumptionEntity>) = assumptionDao.upsertAll(entities)
    override fun observeAssumptions(runId: String): Flow<List<AssumptionEntity>> = assumptionDao.observeForRun(runId)

    override suspend fun upsertForecasts(entities: List<ForecastEntity>) = forecastDao.upsertAll(entities)
    override fun observeForecasts(goalId: String): Flow<List<ForecastEntity>> = forecastDao.observeForGoal(goalId)

    override fun observeActiveRecommendations(): Flow<List<RecommendationEntity>> = recommendationDao.observeActive()
    override suspend fun upsertRecommendation(entity: RecommendationEntity) = recommendationDao.upsert(entity)
}

@Singleton
class JournalSnapshotRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val snapshotDao: SnapshotDao,
    private val reviewRecordDao: ReviewRecordDao,
) : JournalSnapshotRepository {
    override fun observeJournal(): Flow<List<JournalEntryEntity>> = journalDao.observeAll()
    override suspend fun upsertJournalEntry(entity: JournalEntryEntity) = journalDao.upsert(entity)
    override fun observeSnapshots(): Flow<List<SnapshotEntity>> = snapshotDao.observeAll()
    override suspend fun upsertSnapshot(entity: SnapshotEntity) = snapshotDao.upsert(entity)
    override suspend fun getSnapshot(id: String): SnapshotEntity? = snapshotDao.getById(id)
    override fun observeReviewRecords(): Flow<List<ReviewRecordEntity>> = reviewRecordDao.observeAll()
    override suspend fun upsertReviewRecords(entities: List<ReviewRecordEntity>) = reviewRecordDao.upsertAll(entities)
}
