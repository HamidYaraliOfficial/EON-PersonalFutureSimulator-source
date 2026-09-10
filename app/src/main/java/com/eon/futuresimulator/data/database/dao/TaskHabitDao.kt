package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.HabitCheckInEntity
import com.eon.futuresimulator.data.database.entity.HabitEntity
import com.eon.futuresimulator.data.database.entity.TaskEntity
import com.eon.futuresimulator.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY scheduledDateEpochMillis ASC, priority DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE goalId = :goalId ORDER BY scheduledDateEpochMillis ASC")
    fun observeForGoal(goalId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status != :done AND scheduledDateEpochMillis BETWEEN :fromMillis AND :toMillis")
    suspend fun getOpenTasksInRange(fromMillis: Long, toMillis: Long, done: TaskStatus = TaskStatus.DONE): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE status = 'TODO' OR status = 'IN_PROGRESS'")
    suspend fun getBacklogOnce(): List<TaskEntity>

    @Query("SELECT * FROM tasks")
    suspend fun getAllOnce(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(tasks: List<TaskEntity>)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: String): HabitEntity?

    @Query("SELECT * FROM habits")
    suspend fun getAllOnce(): List<HabitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: String)

    // --- Check-ins ---
    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId ORDER BY dateEpochMillis DESC")
    fun observeCheckIns(habitId: String): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId ORDER BY dateEpochMillis DESC")
    suspend fun getCheckInsOnce(habitId: String): List<HabitCheckInEntity>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND dateEpochMillis = :dateEpochMillis LIMIT 1")
    suspend fun getCheckInForDate(habitId: String, dateEpochMillis: Long): HabitCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCheckIn(checkIn: HabitCheckInEntity)

    @Query("SELECT * FROM habit_check_ins")
    suspend fun getAllCheckInsOnce(): List<HabitCheckInEntity>
}
