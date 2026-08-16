package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosSubjectRepository(
    private val database: IosSarahDatabase
) : SubjectRepository {

    override fun getAllSubjects(): Flow<List<Subject>> {
        return database.subjectsFlow
    }

    override fun getActiveSubjects(): Flow<List<Subject>> {
        return database.subjectsFlow.map { list ->
            list.filter { it.isActive }
        }
    }

    override suspend fun getSubjectById(id: Long): Subject? {
        return database.subjectsFlow.value.find { it.id == id }
    }

    override suspend fun insertSubject(subject: Subject): Long {
        val currentList = database.subjectsFlow.value.toMutableList()
        val newId = if (subject.id > 0) subject.id else (currentList.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = subject.copy(id = newId)
        val existingIndex = currentList.indexOfFirst { it.id == newId }
        if (existingIndex >= 0) {
            currentList[existingIndex] = toInsert
        } else {
            currentList.add(toInsert)
        }
        database.subjectsFlow.value = currentList
        database.saveSubjects()
        return newId
    }

    override suspend fun updateSubject(subject: Subject) {
        val currentList = database.subjectsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == subject.id }
        if (index >= 0) {
            currentList[index] = subject
            database.subjectsFlow.value = currentList
            database.saveSubjects()
        }
    }

    override suspend fun deleteSubject(subject: Subject) {
        val currentList = database.subjectsFlow.value.toMutableList()
        val removed = currentList.removeAll { it.id == subject.id }
        if (removed) {
            database.subjectsFlow.value = currentList
            database.saveSubjects()
        }
    }
}
