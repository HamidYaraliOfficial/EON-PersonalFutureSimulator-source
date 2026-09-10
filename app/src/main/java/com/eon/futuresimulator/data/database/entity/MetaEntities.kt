package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.domain.model.ReviewPeriodType
import com.eon.futuresimulator.core.util.DataSource

/** Future Journal — kept in Dual Reality Mode: entries are tagged REAL or SIMULATED and never merged. */
@Entity(tableName = "journal_entries", indices = [Index("scenarioId")])
data class JournalEntryEntity(
    @PrimaryKey val id: String,
    val scenarioId: String?,
    val isSimulated: Boolean,
    val dateEpochMillis: Long,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

/** A point-in-time capture of Goals/Habits/Tasks/Calendar so later Simulations can branch from it. */
@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey val id: String,
    val label: String,
    val snapshotJson: String,
    val takenAtEpochMillis: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

/** Reality vs Simulation: stores a single forecast-vs-actual comparison point. Never a fabricated accuracy number. */
@Entity(tableName = "review_records", indices = [Index("simulationRunId")])
data class ReviewRecordEntity(
    @PrimaryKey val id: String,
    val simulationRunId: String,
    val periodType: ReviewPeriodType,
    val metricName: String,
    val forecastValue: Double,
    val actualValue: Double,
    val forecastErrorPercent: Double,
    val reviewedAtEpochMillis: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
