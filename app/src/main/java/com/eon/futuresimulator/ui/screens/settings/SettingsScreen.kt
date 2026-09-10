package com.eon.futuresimulator.ui.screens.settings

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
import com.eon.futuresimulator.core.preferences.SupportedLanguages
import com.eon.futuresimulator.core.preferences.UserPreferencesRepository
import com.eon.futuresimulator.core.theme.ThemeMode
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageTag: String = "en",
    val autoAdaptEnabled: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        prefs.themeMode, prefs.languageTag, prefs.autoAdaptEnabled,
    ) { theme, lang, autoAdapt -> SettingsUiState(theme, lang, autoAdapt) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { prefs.setThemeMode(mode) }
    fun setLanguage(tag: String) = viewModelScope.launch { prefs.setLanguageTag(tag) }
    fun setAutoAdapt(enabled: Boolean) = viewModelScope.launch { prefs.setAutoAdaptEnabled(enabled) }
}

@Composable
fun SettingsScreen(
    onOpenPrivacyCenter: () -> Unit,
    onOpenExportImport: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }

        item {
            EonCard {
                Text("Theme", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                FlowRowThemeChips(selected = state.themeMode, onSelect = viewModel::setTheme)
            }
        }

        item {
            EonCard {
                Text("Language / زبان / 语言", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SupportedLanguages.ALL.forEach { lang ->
                        FilterChip(
                            selected = state.languageTag == lang.tag,
                            onClick = { viewModel.setLanguage(lang.tag) },
                            label = { Text(lang.displayName) },
                        )
                    }
                }
            }
        }

        item {
            EonCard {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("Auto Adapt", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("Let the Adaptive Plan Engine reschedule milestones automatically when you fall behind.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = state.autoAdaptEnabled, onCheckedChange = viewModel::setAutoAdapt)
                }
            }
        }

        item {
            EonCard {
                SettingsRow(title = "Privacy Center", subtitle = "Calendar, AI, Sync access & data retention", onClick = onOpenPrivacyCenter)
            }
        }
        item {
            EonCard {
                SettingsRow(title = "Export / Import", subtitle = "Versioned, checksummed backup of everything EON knows", onClick = onOpenExportImport)
            }
        }
    }
}

@Composable
private fun SettingsRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FlowRowThemeChips(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
            FilterChip(selected = selected == mode, onClick = { onSelect(mode) }, label = { Text(mode.name) })
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(ThemeMode.AMOLED, ThemeMode.RED, ThemeMode.BLUE).forEach { mode ->
            FilterChip(selected = selected == mode, onClick = { onSelect(mode) }, label = { Text(mode.name) })
        }
    }
}
