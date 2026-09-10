package com.eon.futuresimulator.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.eon.futuresimulator.simulation.model.ConfidenceBandPoint

/**
 * Interactive Future Graph — X axis = time (day index), Y axis = the selected metric.
 * Hand-drawn on Compose Canvas: a filled P10-P90 Confidence Band behind a solid P50 line.
 * No external chart library — keeps the Simulator screen dependency-light and fully
 * themeable via MaterialTheme colors.
 */
@Composable
fun LineChartWithConfidenceBand(
    points: List<ConfidenceBandPoint>,
    modifier: Modifier = Modifier,
    yAxisLabel: String = "",
    lineColor: Color = MaterialTheme.colorScheme.primary,
) {
    Column(modifier = modifier) {
        if (yAxisLabel.isNotBlank()) {
            Text(yAxisLabel, style = MaterialTheme.typography.labelMedium)
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
            if (points.isEmpty()) return@Canvas
            val maxY = (points.maxOf { it.p90 }).coerceAtLeast(1.0)
            val minY = (points.minOf { it.p10 }).coerceAtMost(0.0)
            val range = (maxY - minY).coerceAtLeast(1.0)
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)

            fun xOf(i: Int) = i * stepX
            fun yOf(v: Double) = size.height - ((v - minY) / range * size.height).toFloat()

            // Confidence band fill
            val bandPath = androidx.compose.ui.graphics.Path().apply {
                points.forEachIndexed { i, p -> if (i == 0) moveTo(xOf(i), yOf(p.p90)) else lineTo(xOf(i), yOf(p.p90)) }
                for (i in points.indices.reversed()) lineTo(xOf(i), yOf(points[i].p10))
                close()
            }
            drawPath(bandPath, color = lineColor.copy(alpha = 0.15f))

            // P50 median line
            for (i in 0 until points.size - 1) {
                drawLine(
                    color = lineColor,
                    start = Offset(xOf(i), yOf(points[i].p50)),
                    end = Offset(xOf(i + 1), yOf(points[i + 1].p50)),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round,
                )
            }
            // Baseline zero/reference line
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(0f, yOf(0.0)),
                end = Offset(size.width, yOf(0.0)),
                strokeWidth = 1.5f,
            )
        }
    }
}
