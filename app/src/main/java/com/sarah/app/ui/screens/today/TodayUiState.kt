package com.sarah.app.ui.screens.today

import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.FeasibilityReport
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.UserProfile

import com.sarah.app.domain.model.Reminder

data class TodayUiState(
    val isLoading: Boolean = true,
    val greeting: String = "Good Evening",
    val userProfile: UserProfile? = null,
    val schedule: CollegeSchedule = CollegeSchedule(),
    val energyLevel: EnergyLevel = EnergyLevel.NORMAL,
    val tasks: List<Task> = emptyList(),
    val subjects: List<Subject> = emptyList(),
    val upcomingReminders: List<Reminder> = emptyList(),
    val dailySummary: String = "",
    val humanFeasibilityHeadline: String = "",
    val humanFeasibilitySubtext: String = "",
    val humanCapacitySummary: String = "",
    val feasibilityReport: FeasibilityReport? = null,
    val dailyPlan: DailyPlan? = null,
    val nextAction: NextAction? = null
)
