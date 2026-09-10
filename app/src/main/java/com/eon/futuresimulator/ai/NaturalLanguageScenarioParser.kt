package com.eon.futuresimulator.ai

import com.eon.futuresimulator.domain.model.ScenarioType
import com.eon.futuresimulator.simulation.model.ScenarioConfig
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedScenarioDraft(
    val config: ScenarioConfig,
    val matchedPhrases: List<String>,
    val confidence: Double,
)

/**
 * Natural Language Scenario Builder — a deterministic, offline, regex/keyword parser.
 * It never calls a network model; it extracts numbers + known keywords ("study",
 * "workout"/"exercise", "sleep", "read"/"pages", "distraction"/"social media",
 * "work X days") and maps them onto [ScenarioConfig] fields. The result is always
 * shown to the user as a Preview (Assumptions + Parameters) before it can be run —
 * this class never runs a simulation itself.
 */
@Singleton
class NaturalLanguageScenarioParser @Inject constructor() {

    private val numberRegex = Regex("""(\d+(?:\.\d+)?)""")

    fun parse(text: String, horizonDaysDefault: Int = 90): ParsedScenarioDraft {
        val lower = text.lowercase()
        var config = ScenarioConfig(name = "From text: \"${text.take(40)}\"", scenarioType = ScenarioType.CUSTOM, horizonDays = horizonDaysDefault)
        val matched = mutableListOf<String>()

        findMinutesOrHours("study|studying|python|learn|reading code", lower)?.let {
            config = config.copy(dailyStudyMinutesDelta = it)
            matched += "study: +$it min/day"
        }
        findCount("workout|exercise|gym|run|training", lower)?.let {
            config = config.copy(weeklyWorkoutSessionsDelta = it)
            matched += "workouts: +$it/week"
        }
        findMinutesOrHours("sleep", lower)?.let {
            config = config.copy(sleepHoursTarget = it / 60.0)
            matched += "sleep target: ${"%.1f".format(it / 60.0)}h"
        }
        findCount("page|pages", lower)?.let {
            config = config.copy(readingPagesPerDayDelta = it)
            matched += "reading: +$it pages/day"
        }
        findMinutesOrHours("distraction|social media|phone|scrolling", lower)?.let {
            config = config.copy(distractionReductionMinutesPerDay = it)
            matched += "distraction reduction: $it min/day"
        }
        findCount("day.?s? (a|per) week", lower)?.let {
            config = config.copy(workDaysPerWeekDelta = it - 5)
            matched += "work days/week: $it"
        }
        findHorizonDays(lower)?.let {
            config = config.copy(horizonDays = it)
            matched += "horizon: $it days"
        }

        val confidence = (matched.size.toDouble() / 4.0).coerceIn(0.1, 0.95)
        return ParsedScenarioDraft(config, matched, confidence)
    }

    private fun findMinutesOrHours(keywordPattern: String, text: String): Int? {
        val regex = Regex("""(\d+(?:\.\d+)?)\s*(hour|hr|h|minute|min|m)?s?\s*(?:of|a|per)?\s*(?:$keywordPattern)""")
        val match = regex.find(text) ?: Regex("""(?:$keywordPattern)[^\d]{0,15}(\d+(?:\.\d+)?)\s*(hour|hr|h|minute|min|m)?""").find(text)
        val value = match?.groupValues?.getOrNull(1)?.toDoubleOrNull() ?: return null
        val unit = match.groupValues.getOrNull(2)
        return if (unit?.startsWith("h") == true || unit.isNullOrBlank() && value <= 6) (value * 60).toInt() else value.toInt()
    }

    private fun findCount(keywordPattern: String, text: String): Int? {
        val regex = Regex("""(\d+)\s*(?:times?|x)?\s*(?:a|per)?\s*(?:week)?\s*(?:$keywordPattern)""")
        val match = regex.find(text) ?: Regex("""(?:$keywordPattern)[^\d]{0,15}(\d+)""").find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun findHorizonDays(text: String): Int? {
        val monthMatch = Regex("""(\d+)\s*month""").find(text)
        if (monthMatch != null) return (monthMatch.groupValues[1].toIntOrNull() ?: return null) * 30
        val weekMatch = Regex("""(\d+)\s*week""").find(text)
        if (weekMatch != null) return (weekMatch.groupValues[1].toIntOrNull() ?: return null) * 7
        val dayMatch = Regex("""(\d+)\s*day""").find(text)
        if (dayMatch != null) return dayMatch.groupValues[1].toIntOrNull()
        return null
    }
}
