package com.eon.futuresimulator.ui.screens.planner

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
import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.domain.repository.RoutineRepository
import com.eon.futuresimulator.domain.repository.TaskRepository
import com.eon.futuresimulator.recommendations.SchedulePlan
import com.eon.futuresimulator.recommendations.SmartScheduleGenerator
import com.eon.futuresimulator.simulation.engine.TimeBudgetEngine
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlannerUiState(
    val weeklyFreeMinutes: Int = 0,
    val backlogMinutes: Int = 0,
    val plans: List<SchedulePlan> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class PlannerViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val scheduleGenerator: SmartScheduleGenerator,
) : ViewModel() {
    private val _state = MutableStateFlow(PlannerUiState())
    val state: StateFlow<PlannerUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val backlog = taskRepository.getBacklogOnce()
            val routines = routineRepository.getAllOnce()
            val weeklyFree = routines.sumOf { it.freeMinutes }
            val todayFree = routines.firstOrNull()?.freeMinutes ?: 150
            val plans = scheduleGenerator.generate(backlog, todayFree)
            _state.value = PlannerUiState(
                weeklyFreeMinutes = weeklyFree,
                backlogMinutes = backlog.sumOf { it.durationMinutes },
                plans = plans,
                loading = false,
            )
        }
    }
}

@Composable
fun PlannerScreen(viewModel: PlannerViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Planner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        item {
            EonCard {
                Text("Time Budget", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("~${state.weeklyFreeMinutes / 60}h free this week · ${state.backlogMinutes / 60}h task backlog", style = MaterialTheme.typography.bodySmall)
            }
        }
        item { Text("Smart Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }
        items(state.plans) { plan ->
            EonCard(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(plan.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                    Text("${plan.utilizationPercent.toInt()}% of free time", style = MaterialTheme.typography.labelMedium)
                }
                Text(
                    "${plan.allocation.slots.count { it.fitsToday }} tasks fit · ${plan.allocation.overflowMinutes} min overflow",
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp),
                )
                plan.allocation.warnings.forEach { w -> Text("⚠ $w", style = MaterialTheme.typography.labelSmall) }
            }
        }
        if (!state.loading && state.plans.isEmpty()) {
            item { Text("No open tasks to schedule right now.", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}
