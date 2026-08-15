package com.sarah.app.domain.repository

import com.sarah.app.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun getAllSubjects(): Flow<List<Subject>>
    fun getActiveSubjects(): Flow<List<Subject>>
    suspend fun getSubjectById(id: Long): Subject?
    suspend fun insertSubject(subject: Subject): Long
    suspend fun updateSubject(subject: Subject)
    suspend fun deleteSubject(subject: Subject)
}
