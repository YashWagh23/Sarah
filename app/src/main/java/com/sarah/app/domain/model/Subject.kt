package com.sarah.app.domain.model

data class Subject(
    val id: Long = 0,
    val name: String,
    val code: String = "",
    val professorName: String = "",
    val colorHex: String = "#7C4DFF",
    val weeklyHours: Int = 4,
    val targetAttendancePercentage: Int = 75,
    val currentAttendancePercentage: Int = 100,
    val isActive: Boolean = true
)
