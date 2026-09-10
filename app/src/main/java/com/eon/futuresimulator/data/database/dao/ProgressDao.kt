package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.ProgressMetricEntity
import com.eon.futuresimulator.data.database.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streaks WHERE habitId = :habitId LIMIT 1")
    fun observeForHabit(habitId: String): Flow<StreakEntity?>

    @Query("SELECT * FROM streaks WHERE habitId = :habitId LIMIT 1")
    suspend fun getForHabit(habitId: String): StreakEntity?

    @Query("SELECT * FROM streaks")
    fun observeAll(): Flow<List<StreakEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StreakEntity)
}

@Dao
interface ProgressMetricDao {
    @Query("SELECT * FROM progress_metrics WHERE goalId = :goalId ORDER BY dateEpochMillis ASC")
    fun observeForGoal(goalId: String): Flow<List<ProgressMetricEntity>>

    @Query("SELECT * FROM progress_metrics WHERE goalId = :goalId ORDER BY dateEpochMillis ASC")
    suspend fun getForGoalOnce(goalId: String): List<ProgressMetricEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProgressMetricEntity)

    @Query("SELECT * FROM progress_metrics")
    suspend fun getAllOnce(): List<ProgressMetricEntity>
}
