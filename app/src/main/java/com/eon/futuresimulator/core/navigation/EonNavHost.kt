package com.eon.futuresimulator.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eon.futuresimulator.ui.screens.goals.GoalDetailScreen
import com.eon.futuresimulator.ui.screens.goals.GoalsScreen
import com.eon.futuresimulator.ui.screens.home.HomeScreen
import com.eon.futuresimulator.ui.screens.insights.InsightsScreen
import com.eon.futuresimulator.ui.screens.planner.PlannerScreen
import com.eon.futuresimulator.ui.screens.settings.ExportImportScreen
import com.eon.futuresimulator.ui.screens.settings.PrivacyCenterScreen
import com.eon.futuresimulator.ui.screens.settings.SettingsScreen
import com.eon.futuresimulator.ui.screens.simulator.SimulatorScreen
import com.eon.futuresimulator.ui.screens.timeline.TimelineScreen

private fun iconFor(route: String): ImageVector = when (route) {
    EonDestination.Home.route -> Icons.Default.Home
    EonDestination.Goals.route -> Icons.Default.Flag
    EonDestination.Planner.route -> Icons.Default.CalendarMonth
    EonDestination.Simulator.route -> Icons.Default.Science
    EonDestination.Timeline.route -> Icons.Default.Timeline
    EonDestination.Insights.route -> Icons.Default.Insights
    else -> Icons.Default.Settings
}

private fun labelFor(route: String): String = when (route) {
    EonDestination.Home.route -> "Home"
    EonDestination.Goals.route -> "Goals"
    EonDestination.Planner.route -> "Planner"
    EonDestination.Simulator.route -> "Simulator"
    EonDestination.Timeline.route -> "Timeline"
    EonDestination.Insights.route -> "Insights"
    else -> "Settings"
}

@Composable
fun EonApp(startDeepLink: String? = null) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (EonDestination.bottomBarItems.any { it.route == currentRoute }) {
                NavigationBar {
                    EonDestination.bottomBarItems.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(iconFor(dest.route), contentDescription = labelFor(dest.route)) },
                            label = { Text(labelFor(dest.route)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (startDeepLink == "what_if") EonDestination.Simulator.route else EonDestination.Home.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(EonDestination.Home.route) {
                HomeScreen(
                    onOpenGoal = { id -> navController.navigate(EonRoutes.goalDetail(id)) },
                    onOpenSimulator = { navController.navigate(EonDestination.Simulator.route) },
                )
            }
            composable(EonDestination.Goals.route) {
                GoalsScreen(onOpenGoal = { id -> navController.navigate(EonRoutes.goalDetail(id)) })
            }
            composable(EonRoutes.GOAL_DETAIL) {
                GoalDetailScreen(
                    onBack = { navController.popBackStack() },
                    onRunScenarioForGoal = { navController.navigate(EonDestination.Simulator.route) },
                )
            }
            composable(EonDestination.Planner.route) { PlannerScreen() }
            composable(EonDestination.Simulator.route) { SimulatorScreen() }
            composable(EonDestination.Timeline.route) { TimelineScreen() }
            composable(EonDestination.Insights.route) { InsightsScreen() }
            composable(EonDestination.Settings.route) {
                SettingsScreen(
                    onOpenPrivacyCenter = { navController.navigate(EonRoutes.PRIVACY_CENTER) },
                    onOpenExportImport = { navController.navigate(EonRoutes.EXPORT_IMPORT) },
                )
            }
            composable(EonRoutes.PRIVACY_CENTER) { PrivacyCenterScreen() }
            composable(EonRoutes.EXPORT_IMPORT) { ExportImportScreen() }
        }
    }
}
