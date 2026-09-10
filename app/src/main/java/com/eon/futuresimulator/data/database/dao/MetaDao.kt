package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.JournalEntryEntity
import com.eon.futuresimulator.data.database.entity.ReviewRecordEntity
import com.eon.futuresimulator.data.database.entity.SnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY dateEpochMillis DESC")
    fun observeAll(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isSimulated = :simulated ORDER BY dateEpochMillis DESC")
    fun observeByReality(simulated: Boolean): Flow<List<JournalEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: JournalEntryEntity)

    @Query("SELECT * FROM journal_entries")
    suspend fun getAllOnce(): List<JournalEntryEntity>

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM snapshots ORDER BY takenAtEpochMillis DESC")
    fun observeAll(): Flow<List<SnapshotEntity>>

    @Query("SELECT * FROM snapshots WHERE id = :id")
    suspend fun getById(id: String): SnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SnapshotEntity)

    @Query("SELECT * FROM snapshots")
    suspend fun getAllOnce(): List<SnapshotEntity>

    @Query("DELETE FROM snapshots WHERE id = :id")
    suspend fun deleteById(id: String)
}

@Dao
interface ReviewRecordDao {
    @Query("SELECT * FROM review_records ORDER BY reviewedAtEpochMillis DESC")
    fun observeAll(): Flow<List<ReviewRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<ReviewRecordEntity>)

    @Query("SELECT * FROM review_records")
    suspend fun getAllOnce(): List<ReviewRecordEntity>
}
