package com.eon.futuresimulator

import com.eon.futuresimulator.export.EonExportBundle
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

class ExportImportTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `export bundle round-trips through JSON without data loss`() {
        val bundle = EonExportBundle(
            exportedAtEpochMillis = 123L,
            goals = emptyList(), milestones = emptyList(), tasks = emptyList(), habits = emptyList(),
            habitCheckIns = emptyList(), routines = emptyList(), studySessions = emptyList(), workSessions = emptyList(),
            sleepRecords = emptyList(), activityRecords = emptyList(), financialInputs = emptyList(),
            energyLevels = emptyList(), moods = emptyList(), streaks = emptyList(), progressMetrics = emptyList(),
            scenarios = emptyList(), simulationRuns = emptyList(), assumptions = emptyList(), forecasts = emptyList(),
            recommendations = emptyList(), journalEntries = emptyList(), snapshots = emptyList(), reviewRecords = emptyList(),
        )
        val serialized = json.encodeToString(bundle)
        val decoded = json.decodeFromString(EonExportBundle.serializer(), serialized)
        assertThat(decoded.schemaVersion).isEqualTo(EonExportBundle.CURRENT_SCHEMA_VERSION)
        assertThat(decoded.exportedAtEpochMillis).isEqualTo(123L)
    }

    @Test
    fun `schema version defaults to current version`() {
        assertThat(EonExportBundle.CURRENT_SCHEMA_VERSION).isEqualTo(1)
    }
}
