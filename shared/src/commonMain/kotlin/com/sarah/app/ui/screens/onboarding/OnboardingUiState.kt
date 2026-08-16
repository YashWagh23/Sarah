package com.sarah.app.ui.screens.onboarding

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val collegeName: String = "",
    val department: String = "",
    val semesterYear: String = "3rd Year",
    val sleepTimeMinutes: Int = 23 * 60 + 30, // 11:30 PM
    val collegeStartTimeMinutes: Int = 9 * 60, // 9:00 AM
    val collegeEndTimeMinutes: Int = 16 * 60 + 30, // 4:30 PM
    val isCompleted: Boolean = false
)
