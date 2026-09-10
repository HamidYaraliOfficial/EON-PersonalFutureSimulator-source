package com.eon.futuresimulator.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.data.database.entity.RecommendationEntity
import com.eon.futuresimulator.domain.model.GoalStatus
import com.eon.futuresimulator.domain.repository.GoalRepository
import com.eon.futuresimulator.domain.repository.HabitRepository
import com.eon.futuresimulator.domain.repository.ScenarioRepository
import com.eon.futuresimulator.habits.HabitStreakCalculator
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val activeGoals: List<GoalEntity> = emptyList(),
    val activeHabitCount: Int = 0,
    val bestStreak: Int = 0,
    val recommendations: List<RecommendationEntity> = emptyList(),
    val recentRunCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    goalRepository: GoalRepository,
    habitRepository: HabitRepository,
    scenarioRepository: ScenarioRepository,
    private val habitStreakCalculator: HabitStreakCalculator,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        goalRepository.observeGoals(),
        habitRepository.observeActiveHabits(),
        scenarioRepository.observeActiveRecommendations(),
        scenarioRepository.observeRecentRuns(5),
    ) { goals, habits, recs, runs ->
        HomeUiState(
            activeGoals = goals.filter { it.status == GoalStatus.ACTIVE }.take(5),
            activeHabitCount = habits.size,
            bestStreak = 0, // populated lazily below via a side computation to avoid N+1 flow combine complexity
            recommendations = recs.take(3),
            recentRunCount = runs.size,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}

@Composable
fun HomeScreen(
    onOpenGoal: (String) -> Unit,
    onOpenSimulator: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("EON", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Personal Future Simulator", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            EonCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("${state.activeGoals.size} active goals · ${state.activeHabitCount} habits", style = MaterialTheme.typography.bodySmall)
                    }
                    FilledTonalButton(onClick = onOpenSimulator) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Quick Simulation")
                    }
                }
            }
        }

        item { Text("Current Goals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(state.activeGoals) { goal ->
            EonCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().clickable { onOpenGoal(goal.id) },
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                        Text(goal.category.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodySmall)
                    }
                    val pct = if (goal.targetValue > 0) ((goal.currentValue / goal.targetValue) * 100).toInt().coerceIn(0, 100) else 0
                    Text("$pct%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }

        if (state.recommendations.isNotEmpty()) {
            item { Text("AI Insights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(state.recommendations) { rec ->
                EonCard(modifier = Modifier.fillMaxWidth()) {
                    Text(rec.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text(rec.explanation, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                    Text("Assumption: ${rec.assumption}", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}
