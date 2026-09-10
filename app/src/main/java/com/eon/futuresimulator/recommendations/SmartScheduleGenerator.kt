package com.eon.futuresimulator.recommendations

import com.eon.futuresimulator.data.database.entity.TaskEntity
import com.eon.futuresimulator.tasks.AllocationPlan
import com.eon.futuresimulator.tasks.TaskTimeAllocationEngine
import javax.inject.Inject
import javax.inject.Singleton

data class SchedulePlan(val label: String, val allocation: AllocationPlan, val utilizationPercent: Double)

/**
 * Smart Schedule Generator — produces Conservative / Balanced / Aggressive plans by
 * feeding progressively larger fractions of the day's free time into the Task & Time
 * Allocation Engine, so the user can compare how much gets done at each pace.
 */
@Singleton
class SmartScheduleGenerator @Inject constructor(
    private val allocationEngine: TaskTimeAllocationEngine,
) {
    fun generate(tasks: List<TaskEntity>, availableMinutesToday: Int): List<SchedulePlan> {
        val presets = listOf("Conservative" to 0.6, "Balanced" to 0.8, "Aggressive" to 1.0)
        return presets.map { (label, fraction) ->
            val minutes = (availableMinutesToday * fraction).toInt()
            SchedulePlan(label, allocationEngine.allocate(tasks, minutes), fraction * 100)
        }
    }
}
