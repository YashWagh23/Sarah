package com.sarah.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcademicNoteModelTest {

    @Test
    fun `test default academic note values`() {
        val note = AcademicNote(
            title = "Mid-term Syllabus",
            content = "Units 1 and 2 only"
        )

        assertEquals(0L, note.id)
        assertNull(note.subjectId)
        assertNull(note.subjectName)
        assertEquals("Mid-term Syllabus", note.title)
        assertEquals("Units 1 and 2 only", note.content)
        assertFalse(note.isPinned)
        assertTrue(note.createdEpochMs > 0)
        assertTrue(note.updatedEpochMs > 0)
    }

    @Test
    fun `test pinned academic note with subject tag`() {
        val note = AcademicNote(
            id = 42L,
            subjectId = 3L,
            subjectName = "Database Management",
            title = "Lab Viva Guidelines",
            content = "Viva will cover experiments 1-5; code in SQL",
            isPinned = true
        )

        assertEquals(42L, note.id)
        assertEquals(3L, note.subjectId)
        assertEquals("Database Management", note.subjectName)
        assertEquals("Lab Viva Guidelines", note.title)
        assertEquals("Viva will cover experiments 1-5; code in SQL", note.content)
        assertTrue(note.isPinned)
    }

    @Test
    fun `test note copy updates timestamp and pin status`() {
        val initialTime = 1000L
        val note = AcademicNote(
            id = 1L,
            title = "Class Notice",
            content = "Old content",
            isPinned = false,
            createdEpochMs = initialTime,
            updatedEpochMs = initialTime
        )

        val updatedTime = 2000L
        val updated = note.copy(
            content = "Updated content",
            isPinned = true,
            updatedEpochMs = updatedTime
        )

        assertEquals("Updated content", updated.content)
        assertTrue(updated.isPinned)
        assertEquals(initialTime, updated.createdEpochMs)
        assertEquals(updatedTime, updated.updatedEpochMs)
    }
}
