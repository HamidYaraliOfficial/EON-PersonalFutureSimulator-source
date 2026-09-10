package com.eon.futuresimulator.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eon.futuresimulator.core.theme.*
import com.eon.futuresimulator.domain.model.FeasibilityLevel
import com.eon.futuresimulator.domain.model.RealityMode
import com.eon.futuresimulator.domain.model.RiskSeverity

/** Windows-11-Fluent-ish soft-elevated card: rounded corners + subtle gradient "Mica" tint. */
@Composable
fun EonCard(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    ),
                ),
            )
            .background(accent.copy(alpha = 0.03f))
            .padding(16.dp),
        content = content,
    )
}

/** Feasibility pill — Feasible / Challenging / Unrealistic, colored + labeled, never a bare verdict. */
@Composable
fun FeasibilityBadge(level: FeasibilityLevel, modifier: Modifier = Modifier) {
    val color = when (level) {
        FeasibilityLevel.FEASIBLE -> FeasibleGreen
        FeasibilityLevel.CHALLENGING -> ChallengingAmber
        FeasibilityLevel.UNREALISTIC -> UnrealisticRed
    }
    Badge(text = level.name.lowercase().replaceFirstChar { it.uppercase() }, color = color, modifier = modifier)
}

@Composable
fun RiskSeverityBadge(severity: RiskSeverity, modifier: Modifier = Modifier) {
    val color = when (severity) {
        RiskSeverity.LOW -> RiskLow
        RiskSeverity.MEDIUM -> RiskMedium
        RiskSeverity.HIGH -> RiskHigh
        RiskSeverity.CRITICAL -> RiskCritical
    }
    Badge(text = severity.name.lowercase().replaceFirstChar { it.uppercase() }, color = color, modifier = modifier)
}

/** Dual Reality Mode indicator — REAL vs SIMULATED must always be visually unambiguous. */
@Composable
fun RealityBadge(mode: RealityMode, modifier: Modifier = Modifier) {
    val color = if (mode == RealityMode.REAL) RealDataColor else SimulatedDataColor
    Badge(text = if (mode == RealityMode.REAL) "REAL" else "SIMULATED", color = color, modifier = modifier)
}

@Composable
fun Badge(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
    }
}

/** Always-visible next to any Simulation output — the labels the whole app is built around. */
@Composable
fun EstimateLabel(text: String = "Estimate", modifier: Modifier = Modifier) {
    Badge(text = text, color = MaterialTheme.colorScheme.primary, modifier = modifier)
}

@Composable
fun AssumptionPanel(assumptions: List<Pair<String, String>>, modifier: Modifier = Modifier) {
    EonCard(modifier = modifier) {
        Text("Assumptions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        assumptions.forEach { (key, explanation) ->
            Row(Modifier.padding(vertical = 3.dp)) {
                Text("• ", color = MaterialTheme.colorScheme.primary)
                Text(explanation, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
