package com.sarah.app.domain.repository

import com.sarah.app.domain.model.AcademicNote
import kotlinx.coroutines.flow.Flow

interface AcademicNoteRepository {
    fun getAllNotes(): Flow<List<AcademicNote>>
    fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNote>>
    suspend fun getNoteById(id: Long): AcademicNote?
    suspend fun insertNote(note: AcademicNote): Long
    suspend fun updateNote(note: AcademicNote)
    suspend fun deleteNote(note: AcademicNote)
    suspend fun deleteNoteById(id: Long)
    suspend fun togglePin(id: Long, isPinned: Boolean)
}
