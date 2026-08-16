package com.sarah.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Assignment
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material.icons.rounded.Description

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Today : Screen("today", "Today", Icons.Rounded.Dashboard)
    object Tasks : Screen("tasks", "Tasks", Icons.Rounded.Assignment)
    object Subjects : Screen("subjects", "Subjects", Icons.Rounded.School)
    object Schedule : Screen("schedule", "Schedule", Icons.Rounded.Schedule)
    object Profile : Screen("profile", "Profile", Icons.Rounded.Person)
    object Notes : Screen("notes", "Notes", Icons.Rounded.Description)
    object Onboarding : Screen("onboarding", "Onboarding")

    companion object {
        val bottomNavItems by lazy { listOf(Today, Tasks, Subjects, Schedule, Profile) }
    }
}
