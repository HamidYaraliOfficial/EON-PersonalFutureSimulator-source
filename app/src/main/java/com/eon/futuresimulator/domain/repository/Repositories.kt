package com.eon.futuresimulator.domain.repository

import com.eon.futuresimulator.data.database.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository contracts (Clean Architecture boundary between domain/UI and Room).
 * Entities double as read models here rather than introducing a parallel domain-model
 * hierarchy for all 24 tables — the mapping layer would add indirection without
 * changing behavior for an app this data-shape-stable. Engines and ViewModels only
 * depend on these interfaces, never on EonDatabase directly.
 */
interface GoalRepository {
    fun observeGoals(): Flow<List<GoalEntity>>
    fun observeGoal(id: String): Flow<GoalEntity?>
    suspend fun getGoal(id: String): GoalEntity?
    suspend fun getAllGoalsOnce(): List<GoalEntity>
    suspend fun upsertGoal(goal: GoalEntity)
    suspend fun deleteGoal(id: String)
    fun observeMilestones(goalId: String): Flow<List<GoalMilestoneEntity>>
    suspend fun getMilestonesOnce(goalId: String): List<GoalMilestoneEntity>
    suspend fun getAllMilestonesOnce(): List<GoalMilestoneEntity>
    suspend fun upsertMilestones(milestones: List<GoalMilestoneEntity>)
    suspend fun upsertMilestone(milestone: GoalMilestoneEntity)
}

interface TaskRepository {
    fun observeTasks(): Flow<List<TaskEntity>>
    fun observeTasksForGoal(goalId: String): Flow<List<TaskEntity>>
    suspend fun getAllTasksOnce(): List<TaskEntity>
    suspend fun getBacklogOnce(): List<TaskEntity>
    suspend fun upsertTask(task: TaskEntity)
    suspend fun upsertTasks(tasks: List<TaskEntity>)
    suspend fun deleteTask(id: String)
}

interface HabitRepository {
    fun observeActiveHabits(): Flow<List<HabitEntity>>
    fun observeAllHabits(): Flow<List<HabitEntity>>
    suspend fun getAllHabitsOnce(): List<HabitEntity>
    suspend fun upsertHabit(habit: HabitEntity)
    suspend fun deleteHabit(id: String)
    fun observeCheckIns(habitId: String): Flow<List<HabitCheckInEntity>>
    suspend fun getCheckInsOnce(habitId: String): List<HabitCheckInEntity>
    suspend fun getAllCheckInsOnce(): List<HabitCheckInEntity>
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)
    fun observeStreak(habitId: String): Flow<StreakEntity?>
    suspend fun upsertStreak(streak: StreakEntity)
}

interface CalendarRepository {
    fun observeEventsInRange(fromMillis: Long, toMillis: Long): Flow<List<CalendarEventEntity>>
    suspend fun cacheEvents(events: List<CalendarEventEntity>)
    suspend fun getAllOnce(): List<CalendarEventEntity>
}

interface RoutineRepository {
    fun observeRoutines(): Flow<List<RoutineEntity>>
    suspend fun getAllOnce(): List<RoutineEntity>
    suspend fun replaceAll(routines: List<RoutineEntity>)
}

interface LifeDataRepository {
    fun observeStudySessions(): Flow<List<StudySessionEntity>>
    fun observeWorkSessions(): Flow<List<WorkSessionEntity>>
    fun observeSleepRecords(): Flow<List<SleepRecordEntity>>
    fun observeActivityRecords(): Flow<List<ActivityRecordEntity>>
    fun observeFinancialInputs(): Flow<List<FinancialInputEntity>>
    fun observeEnergyLevels(): Flow<List<EnergyLevelEntity>>
    fun observeMoods(): Flow<List<MoodEntity>>
    suspend fun upsertStudySession(entity: StudySessionEntity)
    suspend fun upsertWorkSession(entity: WorkSessionEntity)
    suspend fun upsertSleepRecord(entity: SleepRecordEntity)
    suspend fun upsertActivityRecord(entity: ActivityRecordEntity)
    suspend fun upsertFinancialInput(entity: FinancialInputEntity)
    suspend fun upsertEnergyLevel(entity: EnergyLevelEntity)
    suspend fun upsertMood(entity: MoodEntity)
    suspend fun getAllOnce(): LifeDataSnapshot
}

data class LifeDataSnapshot(
    val study: List<StudySessionEntity>, val work: List<WorkSessionEntity>,
    val sleep: List<SleepRecordEntity>, val activity: List<ActivityRecordEntity>,
    val financial: List<FinancialInputEntity>, val energy: List<EnergyLevelEntity>, val mood: List<MoodEntity>,
)

interface ProgressRepository {
    fun observeProgress(goalId: String): Flow<List<ProgressMetricEntity>>
    suspend fun getProgressOnce(goalId: String): List<ProgressMetricEntity>
    suspend fun upsertProgress(entity: ProgressMetricEntity)
    suspend fun getAllOnce(): List<ProgressMetricEntity>
}

interface ScenarioRepository {
    fun observeScenarios(): Flow<List<ScenarioEntity>>
    suspend fun getScenario(id: String): ScenarioEntity?
    suspend fun getBaseline(): ScenarioEntity?
    suspend fun getAllOnce(): List<ScenarioEntity>
    suspend fun upsertScenario(entity: ScenarioEntity)
    suspend fun deleteScenario(id: String)

    fun observeRuns(scenarioId: String): Flow<List<SimulationRunEntity>>
    fun observeRecentRuns(limit: Int = 20): Flow<List<SimulationRunEntity>>
    fun observeRun(runId: String): Flow<SimulationRunEntity?>
    suspend fun getRun(runId: String): SimulationRunEntity?
    suspend fun upsertRun(entity: SimulationRunEntity)

    suspend fun upsertAssumptions(entities: List<AssumptionEntity>)
    fun observeAssumptions(runId: String): Flow<List<AssumptionEntity>>

    suspend fun upsertForecasts(entities: List<ForecastEntity>)
    fun observeForecasts(goalId: String): Flow<List<ForecastEntity>>

    fun observeActiveRecommendations(): Flow<List<RecommendationEntity>>
    suspend fun upsertRecommendation(entity: RecommendationEntity)
}

interface JournalSnapshotRepository {
    fun observeJournal(): Flow<List<JournalEntryEntity>>
    suspend fun upsertJournalEntry(entity: JournalEntryEntity)
    fun observeSnapshots(): Flow<List<SnapshotEntity>>
    suspend fun upsertSnapshot(entity: SnapshotEntity)
    suspend fun getSnapshot(id: String): SnapshotEntity?
    fun observeReviewRecords(): Flow<List<ReviewRecordEntity>>
    suspend fun upsertReviewRecords(entities: List<ReviewRecordEntity>)
}
