package com.eon.futuresimulator.domain.model

/** Shared enums used across the domain layer, Room entities (via Converters) and UI. */

enum class GoalCategory { LEARNING, CAREER, FITNESS, PERSONAL_PROJECT, FINANCE, READING, PRODUCTIVITY, CUSTOM }

enum class GoalHorizon { SHORT_TERM, MEDIUM_TERM, LONG_TERM }

enum class Priority { LOW, MEDIUM, HIGH, CRITICAL }

enum class GoalStatus { ACTIVE, PAUSED, COMPLETED, ABANDONED }

enum class TaskStatus { TODO, IN_PROGRESS, DONE, SKIPPED }

enum class HabitFrequencyType { DAILY, WEEKLY, CUSTOM }

enum class CheckInStatus { COMPLETED, SKIPPED, MISSED }

enum class ActivityType { WORKOUT, WALK, SPORT, STRETCHING, OTHER }

enum class TimeOfDay { MORNING, AFTERNOON, EVENING, NIGHT }

enum class ScenarioType {
    DAILY_STUDY_30MIN, DAILY_STUDY_2H, WORKOUT_4X_WEEK, WORK_OVERTIME, SLEEP_8H,
    REDUCE_DISTRACTIONS, READ_20_PAGES, LEARN_PROGRAMMING_1H, INCREASE_TASK_LOAD,
    FIXED_SCHEDULE, CUSTOM,
}

enum class SimulationRunStatus { PENDING, RUNNING, COMPLETED, FAILED, CANCELLED }

enum class RecommendationStatus { NEW, ACCEPTED, DISMISSED, APPLIED }

/** Output of the Feasibility Analyzer — always explainable, never a bare verdict. */
enum class FeasibilityLevel { FEASIBLE, CHALLENGING, UNREALISTIC }

enum class RiskSeverity { LOW, MEDIUM, HIGH, CRITICAL }

enum class RiskType {
    OVERCOMMITMENT, DEADLINE_COLLISION, HABIT_OVERLOAD, INSUFFICIENT_TIME,
    LOW_CONSISTENCY, TASK_BACKLOG,
}

enum class ReviewPeriodType { DAILY, WEEKLY, MONTHLY }

/** Which statistical shape a stochastic variable in the Uncertainty Engine follows. */
enum class DistributionType { NORMAL, LOG_NORMAL, UNIFORM, BERNOULLI }

/** Dual Reality Mode — never let a REAL and SIMULATED value be mistaken for one another. */
enum class RealityMode { REAL, SIMULATED }
