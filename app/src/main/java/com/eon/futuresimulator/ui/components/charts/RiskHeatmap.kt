package com.eon.futuresimulator.ui.components.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.eon.futuresimulator.core.theme.RiskCritical
import com.eon.futuresimulator.core.theme.RiskHigh
import com.eon.futuresimulator.core.theme.RiskLow
import com.eon.futuresimulator.core.theme.RiskMedium
import com.eon.futuresimulator.domain.model.RiskSeverity
import com.eon.futuresimulator.simulation.model.RiskFinding

/** Risk Heatmap — one cell per finding, colored by severity, laid out along the horizon. */
@Composable
fun RiskHeatmap(risks: List<RiskFinding>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        risks.forEach { risk ->
            val color = when (risk.severity) {
                RiskSeverity.LOW -> RiskLow
                RiskSeverity.MEDIUM -> RiskMedium
                RiskSeverity.HIGH -> RiskHigh
                RiskSeverity.CRITICAL -> RiskCritical
            }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.85f)),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Text(risk.type.name.take(2), color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
