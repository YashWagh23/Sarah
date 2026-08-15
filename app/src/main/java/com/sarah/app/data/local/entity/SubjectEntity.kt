package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.Subject

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val professorName: String,
    val colorHex: String,
    val weeklyHours: Int,
    val targetAttendancePercentage: Int,
    val currentAttendancePercentage: Int,
    val isActive: Boolean
) {
    fun toDomain(): Subject {
        return Subject(
            id = id,
            name = name,
            code = code,
            professorName = professorName,
            colorHex = colorHex,
            weeklyHours = weeklyHours,
            targetAttendancePercentage = targetAttendancePercentage,
            currentAttendancePercentage = currentAttendancePercentage,
            isActive = isActive
        )
    }

    companion object {
        fun fromDomain(subject: Subject): SubjectEntity {
            return SubjectEntity(
                id = subject.id,
                name = subject.name,
                code = subject.code,
                professorName = subject.professorName,
                colorHex = subject.colorHex,
                weeklyHours = subject.weeklyHours,
                targetAttendancePercentage = subject.targetAttendancePercentage,
                currentAttendancePercentage = subject.currentAttendancePercentage,
                isActive = subject.isActive
            )
        }
    }
}
