package com.eon.futuresimulator.core.di

import com.eon.futuresimulator.data.repository.*
import com.eon.futuresimulator.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository
    @Binds @Singleton abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository
    @Binds @Singleton abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository
    @Binds @Singleton abstract fun bindCalendarRepository(impl: CalendarRepositoryImpl): CalendarRepository
    @Binds @Singleton abstract fun bindRoutineRepository(impl: RoutineRepositoryImpl): RoutineRepository
    @Binds @Singleton abstract fun bindLifeDataRepository(impl: LifeDataRepositoryImpl): LifeDataRepository
    @Binds @Singleton abstract fun bindProgressRepository(impl: ProgressRepositoryImpl): ProgressRepository
    @Binds @Singleton abstract fun bindScenarioRepository(impl: ScenarioRepositoryImpl): ScenarioRepository
    @Binds @Singleton abstract fun bindJournalSnapshotRepository(impl: JournalSnapshotRepositoryImpl): JournalSnapshotRepository
}
