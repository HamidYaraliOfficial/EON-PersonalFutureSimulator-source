package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY startEpochMillis DESC")
    fun observeAll(): Flow<List<StudySessionEntity>>
    @Query("SELECT * FROM study_sessions WHERE startEpochMillis BETWEEN :from AND :to")
    suspend fun getInRange(from: Long, to: Long): List<StudySessionEntity>
    @Query("SELECT * FROM study_sessions")
    suspend fun getAllOnce(): List<StudySessionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StudySessionEntity)
    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface WorkSessionDao {
    @Query("SELECT * FROM work_sessions ORDER BY startEpochMillis DESC")
    fun observeAll(): Flow<List<WorkSessionEntity>>
    @Query("SELECT * FROM work_sessions WHERE startEpochMillis BETWEEN :from AND :to")
    suspend fun getInRange(from: Long, to: Long): List<WorkSessionEntity>
    @Query("SELECT * FROM work_sessions")
    suspend fun getAllOnce(): List<WorkSessionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WorkSessionEntity)
    @Query("DELETE FROM work_sessions WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SleepRecordDao {
    @Query("SELECT * FROM sleep_records ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<SleepRecordEntity>>
    @Query("SELECT * FROM sleep_records")
    suspend fun getAllOnce(): List<SleepRecordEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SleepRecordEntity)
    @Query("DELETE FROM sleep_records WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ActivityRecordDao {
    @Query("SELECT * FROM activity_records ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<ActivityRecordEntity>>
    @Query("SELECT * FROM activity_records")
    suspend fun getAllOnce(): List<ActivityRecordEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ActivityRecordEntity)
    @Query("DELETE FROM activity_records WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface FinancialInputDao {
    @Query("SELECT * FROM financial_inputs ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<FinancialInputEntity>>
    @Query("SELECT * FROM financial_inputs")
    suspend fun getAllOnce(): List<FinancialInputEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FinancialInputEntity)
    @Query("DELETE FROM financial_inputs")
    suspend fun deleteAll()
    @Query("DELETE FROM financial_inputs WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface EnergyMoodDao {
    @Query("SELECT * FROM energy_levels ORDER BY dateEpochMillis DESC")
    fun observeEnergy(): Flow<List<EnergyLevelEntity>>
    @Query("SELECT * FROM energy_levels")
    suspend fun getAllEnergyOnce(): List<EnergyLevelEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEnergy(entity: EnergyLevelEntity)

    @Query("SELECT * FROM moods ORDER BY dateEpochMillis DESC")
    fun observeMoods(): Flow<List<MoodEntity>>
    @Query("SELECT * FROM moods")
    suspend fun getAllMoodsOnce(): List<MoodEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMood(entity: MoodEntity)
}
