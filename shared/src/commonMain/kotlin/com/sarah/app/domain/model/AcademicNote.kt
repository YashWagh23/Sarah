package com.sarah.app.domain.model

data class AcademicNote(
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String? = null,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdEpochMs: Long = System.currentTimeMillis(),
    val updatedEpochMs: Long = System.currentTimeMillis()
)
