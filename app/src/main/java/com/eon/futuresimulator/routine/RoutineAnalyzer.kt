package com.eon.futuresimulator.routine

import com.eon.futuresimulator.core.util.DataSource
import com.eon.futuresimulator.core.util.DateTimeUtils
import com.eon.futuresimulator.core.util.IdGenerator
import com.eon.futuresimulator.data.database.entity.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Routine Analyzer — Local Pattern Analysis only (no network, no ML model download).
 * Groups the user's own Study/Work/Sleep/Activity records by weekday and reports
 * *typical* minutes per category, tagged with a [RoutineEntity.derivedConfidence] that
 * grows with sample size — this is always presented as an Observation/Estimate, never
 * a claim about what the user will do.
 */
@Singleton
class RoutineAnalyzer @Inject constructor() {
    companion object { const val MIN_SAMPLES_FOR_CONFIDENCE = 3 }

    fun analyze(
        studySessions: List<StudySessionEntity>,
        workSessions: List<WorkSessionEntity>,
        sleepRecords: List<SleepRecordEntity>,
        activityRecords: List<ActivityRecordEntity>,
    ): List<RoutineEntity> {
        val studyPairs = studySessions.map { it.startEpochMillis to it.durationMinutes }
        val workPairs = workSessions.map { it.startEpochMillis to it.durationMinutes }
        val sleepPairs = sleepRecords.map { it.dateEpochMillis to it.durationMinutes }
        val activityPairs = activityRecords.map { it.dateEpochMillis to it.durationMinutes }

        return (1..7).map { weekday ->
            val studyMinutes = averageMinutesForWeekday(studyPairs, weekday)
            val workMinutes = averageMinutesForWeekday(workPairs, weekday)
            val sleepMinutes = averageMinutesForWeekday(sleepPairs, weekday)
            val exerciseMinutes = averageMinutesForWeekday(activityPairs, weekday)

            val sampleSize = countForWeekday(studyPairs, weekday) + countForWeekday(workPairs, weekday) +
                countForWeekday(sleepPairs, weekday) + countForWeekday(activityPairs, weekday)

            val totalUsed = studyMinutes + workMinutes + sleepMinutes + exerciseMinutes
            val freeMinutes = (24 * 60 - totalUsed).coerceAtLeast(0)
            val confidence = (sampleSize.toDouble() / (sampleSize + MIN_SAMPLES_FOR_CONFIDENCE)).coerceIn(0.0, 0.95)

            RoutineEntity(
                id = IdGenerator.newId(),
                dayOfWeekMask = 1 shl (weekday - 1),
                studyMinutes = studyMinutes,
                workMinutes = workMinutes,
                sleepMinutes = sleepMinutes,
                exerciseMinutes = exerciseMinutes,
                freeMinutes = freeMinutes,
                derivedConfidence = confidence,
                sampleSize = sampleSize,
                createdAt = DateTimeUtils.nowEpochMillis(),
                updatedAt = DateTimeUtils.nowEpochMillis(),
                source = DataSource.DERIVED,
                version = 1,
            )
        }
    }

    private fun matchesWeekday(epochMillis: Long, weekday: Int): Boolean =
        DateTimeUtils.epochMillisToLocalDate(epochMillis).dayOfWeek.value == weekday

    private fun averageMinutesForWeekday(entries: List<Pair<Long, Int>>, weekday: Int): Int {
        val matching = entries.filter { matchesWeekday(it.first, weekday) }
        return if (matching.isEmpty()) 0 else matching.sumOf { it.second } / matching.size
    }

    private fun countForWeekday(entries: List<Pair<Long, Int>>, weekday: Int): Int =
        entries.count { matchesWeekday(it.first, weekday) }
}
