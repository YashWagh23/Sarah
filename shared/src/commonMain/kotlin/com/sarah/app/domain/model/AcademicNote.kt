package com.sarah.app.domain.model

import com.sarah.app.domain.util.currentTimeEpochMs

data class AcademicNote(
    val id: Long = 0,
    val subjectId: Long? = null,
    val subjectName: String? = null,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdEpochMs: Long = currentTimeEpochMs(),
    val updatedEpochMs: Long = currentTimeEpochMs()
)
