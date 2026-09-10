package com.eon.futuresimulator.ui.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.data.database.entity.GoalMilestoneEntity
import com.eon.futuresimulator.domain.repository.GoalRepository
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class GoalDetailUiState(val goal: GoalEntity? = null, val milestones: List<GoalMilestoneEntity> = emptyList())

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    goalRepository: GoalRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val goalId: String = checkNotNull(savedStateHandle["goalId"])

    val uiState: StateFlow<GoalDetailUiState> = combine(
        goalRepository.observeGoal(goalId),
        goalRepository.observeMilestones(goalId),
    ) { goal, milestones -> GoalDetailUiState(goal, milestones) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GoalDetailUiState())
}

@Composable
fun GoalDetailScreen(onBack: () -> Unit, onRunScenarioForGoal: (String) -> Unit, viewModel: GoalDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.goal?.title ?: "Goal") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) } },
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.goal?.let { goal ->
                item {
                    EonCard {
                        Text(goal.description.ifBlank { "No description" }, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(8.dp))
                        val pct = if (goal.targetValue > 0) (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f) else 0f
                        LinearProgressIndicator(progress = { pct }, modifier = Modifier.fillMaxWidth())
                        Text("${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                item {
                    Button(onClick = { onRunScenarioForGoal(goal.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Simulate futures for this goal")
                    }
                }
            }
            item { Text("Milestones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
            items(state.milestones, key = { it.id }) { milestone ->
                EonCard(modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(milestone.title, style = MaterialTheme.typography.bodyMedium)
                        Text(if (milestone.isAchieved) "✓" else "…", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text("Target: ${milestone.targetValue.toInt()}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
