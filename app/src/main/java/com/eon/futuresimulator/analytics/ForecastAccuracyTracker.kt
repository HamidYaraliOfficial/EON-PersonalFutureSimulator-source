package com.eon.futuresimulator.analytics

import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.ReviewRecordEntity
import com.eon.futuresimulator.domain.model.ReviewPeriodType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Daily/Weekly/Monthly Review System backbone — compares one forecast value against
 * what actually happened and stores the real Forecast Error. Never synthesizes an
 * "accuracy score" beyond what the stored errors literally show (see Reality vs
 * Simulation Dashboard, which reads these records directly).
 */
@Singleton
class ForecastAccuracyTracker @Inject constructor() {

    fun buildReviewRecord(
        simulationRunId: String,
        periodType: ReviewPeriodType,
        metricName: String,
        forecastValue: Double,
        actualValue: Double,
    ): ReviewRecordEntity {
        val errorPercent = if (forecastValue == 0.0) {
            if (actualValue == 0.0) 0.0 else 100.0
        } else {
            abs(actualValue - forecastValue) / abs(forecastValue) * 100.0
        }
        return ReviewRecordEntity(
            id = IdGenerator.newId(),
            simulationRunId = simulationRunId,
            periodType = periodType,
            metricName = metricName,
            forecastValue = forecastValue,
            actualValue = actualValue,
            forecastErrorPercent = errorPercent,
            reviewedAtEpochMillis = DateTimeUtils.nowEpochMillis(),
            createdAt = DateTimeUtils.nowEpochMillis(),
            updatedAt = DateTimeUtils.nowEpochMillis(),
            source = DataSource.SYSTEM,
            version = 1,
        )
    }

    /** Average error across stored reviews for one metric — only computed, never fabricated, and null if no data. */
    fun averageErrorPercent(records: List<ReviewRecordEntity>, metricName: String): Double? {
        val matching = records.filter { it.metricName == metricName }
        if (matching.isEmpty()) return null
        return matching.map { it.forecastErrorPercent }.average()
    }
}
