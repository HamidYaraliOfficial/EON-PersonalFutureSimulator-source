package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.domain.model.GoalCategory
import com.eon.futuresimulator.domain.model.GoalHorizon
import com.eon.futuresimulator.domain.model.GoalStatus
import com.eon.futuresimulator.domain.model.Priority
import com.eon.futuresimulator.core.util.DataSource

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: GoalCategory,
    val priority: Priority,
    val horizon: GoalHorizon,
    val deadlineEpochMillis: Long?,
    val targetValue: Double,
    val currentValue: Double,
    val unit: String,
    val status: GoalStatus,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(entity = GoalEntity::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("goalId")],
)
data class GoalMilestoneEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val title: String,
    val targetValue: Double,
    val targetDateEpochMillis: Long,
    val achievedDateEpochMillis: Long?,
    val isAchieved: Boolean,
    val orderIndex: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
