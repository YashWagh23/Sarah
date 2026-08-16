package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.repository.AcademicNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosAcademicNoteRepository(
    private val database: IosSarahDatabase
) : AcademicNoteRepository {

    override fun getAllNotes(): Flow<List<AcademicNote>> {
        return database.academicNotesFlow
    }

    override fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNote>> {
        return database.academicNotesFlow.map { list ->
            list.filter { it.subjectId == subjectId }
        }
    }

    override suspend fun getNoteById(id: Long): AcademicNote? {
        return database.academicNotesFlow.value.find { it.id == id }
    }

    override suspend fun insertNote(note: AcademicNote): Long {
        val currentList = database.academicNotesFlow.value.toMutableList()
        val newId = if (note.id > 0) note.id else (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = note.copy(id = newId)
        val existingIndex = currentList.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = toInsert
        } else {
            currentList.add(toInsert)
        }
        database.academicNotesFlow.value = currentList
        database.saveAcademicNotes()
        return newId
    }

    override suspend fun updateNote(note: AcademicNote) {
        val currentList = database.academicNotesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == note.id }
        if (index >= 0) {
            currentList[index] = note
            database.academicNotesFlow.value = currentList
            database.saveAcademicNotes()
        }
    }

    override suspend fun deleteNote(note: AcademicNote) {
        deleteNoteById(note.id)
    }

    override suspend fun deleteNoteById(id: Long) {
        val currentList = database.academicNotesFlow.value.toMutableList()
        val removed = currentList.removeAll { it.id == id }
        if (removed) {
            database.academicNotesFlow.value = currentList
            database.saveAcademicNotes()
        }
    }

    override suspend fun togglePin(id: Long, isPinned: Boolean) {
        val currentList = database.academicNotesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == id }
        if (index >= 0) {
            val existing = currentList[index]
            currentList[index] = existing.copy(isPinned = isPinned)
            database.academicNotesFlow.value = currentList
            database.saveAcademicNotes()
        }
    }
}
