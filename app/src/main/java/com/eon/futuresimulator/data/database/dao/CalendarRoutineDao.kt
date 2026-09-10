package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.CalendarEventEntity
import com.eon.futuresimulator.data.database.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE startEpochMillis BETWEEN :fromMillis AND :toMillis ORDER BY startEpochMillis ASC")
    fun observeInRange(fromMillis: Long, toMillis: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE startEpochMillis BETWEEN :fromMillis AND :toMillis ORDER BY startEpochMillis ASC")
    suspend fun getInRangeOnce(fromMillis: Long, toMillis: Long): List<CalendarEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE source = 'CALENDAR_SYNC'")
    suspend fun clearSyncedCache()

    @Query("SELECT * FROM calendar_events")
    suspend fun getAllOnce(): List<CalendarEventEntity>
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines")
    fun observeAll(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines")
    suspend fun getAllOnce(): List<RoutineEntity>

    @Query("SELECT * FROM routines WHERE dayOfWeekMask = :mask LIMIT 1")
    suspend fun getForDayMask(mask: Int): RoutineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(routines: List<RoutineEntity>)
}
