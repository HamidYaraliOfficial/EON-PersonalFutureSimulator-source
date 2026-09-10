package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.core.util.DataSource

/** Local cache of Android Calendar events (read-only mirror) so analysis works offline. */
@Entity(tableName = "calendar_events", indices = [Index("startEpochMillis"), Index("externalCalendarId")])
data class CalendarEventEntity(
    @PrimaryKey val id: String,
    val externalEventId: String?,
    val externalCalendarId: String?,
    val title: String,
    val startEpochMillis: Long,
    val endEpochMillis: Long,
    val isAllDay: Boolean,
    val linkedGoalId: String?,
    val linkedTaskId: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

/**
 * Output of the Routine Analyzer: a *derived, observational* weekday pattern
 * (never presented as a certainty — see [derivedConfidence]).
 */
@Entity(tableName = "routines", indices = [Index("dayOfWeekMask")])
data class RoutineEntity(
    @PrimaryKey val id: String,
    val dayOfWeekMask: Int,
    val studyMinutes: Int,
    val workMinutes: Int,
    val sleepMinutes: Int,
    val exerciseMinutes: Int,
    val freeMinutes: Int,
    val derivedConfidence: Double,
    val sampleSize: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
