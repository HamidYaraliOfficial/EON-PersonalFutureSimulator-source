package com.eon.futuresimulator.data.database

import androidx.room.TypeConverter
import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.domain.model.*

/** Every enum is stored as its (readable, migration-friendly) name string. */
class Converters {
    @TypeConverter fun fromDataSource(v: DataSource): String = v.name
    @TypeConverter fun toDataSource(v: String): DataSource = DataSource.valueOf(v)

    @TypeConverter fun fromGoalCategory(v: GoalCategory): String = v.name
    @TypeConverter fun toGoalCategory(v: String): GoalCategory = GoalCategory.valueOf(v)

    @TypeConverter fun fromGoalHorizon(v: GoalHorizon): String = v.name
    @TypeConverter fun toGoalHorizon(v: String): GoalHorizon = GoalHorizon.valueOf(v)

    @TypeConverter fun fromPriority(v: Priority): String = v.name
    @TypeConverter fun toPriority(v: String): Priority = Priority.valueOf(v)

    @TypeConverter fun fromGoalStatus(v: GoalStatus): String = v.name
    @TypeConverter fun toGoalStatus(v: String): GoalStatus = GoalStatus.valueOf(v)

    @TypeConverter fun fromTaskStatus(v: TaskStatus): String = v.name
    @TypeConverter fun toTaskStatus(v: String): TaskStatus = TaskStatus.valueOf(v)

    @TypeConverter fun fromHabitFrequencyType(v: HabitFrequencyType): String = v.name
    @TypeConverter fun toHabitFrequencyType(v: String): HabitFrequencyType = HabitFrequencyType.valueOf(v)

    @TypeConverter fun fromCheckInStatus(v: CheckInStatus): String = v.name
    @TypeConverter fun toCheckInStatus(v: String): CheckInStatus = CheckInStatus.valueOf(v)

    @TypeConverter fun fromActivityType(v: ActivityType): String = v.name
    @TypeConverter fun toActivityType(v: String): ActivityType = ActivityType.valueOf(v)

    @TypeConverter fun fromTimeOfDay(v: TimeOfDay): String = v.name
    @TypeConverter fun toTimeOfDay(v: String): TimeOfDay = TimeOfDay.valueOf(v)

    @TypeConverter fun fromScenarioType(v: ScenarioType): String = v.name
    @TypeConverter fun toScenarioType(v: String): ScenarioType = ScenarioType.valueOf(v)

    @TypeConverter fun fromSimulationRunStatus(v: SimulationRunStatus): String = v.name
    @TypeConverter fun toSimulationRunStatus(v: String): SimulationRunStatus = SimulationRunStatus.valueOf(v)

    @TypeConverter fun fromRecommendationStatus(v: RecommendationStatus): String = v.name
    @TypeConverter fun toRecommendationStatus(v: String): RecommendationStatus = RecommendationStatus.valueOf(v)

    @TypeConverter fun fromReviewPeriodType(v: ReviewPeriodType): String = v.name
    @TypeConverter fun toReviewPeriodType(v: String): ReviewPeriodType = ReviewPeriodType.valueOf(v)
}
