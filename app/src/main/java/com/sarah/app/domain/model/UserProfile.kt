package com.sarah.app.domain.model

data class UserProfile(
    val id: Long = 1,
    val name: String = "Student",
    val collegeName: String = "",
    val department: String = "",
    val semesterYear: String = "",
    val isOnboardingCompleted: Boolean = false,
    val defaultEnergyLevel: EnergyLevel = EnergyLevel.NORMAL
)
