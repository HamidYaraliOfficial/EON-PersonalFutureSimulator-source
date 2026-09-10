package com.eon.futuresimulator.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class BarDatum(val label: String, val value: Double, val color: Color)

/** Simple horizontal bar chart used by the Scenario Comparison Matrix and Sensitivity Chart. */
@Composable
fun BarComparisonChart(data: List<BarDatum>, modifier: Modifier = Modifier, valueFormatter: (Double) -> String = { "%.1f".format(it) }) {
    val maxVal = (data.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(0.01)
    Column(modifier = modifier) {
        data.forEach { datum ->
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(datum.label, modifier = Modifier.weight(0.38f), style = MaterialTheme.typography.bodySmall, maxLines = 1)
                Canvas(modifier = Modifier.weight(0.5f).height(14.dp)) {
                    val widthFraction = (datum.value / maxVal).toFloat().coerceIn(0f, 1f)
                    drawRoundRect(
                        color = datum.color.copy(alpha = 0.18f),
                        size = size.copy(width = size.width),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f),
                    )
                    drawRoundRect(
                        color = datum.color,
                        size = size.copy(width = size.width * widthFraction),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(7f, 7f),
                    )
                }
                Text(
                    valueFormatter(datum.value),
                    modifier = Modifier.weight(0.12f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
