package com.eon.futuresimulator.goals

import com.eon.futuresimulator.data.database.entity.*

enum class GraphNodeType { GOAL, HABIT, TASK, MILESTONE }
data class GraphNode(val id: String, val label: String, val type: GraphNodeType)
data class GraphEdge(val fromId: String, val toId: String)
data class DependencyGraph(val nodes: List<GraphNode>, val edges: List<GraphEdge>)

/**
 * Goal Dependency Graph / Personal Strategy Graph builder — links Goal -> Habits ->
 * Tasks -> Milestones so the UI's Graph screen can render "this Habit feeds these N
 * Goals" and the full Goal -> Habits -> Tasks -> Milestones -> Outcomes path.
 */
object GoalDependencyGraphBuilder {

    fun build(
        goals: List<GoalEntity>,
        milestones: List<GoalMilestoneEntity>,
        tasks: List<TaskEntity>,
        habits: List<HabitEntity>,
    ): DependencyGraph {
        val nodes = mutableListOf<GraphNode>()
        val edges = mutableListOf<GraphEdge>()

        goals.forEach { nodes += GraphNode(it.id, it.title, GraphNodeType.GOAL) }
        habits.forEach { habit ->
            nodes += GraphNode(habit.id, habit.title, GraphNodeType.HABIT)
            habit.goalId?.let { edges += GraphEdge(habit.id, it) }
        }
        tasks.forEach { task ->
            nodes += GraphNode(task.id, task.title, GraphNodeType.TASK)
            task.goalId?.let { edges += GraphEdge(task.id, it) }
        }
        milestones.forEach { m ->
            nodes += GraphNode(m.id, m.title, GraphNodeType.MILESTONE)
            edges += GraphEdge(m.goalId, m.id)
        }

        return DependencyGraph(nodes, edges)
    }
}
