package com.eon.futuresimulator.core.di

import android.content.Context
import androidx.room.Room
import com.eon.futuresimulator.data.database.ALL_MIGRATIONS
import com.eon.futuresimulator.data.database.EonDatabase
import com.eon.futuresimulator.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideEonDatabase(@ApplicationContext context: Context): EonDatabase =
        Room.databaseBuilder(context, EonDatabase::class.java, EonDatabase.DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            // No fallbackToDestructiveMigration: EON is Local-First and a user's Goals,
            // Habits and Simulation history are irreplaceable — every schema bump ships
            // with a real Migration instead.
            .build()

    @Provides fun provideGoalDao(db: EonDatabase): GoalDao = db.goalDao()
    @Provides fun provideTaskDao(db: EonDatabase): TaskDao = db.taskDao()
    @Provides fun provideHabitDao(db: EonDatabase): HabitDao = db.habitDao()
    @Provides fun provideCalendarEventDao(db: EonDatabase): CalendarEventDao = db.calendarEventDao()
    @Provides fun provideRoutineDao(db: EonDatabase): RoutineDao = db.routineDao()
    @Provides fun provideStudySessionDao(db: EonDatabase): StudySessionDao = db.studySessionDao()
    @Provides fun provideWorkSessionDao(db: EonDatabase): WorkSessionDao = db.workSessionDao()
    @Provides fun provideSleepRecordDao(db: EonDatabase): SleepRecordDao = db.sleepRecordDao()
    @Provides fun provideActivityRecordDao(db: EonDatabase): ActivityRecordDao = db.activityRecordDao()
    @Provides fun provideFinancialInputDao(db: EonDatabase): FinancialInputDao = db.financialInputDao()
    @Provides fun provideEnergyMoodDao(db: EonDatabase): EnergyMoodDao = db.energyMoodDao()
    @Provides fun provideStreakDao(db: EonDatabase): StreakDao = db.streakDao()
    @Provides fun provideProgressMetricDao(db: EonDatabase): ProgressMetricDao = db.progressMetricDao()
    @Provides fun provideScenarioDao(db: EonDatabase): ScenarioDao = db.scenarioDao()
    @Provides fun provideSimulationRunDao(db: EonDatabase): SimulationRunDao = db.simulationRunDao()
    @Provides fun provideAssumptionDao(db: EonDatabase): AssumptionDao = db.assumptionDao()
    @Provides fun provideForecastDao(db: EonDatabase): ForecastDao = db.forecastDao()
    @Provides fun provideRecommendationDao(db: EonDatabase): RecommendationDao = db.recommendationDao()
    @Provides fun provideJournalDao(db: EonDatabase): JournalDao = db.journalDao()
    @Provides fun provideSnapshotDao(db: EonDatabase): SnapshotDao = db.snapshotDao()
    @Provides fun provideReviewRecordDao(db: EonDatabase): ReviewRecordDao = db.reviewRecordDao()
}
