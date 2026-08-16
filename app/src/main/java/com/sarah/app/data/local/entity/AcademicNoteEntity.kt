package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.AcademicNote

@Entity(
    tableName = "academic_notes",
    foreignKeys = [
        ForeignKey(
            entity = SubjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class AcademicNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long?,
    val title: String,
    val content: String,
    val isPinned: Boolean = false,
    val createdEpochMs: Long = System.currentTimeMillis(),
    val updatedEpochMs: Long = System.currentTimeMillis()
) {
    fun toDomain(subjectName: String? = null): AcademicNote {
        return AcademicNote(
            id = id,
            subjectId = subjectId,
            subjectName = subjectName,
            title = title,
            content = content,
            isPinned = isPinned,
            createdEpochMs = createdEpochMs,
            updatedEpochMs = updatedEpochMs
        )
    }

    companion object {
        fun fromDomain(note: AcademicNote): AcademicNoteEntity {
            return AcademicNoteEntity(
                id = note.id,
                subjectId = note.subjectId,
                title = note.title,
                content = note.content,
                isPinned = note.isPinned,
                createdEpochMs = note.createdEpochMs,
                updatedEpochMs = note.updatedEpochMs
            )
        }
    }
}
