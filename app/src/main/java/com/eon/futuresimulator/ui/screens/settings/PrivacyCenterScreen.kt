package com.eon.futuresimulator.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.core.preferences.UserPreferencesRepository
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PrivacyUiState(
    val calendarAccess: Boolean = false, val activityData: Boolean = true, val habitData: Boolean = true,
    val usageData: Boolean = true, val cloudAi: Boolean = false, val cloudSync: Boolean = false,
    val biometricLock: Boolean = false, val retentionDays: Int = 365,
)

@HiltViewModel
class PrivacyCenterViewModel @Inject constructor(private val prefs: UserPreferencesRepository) : ViewModel() {
    val state: StateFlow<PrivacyUiState> = combine(
        combine(prefs.calendarAccessConsent, prefs.activityDataConsent, prefs.habitDataConsent) { a, b, c -> Triple(a, b, c) },
        combine(prefs.usageDataConsent, prefs.cloudAiConsent, prefs.cloudSyncConsent) { a, b, c -> Triple(a, b, c) },
        combine(prefs.biometricLockEnabled, prefs.retentionDays) { a, b -> a to b },
    ) { g1, g2, g3 ->
        PrivacyUiState(g1.first, g1.second, g1.third, g2.first, g2.second, g2.third, g3.first, g3.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrivacyUiState())

    fun setCalendar(v: Boolean) = viewModelScope.launch { prefs.setCalendarAccessConsent(v) }
    fun setActivity(v: Boolean) = viewModelScope.launch { prefs.setActivityDataConsent(v) }
    fun setHabit(v: Boolean) = viewModelScope.launch { prefs.setHabitDataConsent(v) }
    fun setUsage(v: Boolean) = viewModelScope.launch { prefs.setUsageDataConsent(v) }
    fun setCloudAi(v: Boolean) = viewModelScope.launch { prefs.setCloudAiConsent(v) }
    fun setCloudSync(v: Boolean) = viewModelScope.launch { prefs.setCloudSyncConsent(v) }
    fun setBiometric(v: Boolean) = viewModelScope.launch { prefs.setBiometricLockEnabled(v) }
    fun setRetention(days: Int) = viewModelScope.launch { prefs.setRetentionDays(days) }
}

@Composable
fun PrivacyCenterScreen(viewModel: PrivacyCenterViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Privacy Center", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "EON is Local-First: everything below defaults to off or on-device only. Nothing is sent anywhere unless you explicitly enable it here.",
            style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
        )

        ToggleRow("Calendar access", "Read-only access to your device Calendar for Time Budget analysis.", state.calendarAccess, viewModel::setCalendar)
        ToggleRow("Activity data", "Store manually logged workouts / activities.", state.activityData, viewModel::setActivity)
        ToggleRow("Habit data", "Store Habit check-ins.", state.habitData, viewModel::setHabit)
        ToggleRow("Usage data", "Local-only interaction stats used to improve Routine analysis.", state.usageData, viewModel::setUsage)
        ToggleRow("Cloud AI", "Send simulation summaries (not raw records) to an external AI provider.", state.cloudAi, viewModel::setCloudAi)
        ToggleRow("Cloud Sync", "Sync an encrypted copy of your data to your own cloud account.", state.cloudSync, viewModel::setCloudSync)
        ToggleRow("Biometric lock", "Require Face/Fingerprint before Export or deleting History.", state.biometricLock, viewModel::setBiometric)

        Spacer(Modifier.height(8.dp))
        EonCard {
            Text("Retention", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Keep detailed history for ${state.retentionDays} days.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            Slider(
                value = state.retentionDays.toFloat(), onValueChange = { viewModel.setRetention(it.toInt()) },
                valueRange = 30f..1095f,
            )
        }
    }
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    EonCard(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}
