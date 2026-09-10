package com.eon.futuresimulator.data.database.dao

import androidx.room.*
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.data.database.entity.GoalMilestoneEntity
import com.eon.futuresimulator.domain.model.GoalStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY priority DESC, createdAt DESC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY priority DESC")
    fun observeByStatus(status: GoalStatus): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeById(id: String): Flow<GoalEntity?>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: String): GoalEntity?

    @Query("SELECT * FROM goals")
    suspend fun getAllOnce(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM goals")
    suspend fun count(): Int

    // --- Milestones ---
    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    fun observeMilestones(goalId: String): Flow<List<GoalMilestoneEntity>>

    @Query("SELECT * FROM goal_milestones WHERE goalId = :goalId ORDER BY orderIndex ASC")
    suspend fun getMilestonesOnce(goalId: String): List<GoalMilestoneEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestone(milestone: GoalMilestoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMilestones(milestones: List<GoalMilestoneEntity>)

    @Query("DELETE FROM goal_milestones WHERE id = :id")
    suspend fun deleteMilestone(id: String)

    @Query("SELECT * FROM goal_milestones")
    suspend fun getAllMilestonesOnce(): List<GoalMilestoneEntity>
}
