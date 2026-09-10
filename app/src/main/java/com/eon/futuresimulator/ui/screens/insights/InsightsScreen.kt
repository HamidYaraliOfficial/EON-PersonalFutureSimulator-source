package com.eon.futuresimulator.ui.screens.insights

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
import com.eon.futuresimulator.analytics.ForecastAccuracyTracker
import com.eon.futuresimulator.analytics.Insight
import com.eon.futuresimulator.analytics.InsightsEngine
import com.eon.futuresimulator.domain.repository.HabitRepository
import com.eon.futuresimulator.domain.repository.JournalSnapshotRepository
import com.eon.futuresimulator.domain.repository.ProgressRepository
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<Insight> = emptyList(),
    val avgForecastErrorPercent: Double? = null,
    val reviewCount: Int = 0,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val progressRepository: ProgressRepository,
    private val journalSnapshotRepository: JournalSnapshotRepository,
    private val insightsEngine: InsightsEngine,
    private val accuracyTracker: ForecastAccuracyTracker,
) : ViewModel() {
    private val _state = MutableStateFlow(InsightsUiState())
    val state: StateFlow<InsightsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val checkIns = habitRepository.getAllCheckInsOnce()
            val progress = progressRepository.getAllOnce()
            val insights = listOfNotNull(
                insightsEngine.weekendVsWeekdayCompletion(checkIns),
                insightsEngine.progressTrend(progress),
            )
            _state.update { it.copy(insights = insights) }
        }
        viewModelScope.launch {
            journalSnapshotRepository.observeReviewRecords().collect { records ->
                val avg = if (records.isEmpty()) null else records.map { it.forecastErrorPercent }.average()
                _state.update { it.copy(avgForecastErrorPercent = avg, reviewCount = records.size) }
            }
        }
    }
}

/** Insights + Reality vs Simulation Dashboard (forecast error is only ever a stored, computed number — never fabricated). */
@Composable
fun InsightsScreen(viewModel: InsightsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Insights", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }

        item {
            EonCard {
                Text("Reality vs Simulation", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (state.reviewCount == 0) {
                    Text("No completed review periods yet — this fills in as Simulations are compared against real outcomes.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                } else {
                    Text(
                        "Average forecast error across ${state.reviewCount} review(s): ${"%.1f".format(state.avgForecastErrorPercent ?: 0.0)}%",
                        style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }

        item { Text("Observed Patterns", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(state.insights) { insight ->
            EonCard(modifier = Modifier.fillMaxWidth()) {
                Text(insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(insight.detail, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                Text("Confidence: ${(insight.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
        if (state.insights.isEmpty()) {
            item { Text("Not enough history yet for a reliable Observation — keep logging Habits and Progress.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
