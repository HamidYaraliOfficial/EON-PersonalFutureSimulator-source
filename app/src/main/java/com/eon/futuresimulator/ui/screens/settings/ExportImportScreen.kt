package com.eon.futuresimulator.ui.screens.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eon.futuresimulator.export.DataExportManager
import com.eon.futuresimulator.export.DataImportManager
import com.eon.futuresimulator.export.ImportResult
import com.eon.futuresimulator.ui.components.EonCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class ExportImportViewModel @Inject constructor(
    private val exportManager: DataExportManager,
    private val importManager: DataImportManager,
) : ViewModel() {
    var statusMessage by mutableStateOf<String?>(null); private set
    var isBusy by mutableStateOf(false); private set

    fun export(write: suspend (String) -> Unit) {
        viewModelScope.launch {
            isBusy = true
            runCatching {
                val jsonText = exportManager.exportToJsonString()
                write(jsonText)
            }.onSuccess { statusMessage = "Export complete." }
                .onFailure { statusMessage = "Export failed: ${it.message}" }
            isBusy = false
        }
    }

    fun import(read: suspend () -> String) {
        viewModelScope.launch {
            isBusy = true
            val raw = runCatching { read() }.getOrElse { statusMessage = "Could not read file: ${it.message}"; isBusy = false; return@launch }
            when (val result = importManager.importFromJsonString(raw)) {
                is ImportResult.Success -> statusMessage = "Imported ${result.importedEntityCount} record(s)."
                is ImportResult.SchemaMismatch -> statusMessage = "File schema v${result.fileVersion} isn't supported (expected v${result.supportedVersion})."
                ImportResult.IntegrityCheckFailed -> statusMessage = "Integrity check failed — the file may be corrupted or edited."
                is ImportResult.Failed -> statusMessage = "Import failed: ${result.message}"
            }
            isBusy = false
        }
    }
}

@Composable
fun ExportImportScreen(viewModel: ExportImportViewModel = hiltViewModel()) {
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.export { text -> writeUri(context, uri, text) }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.import { readUri(context, uri) }
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Export / Import", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        EonCard {
            Text("Export", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Every Goal, Task, Habit, Scenario, Simulation Run and Setting as one versioned, checksummed JSON file.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
            Button(onClick = { exportLauncher.launch("eon_export_${System.currentTimeMillis()}.json") }, enabled = !viewModel.isBusy) {
                Text("Export to file")
            }
        }

        EonCard {
            Text("Import", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text("Restores from a file previously produced by Export. Schema version and checksum are verified first.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp))
            OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }, enabled = !viewModel.isBusy) {
                Text("Import from file")
            }
        }

        if (viewModel.isBusy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        viewModel.statusMessage?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
    }
}

private fun writeUri(context: Context, uri: Uri, text: String) {
    context.contentResolver.openOutputStream(uri)?.use { stream: OutputStream -> stream.write(text.toByteArray(Charsets.UTF_8)) }
}

private fun readUri(context: Context, uri: Uri): String =
    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) } ?: ""
