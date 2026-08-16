package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.AcademicNoteDao
import com.sarah.app.data.local.dao.SubjectDao
import com.sarah.app.data.local.entity.AcademicNoteEntity
import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.repository.AcademicNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AcademicNoteRepositoryImpl(
    private val academicNoteDao: AcademicNoteDao,
    private val subjectDao: SubjectDao
) : AcademicNoteRepository {

    override fun getAllNotes(): Flow<List<AcademicNote>> {
        return combine(
            academicNoteDao.getAllNotes(),
            subjectDao.getAllSubjects()
        ) { noteEntities, subjectEntities ->
            val subjectMap = subjectEntities.associateBy { it.id }
            noteEntities.map { entity ->
                val subjectName = entity.subjectId?.let { subjectMap[it]?.name }
                entity.toDomain(subjectName)
            }
        }
    }

    override fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNote>> {
        return combine(
            academicNoteDao.getNotesBySubject(subjectId),
            subjectDao.getAllSubjects()
        ) { noteEntities, subjectEntities ->
            val subjectMap = subjectEntities.associateBy { it.id }
            noteEntities.map { entity ->
                val subjectName = entity.subjectId?.let { subjectMap[it]?.name }
                entity.toDomain(subjectName)
            }
        }
    }

    override suspend fun getNoteById(id: Long): AcademicNote? {
        val entity = academicNoteDao.getNoteById(id) ?: return null
        val subjectName = entity.subjectId?.let { subjectDao.getSubjectById(it)?.name }
        return entity.toDomain(subjectName)
    }

    override suspend fun insertNote(note: AcademicNote): Long {
        return academicNoteDao.insertNote(AcademicNoteEntity.fromDomain(note))
    }

    override suspend fun updateNote(note: AcademicNote) {
        academicNoteDao.updateNote(AcademicNoteEntity.fromDomain(note).copy(updatedEpochMs = System.currentTimeMillis()))
    }

    override suspend fun deleteNote(note: AcademicNote) {
        academicNoteDao.deleteNote(AcademicNoteEntity.fromDomain(note))
    }

    override suspend fun deleteNoteById(id: Long) {
        academicNoteDao.deleteNoteById(id)
    }

    override suspend fun togglePin(id: Long, isPinned: Boolean) {
        academicNoteDao.updatePinStatus(id, isPinned, System.currentTimeMillis())
    }
}
