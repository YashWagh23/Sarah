package com.sarah.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.rounded.Description
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val iconFilled: ImageVector? = null
) {
    object Today : Screen(
        route = "today",
        title = "Today",
        icon = Icons.Outlined.Today,
        iconFilled = Icons.Filled.Today
    )
    object Tasks : Screen(
        route = "tasks",
        title = "Tasks",
        icon = Icons.Outlined.Assignment,
        iconFilled = Icons.Filled.Assignment
    )
    object Subjects : Screen(
        route = "subjects",
        title = "Subjects",
        icon = Icons.Outlined.MenuBook,
        iconFilled = Icons.Filled.MenuBook
    )
    object Schedule : Screen(
        route = "schedule",
        title = "Schedule",
        icon = Icons.Outlined.CalendarToday,
        iconFilled = Icons.Filled.CalendarToday
    )
    object Profile : Screen(
        route = "profile",
        title = "Profile",
        icon = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )
    object Notes : Screen("notes", "Notes", Icons.Rounded.Description)
    object Onboarding : Screen("onboarding", "Onboarding")

    companion object {
        val bottomNavItems by lazy { listOf(Today, Tasks, Subjects, Schedule, Profile) }
    }
}
