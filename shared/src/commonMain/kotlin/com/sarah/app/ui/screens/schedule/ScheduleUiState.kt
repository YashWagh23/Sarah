package com.sarah.app.ui.screens.schedule

import com.sarah.app.domain.model.CollegeSchedule

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val schedule: CollegeSchedule = CollegeSchedule(),
    val isSaved: Boolean = false
)
