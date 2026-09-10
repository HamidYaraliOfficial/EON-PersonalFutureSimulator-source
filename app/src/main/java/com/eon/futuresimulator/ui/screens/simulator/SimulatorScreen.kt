package com.eon.futuresimulator.ui.screens.simulator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eon.futuresimulator.core.theme.RiskHigh
import com.eon.futuresimulator.core.theme.AccentBluePrimary
import com.eon.futuresimulator.domain.model.ScenarioType
import com.eon.futuresimulator.ui.components.*
import com.eon.futuresimulator.ui.components.charts.BarComparisonChart
import com.eon.futuresimulator.ui.components.charts.BarDatum
import com.eon.futuresimulator.ui.components.charts.LineChartWithConfidenceBand
import com.eon.futuresimulator.ui.components.charts.RiskHeatmap

/**
 * Simulator — the "Control Room". Configure a Scenario (presets or manual sliders),
 * choose Monte Carlo iterations/Horizon/Seed, Run, then read the result as an
 * Interactive Future Graph with Confidence Bands, Feasibility, Risk Heatmap,
 * Sensitivity Chart and the Assumption Panel.
 */
@Composable
fun SimulatorScreen(viewModel: SimulatorViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Simulator", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Configure a scenario, then run it as an Estimate — never a guarantee.", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Text("Presets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
                items(ScenarioType.entries.toList()) { type ->
                    FilterChip(
                        selected = state.scenario.scenarioType == type,
                        onClick = { viewModel.applyPreset(type) },
                        label = { Text(type.name.lowercase().replace('_', ' ')) },
                    )
                }
            }
        }

        item {
            EonCard {
                Text(state.scenario.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                LabeledSlider(
                    label = "Daily study minutes (Δ)", value = state.scenario.dailyStudyMinutesDelta.toFloat(),
                    range = -60f..180f,
                    onChange = { v -> viewModel.updateScenario { it.copy(dailyStudyMinutesDelta = v.toInt()) } },
                )
                LabeledSlider(
                    label = "Weekly workout sessions (Δ)", value = state.scenario.weeklyWorkoutSessionsDelta.toFloat(),
                    range = -3f..7f,
                    onChange = { v -> viewModel.updateScenario { it.copy(weeklyWorkoutSessionsDelta = v.toInt()) } },
                )
                LabeledSlider(
                    label = "Distraction reduction (min/day)", value = state.scenario.distractionReductionMinutesPerDay.toFloat(),
                    range = 0f..120f,
                    onChange = { v -> viewModel.updateScenario { it.copy(distractionReductionMinutesPerDay = v.toInt()) } },
                )
                LabeledSlider(
                    label = "Habit reliability", value = state.scenario.habitReliability.toFloat(),
                    range = 0.1f..1f,
                    onChange = { v -> viewModel.updateScenario { it.copy(habitReliability = v.toDouble()) } },
                )
                LabeledSlider(
                    label = "Miss rate", value = state.scenario.missRate.toFloat(),
                    range = 0f..0.6f,
                    onChange = { v -> viewModel.updateScenario { it.copy(missRate = v.toDouble()) } },
                )
                LabeledSlider(
                    label = "Horizon (days)", value = state.scenario.horizonDays.toFloat(),
                    range = 14f..365f,
                    onChange = { v -> viewModel.updateScenario { it.copy(horizonDays = v.toInt()) } },
                )
                LabeledSlider(
                    label = "Monte Carlo iterations", value = state.iterations.toFloat(),
                    range = 1f..state.maxIterations.toFloat(),
                    onChange = { v -> viewModel.updateIterations(v.toInt()) },
                )

                Spacer(Modifier.height(6.dp))
                if (state.availableGoals.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    val label = state.availableGoals.firstOrNull { it.first == state.scenario.targetGoalId }?.second ?: "Auto-select goal"
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text("Target: $label") }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            state.availableGoals.forEach { (id, title) ->
                                DropdownMenuItem(text = { Text(title) }, onClick = {
                                    viewModel.updateScenario { it.copy(targetGoalId = id) }; expanded = false
                                })
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.runSimulation() }, enabled = !state.isRunning, modifier = Modifier.weight(1f)) {
                        Text(if (state.isRunning) "Running…" else "Run Simulation")
                    }
                    if (state.isRunning) {
                        OutlinedButton(onClick = { viewModel.cancelSimulation() }) { Text("Cancel") }
                    }
                }
                if (state.isRunning) {
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp)) }
            }
        }

        state.result?.let { result ->
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Result", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    EstimateLabel()
                    Spacer(Modifier.width(6.dp))
                    FeasibilityBadge(result.feasibility.level)
                }
            }

            result.goalCompletionEstimate?.let { est ->
                item {
                    EonCard {
                        Text("Projected completion", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Between ${"%.1f".format(est.lowWeeks)} and ${"%.1f".format(est.highWeeks)} weeks (median ~${"%.1f".format(est.medianWeeks)}), at ${est.confidencePercent}% confidence.",
                            style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            item {
                EonCard {
                    LineChartWithConfidenceBand(
                        points = result.confidenceBands["goalProgressPercent"] ?: emptyList(),
                        yAxisLabel = "Goal Progress % — P10/P50/P90 across ${result.iterations} simulated trajectories",
                    )
                }
            }

            if (result.risks.isNotEmpty()) {
                item {
                    EonCard {
                        Text("Risks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        RiskHeatmap(result.risks)
                        Spacer(Modifier.height(8.dp))
                        result.risks.forEach { risk ->
                            Row(Modifier.padding(vertical = 4.dp)) {
                                RiskSeverityBadge(risk.severity)
                                Spacer(Modifier.width(8.dp))
                                Column { Text(risk.reason, style = MaterialTheme.typography.bodySmall); Text("→ ${risk.suggestedMitigation}", style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
            }

            if (state.sensitivity.isNotEmpty()) {
                item {
                    EonCard {
                        Text("Sensitivity", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        BarComparisonChart(
                            data = state.sensitivity.take(6).map { BarDatum(it.variableName, it.impactScore, AccentBluePrimary) },
                        )
                    }
                }
            }

            item { AssumptionPanel(result.assumptions.map { it.key to it.explanation }) }

            state.aiExplanation?.let { explanation ->
                item {
                    EonCard {
                        Text("AI Future Advisor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(explanation, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun LabeledSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("%.1f".format(value), style = MaterialTheme.typography.labelMedium)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
