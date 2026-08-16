package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CaptureSourceType
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.ExtractedTaskDraft
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class NaturalLanguageTaskParser {

    fun parse(
        rawText: String,
        availableSubjects: List<Subject>,
        sourceType: CaptureSourceType = CaptureSourceType.NATURAL_LANGUAGE,
        currentDate: LocalDate? = null,
        timeZone: TimeZone = TimeZone.currentSystemDefault()
    ): ExtractedTaskDraft {
        val cleanInput = rawText.trim()
        val lower = cleanInput.lowercase()

        // 1. Identify Subject
        val matchedSubject = findMatchingSubject(lower, availableSubjects)

        // 2. Identify Task Type
        val taskType = detectTaskType(lower)

        // 3. Identify Deadline
        val parsedDeadline = DateTimeParserHelper.parseDeadline(lower, currentDate, timeZone)
            ?: (Clock.System.now().toEpochMilliseconds() + 24 * 60 * 60 * 1000L) // Default to tomorrow

        // 4. Identify Estimated Duration
        val estimatedMinutes = parseDurationMinutes(lower) ?: when (taskType) {
            TaskType.PRACTICAL -> 50
            TaskType.ASSIGNMENT -> 45
            TaskType.PROJECT -> 90
            TaskType.EXAM_PREP -> 60
            TaskType.REVISION -> 30
            TaskType.READING -> 25
            TaskType.SUBMISSION -> 20
            TaskType.OTHER -> 45
        }

        // 5. Identify Priority
        val priority = detectPriority(lower)

        // 6. Identify Energy Requirement & Difficulty
        val energy = when (taskType) {
            TaskType.PRACTICAL, TaskType.PROJECT, TaskType.EXAM_PREP -> EnergyRequirement.HIGH
            TaskType.ASSIGNMENT, TaskType.SUBMISSION -> EnergyRequirement.MEDIUM
            TaskType.REVISION, TaskType.READING, TaskType.OTHER -> EnergyRequirement.LOW
        }

        val difficulty = when {
            lower.contains("difficult") || lower.contains("hard") || lower.contains("complex") -> Difficulty.HARD
            lower.contains("easy") || lower.contains("simple") || lower.contains("quick") -> Difficulty.EASY
            else -> Difficulty.MEDIUM
        }

        // 7. Clean up Title
        val title = cleanTitle(cleanInput, matchedSubject, taskType)

        return ExtractedTaskDraft(
            title = title,
            subjectId = matchedSubject?.id,
            subjectName = matchedSubject?.name,
            type = taskType,
            description = cleanInput,
            deadlineEpochMs = parsedDeadline,
            estimatedMinutes = estimatedMinutes,
            priority = priority,
            difficulty = difficulty,
            energyRequirement = energy,
            confidenceScore = if (matchedSubject != null) 0.95f else 0.8f,
            sourceType = sourceType,
            rawExtractedText = rawText
        )
    }

    private fun findMatchingSubject(lowerText: String, subjects: List<Subject>): Subject? {
        for (subject in subjects) {
            val nameLower = subject.name.lowercase()
            val codeLower = subject.code.lowercase()

            // Direct substring match
            if (nameLower.isNotBlank() && lowerText.contains(nameLower)) return subject
            if (codeLower.isNotBlank() && lowerText.contains(codeLower)) return subject

            // Split name into words (e.g. "Java & OOP" -> "java", "oop")
            val words = nameLower.split(Regex("[^a-zA-Z0-9]+")).filter { it.length > 2 }
            for (w in words) {
                val wordRegex = Regex("\\b$w\\b")
                if (wordRegex.containsMatchIn(lowerText)) {
                    return subject
                }
            }
        }
        return null
    }

    private fun detectTaskType(lower: String): TaskType {
        return when {
            lower.contains("practical") || lower.contains("program") || lower.contains("lab") || lower.contains("record") -> TaskType.PRACTICAL
            lower.contains("quiz") || lower.contains("test") || lower.contains("exam") || lower.contains("midterm") -> TaskType.EXAM_PREP
            lower.contains("revise") || lower.contains("revision") || lower.contains("recap") -> TaskType.REVISION
            lower.contains("read") || lower.contains("chapter") || lower.contains("article") -> TaskType.READING
            lower.contains("project") || lower.contains("presentation") -> TaskType.PROJECT
            lower.contains("submit") || lower.contains("submission") || lower.contains("turn in") -> TaskType.SUBMISSION
            lower.contains("assignment") || lower.contains("homework") || lower.contains("exercise") -> TaskType.ASSIGNMENT
            else -> TaskType.ASSIGNMENT
        }
    }

    private fun parseDurationMinutes(lower: String): Int? {
        // e.g. "1 hour", "2 hours", "1.5 hrs", "90 mins", "45 min"
        val hourMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*(?:hours?|hrs?|h)\\b").find(lower)
        if (hourMatch != null) {
            val h = hourMatch.groupValues[1].toFloatOrNull() ?: 1f
            return (h * 60).toInt()
        }

        val minMatch = Regex("(\\d+)\\s*(?:mins?|minutes?|m)\\b").find(lower)
        if (minMatch != null) {
            return minMatch.groupValues[1].toIntOrNull()
        }

        return null
    }

    private fun detectPriority(lower: String): TaskPriority {
        return when {
            lower.contains("critical") || lower.contains("urgent") || lower.contains("asap") || lower.contains("mandatory") -> TaskPriority.CRITICAL
            lower.contains("sir said") || lower.contains("sir gave") || lower.contains("important") || lower.contains("must") || lower.contains("due tomorrow") -> TaskPriority.HIGH
            lower.contains("optional") || lower.contains("low priority") || lower.contains("whenever") -> TaskPriority.LOW
            else -> TaskPriority.MEDIUM
        }
    }

    private fun cleanTitle(raw: String, subject: Subject?, type: TaskType): String {
        var result = raw

        // Remove filler prefixes
        val prefixesToRemove = listOf(
            Regex("(?i)^sir\\s+(said|gave|told us to)\\s+"),
            Regex("(?i)^teacher\\s+(said|gave|told us to)\\s+"),
            Regex("(?i)^prof(essor)?\\s+(said|gave|told us to)\\s+"),
            Regex("(?i)^i\\s+need\\s+to\\s+"),
            Regex("(?i)^need\\s+to\\s+"),
            Regex("(?i)^today('s)?\\s+"),
            Regex("(?i)^tomorrow\\s+there\\s+is\\s+a\\s+")
        )

        for (pattern in prefixesToRemove) {
            result = result.replace(pattern, "")
        }

        // Capitalize first letter
        result = result.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

        if (result.length < 5) {
            val subjName = subject?.name ?: "Academic"
            result = "$subjName ${type.displayName}"
        }

        return result
    }
}
