package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.core.util.DataSource

@Entity(tableName = "streaks", indices = [Index("habitId", unique = true)])
data class StreakEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val currentStreak: Int,
    val longestStreak: Int,
    val completionRate: Double,
    val lastCheckInDateEpochMillis: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "progress_metrics", indices = [Index("goalId"), Index("dateEpochMillis")])
data class ProgressMetricEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val dateEpochMillis: Long,
    val value: Double,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
