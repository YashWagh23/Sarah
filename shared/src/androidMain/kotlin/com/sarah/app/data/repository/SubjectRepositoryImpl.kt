package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.SubjectDao
import com.sarah.app.data.local.entity.SubjectEntity
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SubjectRepositoryImpl(
    private val subjectDao: SubjectDao
) : SubjectRepository {

    override fun getAllSubjects(): Flow<List<Subject>> {
        return subjectDao.getAllSubjects().map { list -> list.map { it.toDomain() } }
    }

    override fun getActiveSubjects(): Flow<List<Subject>> {
        return subjectDao.getActiveSubjects().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSubjectById(id: Long): Subject? {
        return subjectDao.getSubjectById(id)?.toDomain()
    }

    override suspend fun insertSubject(subject: Subject): Long {
        return subjectDao.insertSubject(SubjectEntity.fromDomain(subject))
    }

    override suspend fun updateSubject(subject: Subject) {
        subjectDao.updateSubject(SubjectEntity.fromDomain(subject))
    }

    override suspend fun deleteSubject(subject: Subject) {
        subjectDao.deleteSubject(SubjectEntity.fromDomain(subject))
    }
}
