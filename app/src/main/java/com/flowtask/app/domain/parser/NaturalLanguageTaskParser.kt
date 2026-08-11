package com.flowtask.app.domain.parser

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class ParsedTaskDraft(
    val title: String,
    val date: LocalDate,
    val time: LocalTime,
    val durationMinutes: Int,
    val recognizedDate: Boolean,
    val recognizedTime: Boolean,
)

interface TaskTextParser {
    fun parse(text: String, today: LocalDate = LocalDate.now()): ParsedTaskDraft
}

/** Deterministic, offline parser. The interface is intentionally replaceable by an LLM-backed parser. */
class NaturalLanguageTaskParser : TaskTextParser {
    override fun parse(text: String, today: LocalDate): ParsedTaskDraft {
        val normalized = text.trim().replace(Regex("\\s+"), " ")
        val lower = normalized.lowercase(Locale.getDefault())
        val dateResult = parseDate(lower, today)
        val timeResult = parseTime(lower)
        val duration = parseDuration(lower)
        return ParsedTaskDraft(
            title = cleanTitle(normalized).ifBlank { "Untitled task" },
            date = dateResult.first,
            time = timeResult.first,
            durationMinutes = duration,
            recognizedDate = dateResult.second,
            recognizedTime = timeResult.second,
        )
    }

    private fun parseDate(text: String, today: LocalDate): Pair<LocalDate, Boolean> {
        if (Regex("\\btomorrow\\b").containsMatchIn(text)) return today.plusDays(1) to true
        if (Regex("\\btoday\\b").containsMatchIn(text)) return today to true
        weekdays.entries.firstOrNull { day ->
            Regex("\\b${day.key}\\b").containsMatchIn(text)
        }?.let { (_, day) ->
            var date = today.with(TemporalAdjusters.nextOrSame(day))
            if (date == today) date = date.plusWeeks(1)
            return date to true
        }
        return today to false
    }

    private fun parseTime(text: String): Pair<LocalTime, Boolean> {
        val explicit = Regex("\\b(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)\\b")
            .find(text)
            ?: Regex("\\bat\\s+(\\d{1,2})(?::(\\d{2}))?\\b").find(text)
        if (explicit != null) {
            val rawHour = explicit.groupValues[1].toInt()
            val minute = explicit.groupValues[2].toIntOrNull() ?: 0
            val meridiem = explicit.groupValues.getOrNull(3).orEmpty()
            val hour = when {
                meridiem == "pm" && rawHour < 12 -> rawHour + 12
                meridiem == "am" && rawHour == 12 -> 0
                meridiem.isEmpty() && rawHour in 1..7 -> rawHour + 12
                else -> rawHour
            }
            if (hour in 0..23 && minute in 0..59) return LocalTime.of(hour, minute) to true
        }
        return when {
            Regex("\\bmorning\\b").containsMatchIn(text) -> LocalTime.of(9, 0) to true
            Regex("\\bafternoon\\b").containsMatchIn(text) -> LocalTime.of(14, 0) to true
            Regex("\\btonight\\b|\\bevening\\b").containsMatchIn(text) -> LocalTime.of(19, 0) to true
            else -> LocalTime.of(9, 0) to false
        }
    }

    private fun parseDuration(text: String): Int {
        Regex("\\bfor\\s+(\\d+(?:\\.\\d+)?|one|two|three|four)\\s*(hours?|hrs?|h)\\b").find(text)?.let {
            return (wordNumber(it.groupValues[1]) * 60).toInt().coerceAtLeast(15)
        }
        Regex("\\bfor\\s+(\\d+)\\s*(minutes?|mins?|m)\\b").find(text)?.let {
            return it.groupValues[1].toInt().coerceAtLeast(5)
        }
        return 45
    }

    private fun cleanTitle(text: String): String = text
        .replace(Regex("(?i)\\b(today|tomorrow|monday|tuesday|wednesday|thursday|friday|saturday|sunday)\\b"), "")
        .replace(Regex("(?i)\\b(at\\s+)?\\d{1,2}(?::\\d{2})?\\s*(am|pm)\\b"), "")
        .replace(Regex("(?i)\\bat\\s+\\d{1,2}(?::\\d{2})?\\b"), "")
        .replace(Regex("(?i)\\bfor\\s+(\\d+(?:\\.\\d+)?|one|two|three|four)\\s*(hours?|hrs?|h|minutes?|mins?|m)\\b"), "")
        .replace(Regex("(?i)\\b(morning|afternoon|tonight|evening)\\b"), "")
        .replace(Regex("\\s+"), " ")
        .trim(' ', ',', '.', '-')

    private fun wordNumber(value: String): Double = when (value) {
        "one" -> 1.0
        "two" -> 2.0
        "three" -> 3.0
        "four" -> 4.0
        else -> value.toDouble()
    }

    private companion object {
        val weekdays = linkedMapOf(
            "monday" to DayOfWeek.MONDAY,
            "tuesday" to DayOfWeek.TUESDAY,
            "wednesday" to DayOfWeek.WEDNESDAY,
            "thursday" to DayOfWeek.THURSDAY,
            "friday" to DayOfWeek.FRIDAY,
            "saturday" to DayOfWeek.SATURDAY,
            "sunday" to DayOfWeek.SUNDAY,
        )
    }
}
