package com.eon.futuresimulator.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.eon.futuresimulator.domain.model.CheckInStatus
import com.eon.futuresimulator.domain.model.HabitFrequencyType
import com.eon.futuresimulator.domain.model.Priority
import com.eon.futuresimulator.domain.model.TaskStatus
import com.eon.futuresimulator.core.util.DataSource

@Entity(tableName = "tasks", indices = [Index("goalId"), Index("scheduledDateEpochMillis")])
data class TaskEntity(
    @PrimaryKey val id: String,
    val goalId: String?,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val priority: Priority,
    val deadlineEpochMillis: Long?,
    val status: TaskStatus,
    val scheduledDateEpochMillis: Long?,
    val isRecurring: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "habits", indices = [Index("goalId")])
data class HabitEntity(
    @PrimaryKey val id: String,
    val goalId: String?,
    val title: String,
    val description: String,
    val frequencyType: HabitFrequencyType,
    val targetDaysPerWeek: Int,
    /** Bitmask, bit 0 = Monday ... bit 6 = Sunday. Used when frequencyType == CUSTOM. */
    val customScheduleMask: Int,
    val reminderTimeMinutesOfDay: Int?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)

@Entity(tableName = "habit_check_ins", indices = [Index("habitId"), Index("dateEpochMillis")])
data class HabitCheckInEntity(
    @PrimaryKey val id: String,
    val habitId: String,
    val dateEpochMillis: Long,
    val status: CheckInStatus,
    val note: String,
    val createdAt: Long,
    val updatedAt: Long,
    val source: DataSource,
    val version: Int,
)
