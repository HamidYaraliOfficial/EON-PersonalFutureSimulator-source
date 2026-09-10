package com.eon.futuresimulator.ui.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.GoalEntity
import com.eon.futuresimulator.domain.model.*
import com.eon.futuresimulator.domain.repository.GoalRepository
import com.eon.futuresimulator.goals.GoalBreakdownEngine
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val breakdownEngine: GoalBreakdownEngine,
) : ViewModel() {

    val goals: StateFlow<List<GoalEntity>> = goalRepository.observeGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGoal(title: String, category: GoalCategory, targetValue: Double, unit: String, deadlineEpochMillis: Long?) {
        viewModelScope.launch {
            val goal = GoalEntity(
                id = IdGenerator.newId(), title = title, description = "", category = category,
                priority = Priority.MEDIUM, horizon = GoalHorizon.MEDIUM_TERM, deadlineEpochMillis = deadlineEpochMillis,
                targetValue = targetValue, currentValue = 0.0, unit = unit, status = GoalStatus.ACTIVE,
                createdAt = DateTimeUtils.nowEpochMillis(), updatedAt = DateTimeUtils.nowEpochMillis(),
                source = DataSource.MANUAL, version = 1,
            )
            goalRepository.upsertGoal(goal)
            // Goal Breakdown Engine immediately proposes Milestones + first Tasks for review.
            val plan = breakdownEngine.breakdown(goal)
            goalRepository.upsertMilestones(plan.milestones)
        }
    }
}

@Composable
fun GoalsScreen(onOpenGoal: (String) -> Unit, viewModel: GoalsViewModel = hiltViewModel()) {
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Create goal") }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Text("Goals", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            items(goals, key = { it.id }) { goal ->
                EonCard(modifier = Modifier.fillMaxWidth()) {
                    androidx.compose.foundation.layout.Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                            Text(
                                "${goal.category.name.lowercase().replaceFirstChar { it.uppercase() }} · ${goal.horizon.name.lowercase().replace('_', ' ')}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        TextButton(onClick = { onOpenGoal(goal.id) }) { Text("Open") }
                    }
                }
            }
            if (goals.isEmpty()) {
                item {
                    Text(
                        "No goals yet. Tap + to create your first Goal — EON will suggest Milestones automatically.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { title, category, target, unit ->
                viewModel.createGoal(title, category, target, unit, null)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun CreateGoalDialog(onDismiss: () -> Unit, onCreate: (String, GoalCategory, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("100") }
    var unit by remember { mutableStateOf("%") }
    var category by remember { mutableStateOf(GoalCategory.PRODUCTIVITY) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true)
                OutlinedTextField(value = targetText, onValueChange = { targetText = it }, label = { Text("Target value") }, singleLine = true)
                OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit") }, singleLine = true)
                Box {
                    OutlinedButton(onClick = { expanded = true }) { Text(category.name) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        GoalCategory.entries.forEach { c ->
                            DropdownMenuItem(text = { Text(c.name) }, onClick = { category = c; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onCreate(title.ifBlank { "Untitled Goal" }, category, targetText.toDoubleOrNull() ?: 100.0, unit) }) {
                Text("Create")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
