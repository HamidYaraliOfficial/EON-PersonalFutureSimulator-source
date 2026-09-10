package com.eon.futuresimulator.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.eon.futuresimulator.data.database.dao.*
import com.eon.futuresimulator.data.database.entity.*

/**
 * EON is Local-First: every entity below lives only in this on-device Room database
 * unless the user explicitly enables Cloud Sync in the Privacy Center. Bumping
 * [version] always ships alongside a real Migration in [EonDatabaseMigrations] —
 * destructive fallback is intentionally NOT configured (see DatabaseModule).
 */
@Database(
    entities = [
        GoalEntity::class, GoalMilestoneEntity::class,
        TaskEntity::class, HabitEntity::class, HabitCheckInEntity::class,
        CalendarEventEntity::class, RoutineEntity::class,
        StudySessionEntity::class, WorkSessionEntity::class, SleepRecordEntity::class,
        ActivityRecordEntity::class, FinancialInputEntity::class,
        EnergyLevelEntity::class, MoodEntity::class,
        StreakEntity::class, ProgressMetricEntity::class,
        ScenarioEntity::class, SimulationRunEntity::class, AssumptionEntity::class,
        ForecastEntity::class, RecommendationEntity::class,
        JournalEntryEntity::class, SnapshotEntity::class, ReviewRecordEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class EonDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun taskDao(): TaskDao
    abstract fun habitDao(): HabitDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun routineDao(): RoutineDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun workSessionDao(): WorkSessionDao
    abstract fun sleepRecordDao(): SleepRecordDao
    abstract fun activityRecordDao(): ActivityRecordDao
    abstract fun financialInputDao(): FinancialInputDao
    abstract fun energyMoodDao(): EnergyMoodDao
    abstract fun streakDao(): StreakDao
    abstract fun progressMetricDao(): ProgressMetricDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun simulationRunDao(): SimulationRunDao
    abstract fun assumptionDao(): AssumptionDao
    abstract fun forecastDao(): ForecastDao
    abstract fun recommendationDao(): RecommendationDao
    abstract fun journalDao(): JournalDao
    abstract fun snapshotDao(): SnapshotDao
    abstract fun reviewRecordDao(): ReviewRecordDao

    companion object {
        const val DATABASE_NAME = "eon.db"
    }
}
