package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.domain.model.ActivityType
import com.eon.futuresimulator.domain.model.TimeOfDay
import com.eon.futuresimulator.core.util.DataSource

@Entity(tableName = "study_sessions", indices = [Index("startEpochMillis")])
data class StudySessionEntity(
    @PrimaryKey val id: String,
    val subject: String,
    val startEpochMillis: Long,
    val durationMinutes: Int,
    val focusRating: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "work_sessions", indices = [Index("startEpochMillis")])
data class WorkSessionEntity(
    @PrimaryKey val id: String,
    val project: String,
    val startEpochMillis: Long,
    val durationMinutes: Int,
    val isOvertime: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "sleep_records", indices = [Index("dateEpochMillis")])
data class SleepRecordEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val sleepStartEpochMillis: Long,
    val sleepEndEpochMillis: Long,
    val durationMinutes: Int,
    val quality: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "activity_records", indices = [Index("dateEpochMillis")])
data class ActivityRecordEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val activityType: ActivityType,
    val durationMinutes: Int,
    val intensity: Int?,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

/** Manual-entry only, and only stored while the Privacy Center "Financial Input" toggle is on. */
@Entity(tableName = "financial_inputs", indices = [Index("dateEpochMillis")])
data class FinancialInputEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val category: String,
    val amount: Double,
    val currency: String,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "energy_levels", indices = [Index("dateEpochMillis")])
data class EnergyLevelEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val level: Int,
    val timeOfDay: TimeOfDay,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "moods", indices = [Index("dateEpochMillis")])
data class MoodEntity(
    @PrimaryKey val id: String,
    val dateEpochMillis: Long,
    val moodScore: Int,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
