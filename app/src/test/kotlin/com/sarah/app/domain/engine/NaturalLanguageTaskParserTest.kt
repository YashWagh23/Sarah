package com.sarah.app.domain.engine

import com.sarah.app.domain.model.CaptureSourceType
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

class NaturalLanguageTaskParserTest {

    private lateinit var parser: NaturalLanguageTaskParser
    private lateinit var subjects: List<Subject>
    private val zone = TimeZone.UTC
    private val fixedDate = LocalDate(2026, 8, 15) // Saturday

    @Before
    fun setup() {
        parser = NaturalLanguageTaskParser()
        subjects = listOf(
            Subject(id = 1, name = "Java & OOP", code = "CS301"),
            Subject(id = 2, name = "Database Management", code = "CS302"),
            Subject(id = 3, name = "Operating Systems", code = "CS303")
        )
    }

    @Test
    fun `test parse practical task with professor instruction and Monday deadline`() {
        val input = "Sir gave 3 Java programs. Submit Monday."
        val draft = parser.parse(
            rawText = input,
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        assertEquals("Java & OOP", draft.subjectName)
        assertEquals(1L, draft.subjectId)
        assertEquals(TaskType.PRACTICAL, draft.type)
        assertEquals(TaskPriority.HIGH, draft.priority)
        assertEquals(50, draft.estimatedMinutes)
        assertEquals(EnergyRequirement.HIGH, draft.energyRequirement)
        assertTrue(draft.title.contains("3 Java programs"))
    }

    @Test
    fun `test parse DBMS assignment with explicit Friday 5 PM deadline`() {
        val input = "DBMS assignment due Friday by 5 PM"
        val draft = parser.parse(
            rawText = input,
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        assertEquals(TaskType.ASSIGNMENT, draft.type)
        assertEquals(45, draft.estimatedMinutes)
        assertTrue(draft.deadlineEpochMs > 0)
    }

    @Test
    fun `test parse quiz with tomorrow deadline and exam prep type`() {
        val input = "Tomorrow there is a quiz on normalization"
        val draft = parser.parse(
            rawText = input,
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        assertEquals(TaskType.EXAM_PREP, draft.type)
        assertEquals(60, draft.estimatedMinutes)
        assertEquals(EnergyRequirement.HIGH, draft.energyRequirement)
        assertTrue(draft.title.contains("Quiz on normalization"))
    }

    @Test
    fun `test parse practical record book with Friday deadline`() {
        val input = "Sir said bring record book on Friday."
        val draft = parser.parse(
            rawText = input,
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        assertEquals(TaskType.PRACTICAL, draft.type)
        assertEquals(TaskPriority.HIGH, draft.priority)
    }

    @Test
    fun `test parse explicit duration hours and minutes`() {
        val input = "Revise operating systems process scheduling for 1.5 hours"
        val draft = parser.parse(
            rawText = input,
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        assertEquals("Operating Systems", draft.subjectName)
        assertEquals(TaskType.REVISION, draft.type)
        assertEquals(90, draft.estimatedMinutes)
        assertEquals(EnergyRequirement.LOW, draft.energyRequirement)
    }

    @Test
    fun `test toTask conversion produces valid domain Task`() {
        val draft = parser.parse(
            rawText = "Complete 5 Java programs by Monday",
            availableSubjects = subjects,
            sourceType = CaptureSourceType.NATURAL_LANGUAGE,
            currentDate = fixedDate,
            timeZone = zone
        )

        val task = draft.toTask()
        assertNotNull(task)
        assertEquals(draft.title, task.title)
        assertEquals(draft.subjectId, task.subjectId)
        assertEquals(draft.estimatedMinutes, task.estimatedMinutes)
        assertEquals(draft.deadlineEpochMs, task.deadlineEpochMs)
    }
}
