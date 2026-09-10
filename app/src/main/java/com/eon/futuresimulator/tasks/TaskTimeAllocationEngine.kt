package com.eon.futuresimulator.tasks

import com.eon.futuresimulator.data.database.entity.TaskEntity
import com.eon.futuresimulator.domain.model.Priority
import com.eon.futuresimulator.domain.model.TaskStatus
import javax.inject.Inject
import javax.inject.Singleton

data class AllocationSlot(val task: TaskEntity, val allocatedMinutes: Int, val fitsToday: Boolean)
data class AllocationPlan(val slots: List<AllocationSlot>, val overflowMinutes: Int, val warnings: List<String>)

/**
 * Task & Time Allocation Engine — greedily fits open Tasks into the free minutes left
 * today/this week, prioritizing Priority then nearest Deadline. Anything that doesn't
 * fit is reported as overflow with a plain-language warning rather than silently
 * dropped.
 */
@Singleton
class TaskTimeAllocationEngine @Inject constructor() {

    fun allocate(tasks: List<TaskEntity>, availableMinutes: Int): AllocationPlan {
        val open = tasks.filter { it.status == TaskStatus.TODO || it.status == TaskStatus.IN_PROGRESS }
            .sortedWith(
                compareByDescending<TaskEntity> { it.priority.ordinal }
                    .thenBy { it.deadlineEpochMillis ?: Long.MAX_VALUE },
            )

        var remaining = availableMinutes
        val slots = mutableListOf<AllocationSlot>()
        var overflow = 0

        open.forEach { task ->
            val fits = task.durationMinutes <= remaining
            if (fits) remaining -= task.durationMinutes else overflow += task.durationMinutes
            slots += AllocationSlot(task, if (fits) task.durationMinutes else 0, fits)
        }

        val warnings = mutableListOf<String>()
        if (overflow > 0) {
            warnings += "$overflow minute(s) of open tasks don't fit in today's available time."
        }
        val highPriorityOverflow = slots.filter { !it.fitsToday && it.task.priority.ordinal >= Priority.HIGH.ordinal }
        if (highPriorityOverflow.isNotEmpty()) {
            warnings += "${highPriorityOverflow.size} High/Critical priority task(s) could not be scheduled today."
        }

        return AllocationPlan(slots, overflow, warnings)
    }
}
