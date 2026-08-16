package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.CollegeSchedule

@Entity(tableName = "schedule")
data class ScheduleEntity(
    @PrimaryKey
    val id: Long = 1,
    val wakeTimeMinutes: Int,
    val sleepTimeMinutes: Int,
    val collegeStartTimeMinutes: Int,
    val collegeEndTimeMinutes: Int,
    val commuteMinutes: Int,
    val dinnerBufferMinutes: Int,
    val breakDurationMinutes: Int,
    val preferredSessionLengthMinutes: Int
) {
    fun toDomain(): CollegeSchedule {
        return CollegeSchedule(
            id = id,
            wakeTimeMinutes = wakeTimeMinutes,
            sleepTimeMinutes = sleepTimeMinutes,
            collegeStartTimeMinutes = collegeStartTimeMinutes,
            collegeEndTimeMinutes = collegeEndTimeMinutes,
            commuteMinutes = commuteMinutes,
            dinnerBufferMinutes = dinnerBufferMinutes,
            breakDurationMinutes = breakDurationMinutes,
            preferredSessionLengthMinutes = preferredSessionLengthMinutes
        )
    }

    companion object {
        fun fromDomain(schedule: CollegeSchedule): ScheduleEntity {
            return ScheduleEntity(
                id = schedule.id,
                wakeTimeMinutes = schedule.wakeTimeMinutes,
                sleepTimeMinutes = schedule.sleepTimeMinutes,
                collegeStartTimeMinutes = schedule.collegeStartTimeMinutes,
                collegeEndTimeMinutes = schedule.collegeEndTimeMinutes,
                commuteMinutes = schedule.commuteMinutes,
                dinnerBufferMinutes = schedule.dinnerBufferMinutes,
                breakDurationMinutes = schedule.breakDurationMinutes,
                preferredSessionLengthMinutes = schedule.preferredSessionLengthMinutes
            )
        }
    }
}
