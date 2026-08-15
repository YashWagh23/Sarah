package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.TemporaryInterruption

@Entity(tableName = "temporary_interruptions")
data class TemporaryInterruptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val startMinutes: Int,
    val endMinutes: Int,
    val dateEpochDay: Long
) {
    fun toDomain(): TemporaryInterruption {
        return TemporaryInterruption(
            id = id,
            title = title,
            startMinutes = startMinutes,
            endMinutes = endMinutes,
            dateEpochDay = dateEpochDay
        )
    }

    companion object {
        fun fromDomain(interruption: TemporaryInterruption): TemporaryInterruptionEntity {
            return TemporaryInterruptionEntity(
                id = interruption.id,
                title = interruption.title,
                startMinutes = interruption.startMinutes,
                endMinutes = interruption.endMinutes,
                dateEpochDay = interruption.dateEpochDay
            )
        }
    }
}
