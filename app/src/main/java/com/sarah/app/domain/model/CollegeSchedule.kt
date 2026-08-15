package com.sarah.app.domain.model

data class CollegeSchedule(
    val id: Long = 1,
    val wakeTimeMinutes: Int = 7 * 60, // 07:00 AM (420)
    val sleepTimeMinutes: Int = 23 * 60 + 30, // 11:30 PM (1410)
    val collegeStartTimeMinutes: Int = 9 * 60, // 09:00 AM (540)
    val collegeEndTimeMinutes: Int = 16 * 60 + 30, // 04:30 PM (990)
    val commuteMinutes: Int = 45,
    val dinnerBufferMinutes: Int = 45,
    val breakDurationMinutes: Int = 15,
    val preferredSessionLengthMinutes: Int = 45
)
