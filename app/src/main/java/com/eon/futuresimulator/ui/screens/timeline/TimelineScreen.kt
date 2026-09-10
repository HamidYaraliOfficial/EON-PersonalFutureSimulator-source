package com.eon.futuresimulator.ui.screens.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.data.database.entity.SimulationRunEntity
import com.eon.futuresimulator.domain.model.SimulationRunStatus
import com.eon.futuresimulator.domain.repository.ScenarioRepository
import com.eon.futuresimulator.simulation.model.SimulationResult
import com.eon.futuresimulator.ui.components.EonCard
import com.eon.futuresimulator.ui.components.EstimateLabel
import com.eon.futuresimulator.ui.components.charts.LineChartWithConfidenceBand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class TimelineUiState(
    val runs: List<SimulationRunEntity> = emptyList(),
    val selectedResult: SimulationResult? = null,
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val scenarioRepository: ScenarioRepository,
) : ViewModel() {
    private val json = Json { ignoreUnknownKeys = true }
    private val _state = MutableStateFlow(TimelineUiState())
    val state: StateFlow<TimelineUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            scenarioRepository.observeRecentRuns(30).collect { runs ->
                _state.update { it.copy(runs = runs) }
                val latestCompleted = runs.firstOrNull { it.status == SimulationRunStatus.COMPLETED && it.resultJson != null }
                val decoded = latestCompleted?.resultJson?.let { j -> runCatching { json.decodeFromString(SimulationResult.serializer(), j) }.getOrNull() }
                if (decoded != null) _state.update { it.copy(selectedResult = decoded) }
            }
        }
    }

    fun select(run: SimulationRunEntity) {
        val decoded = run.resultJson?.let { j -> runCatching { json.decodeFromString(SimulationResult.serializer(), j) }.getOrNull() }
        _state.update { it.copy(selectedResult = decoded) }
    }
}

/** Simulation Timeline / Simulation Replay — Future Days/Weeks/Months for any past run. */
@Composable
fun TimelineScreen(viewModel: TimelineViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Timeline", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                EstimateLabel()
            }
        }

        state.selectedResult?.let { result ->
            item {
                EonCard {
                    Text(result.scenarioName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    LineChartWithConfidenceBand(
                        points = result.confidenceBands["goalProgressPercent"] ?: emptyList(),
                        yAxisLabel = "Goal Progress % over ${result.horizonDays} days",
                    )
                }
            }
        }

        item { Text("Simulation Replay — recent runs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(state.runs, key = { it.id }) { run ->
            EonCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().clickable(
                        enabled = run.status == SimulationRunStatus.COMPLETED,
                    ) { viewModel.select(run) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text("Run ${run.id.take(8)}", style = MaterialTheme.typography.bodyMedium)
                        Text("${run.iterations} iterations · seed ${run.seed}", style = MaterialTheme.typography.labelSmall)
                    }
                    Text(run.status.name, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        if (state.runs.isEmpty()) {
            item { Text("No simulations run yet — head to the Simulator to create your first one.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
