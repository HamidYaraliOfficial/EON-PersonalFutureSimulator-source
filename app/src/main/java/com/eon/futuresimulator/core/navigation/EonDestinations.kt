package com.eon.futuresimulator.core.navigation

/** Main Navigation: Home, Goals, Planner, Simulator, Timeline, Insights, Settings. */
sealed class EonDestination(val route: String, val labelKey: String, val icon: String) {
    data object Home : EonDestination("home", "nav_home", "home")
    data object Goals : EonDestination("goals", "nav_goals", "flag")
    data object Planner : EonDestination("planner", "nav_planner", "calendar")
    data object Simulator : EonDestination("simulator", "nav_simulator", "science")
    data object Timeline : EonDestination("timeline", "nav_timeline", "timeline")
    data object Insights : EonDestination("insights", "nav_insights", "insights")
    data object Settings : EonDestination("settings", "nav_settings", "settings")

    companion object {
        val bottomBarItems = listOf(Home, Goals, Planner, Simulator, Timeline, Insights, Settings)
    }
}

/** Secondary / detail routes, reached by pushing on top of the bottom-nav graph. */
object EonRoutes {
    const val GOAL_DETAIL = "goal_detail/{goalId}"
    fun goalDetail(goalId: String) = "goal_detail/$goalId"

    const val CREATE_GOAL = "create_goal"

    const val SCENARIO_BUILDER = "scenario_builder"
    const val SCENARIO_RESULT = "scenario_result/{runId}"
    fun scenarioResult(runId: String) = "scenario_result/$runId"

    const val SCENARIO_COMPARISON = "scenario_comparison"
    const val PRIVACY_CENTER = "privacy_center"
    const val EXPORT_IMPORT = "export_import"
    const val GOAL_GRAPH = "goal_graph"
    const val NL_SCENARIO_BUILDER = "nl_scenario_builder"
}
