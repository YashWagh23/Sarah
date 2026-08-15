package com.sarah.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sarah.app.SarahApp
import com.sarah.app.ui.screens.onboarding.OnboardingScreen
import com.sarah.app.ui.screens.onboarding.OnboardingViewModel
import com.sarah.app.ui.screens.profile.ProfileScreen
import com.sarah.app.ui.screens.profile.ProfileViewModel
import com.sarah.app.ui.screens.schedule.ScheduleScreen
import com.sarah.app.ui.screens.schedule.ScheduleViewModel
import com.sarah.app.ui.screens.subjects.SubjectsScreen
import com.sarah.app.ui.screens.subjects.SubjectsViewModel
import com.sarah.app.ui.screens.tasks.TasksScreen
import com.sarah.app.ui.screens.tasks.TasksViewModel
import com.sarah.app.ui.screens.today.TodayScreen
import com.sarah.app.ui.screens.today.TodayViewModel
import com.sarah.app.ui.theme.CyanAccent
import com.sarah.app.ui.theme.DarkBackground
import com.sarah.app.ui.theme.DarkBorder
import com.sarah.app.ui.theme.DarkSurface
import com.sarah.app.ui.theme.ElectricIndigo
import com.sarah.app.ui.theme.TextMuted
import com.sarah.app.ui.theme.TextPrimary
import com.sarah.app.ui.theme.TextSecondary

@Composable
fun AppNavigation(
    app: SarahApp,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val isOnboardingDone by app.preferencesManager.onboardingCompletedFlow.collectAsState(initial = app.preferencesManager.isOnboardingCompleted)

    val startDestination = if (isOnboardingDone) Screen.Today.route else Screen.Onboarding.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    containerColor = DarkSurface,
                    contentColor = TextPrimary
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title,
                                        tint = if (isSelected) CyanAccent else TextMuted
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    color = if (isSelected) TextPrimary else TextMuted
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = ElectricIndigo.copy(alpha = 0.25f)
                            )
                        )
                    }
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Onboarding.route) {
                val onboardingViewModel: OnboardingViewModel = viewModel(
                    factory = OnboardingViewModel.Factory(
                        app.userRepository,
                        app.scheduleRepository,
                        app.preferencesManager
                    )
                )
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate(Screen.Today.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Today.route) {
                val todayViewModel: TodayViewModel = viewModel(
                    factory = TodayViewModel.Factory(
                        app.taskRepository,
                        app.subjectRepository,
                        app.scheduleRepository,
                        app.userRepository,
                        app.preferencesManager,
                        app.feasibilityEngine
                    )
                )
                TodayScreen(
                    viewModel = todayViewModel,
                    onQuickAddClick = {
                        navController.navigate(Screen.Tasks.route)
                    }
                )
            }

            composable(Screen.Tasks.route) {
                val tasksViewModel: TasksViewModel = viewModel(
                    factory = TasksViewModel.Factory(
                        app.taskRepository,
                        app.subjectRepository
                    )
                )
                TasksScreen(viewModel = tasksViewModel)
            }

            composable(Screen.Subjects.route) {
                val subjectsViewModel: SubjectsViewModel = viewModel(
                    factory = SubjectsViewModel.Factory(
                        app.subjectRepository,
                        app.taskRepository
                    )
                )
                SubjectsScreen(viewModel = subjectsViewModel)
            }

            composable(Screen.Schedule.route) {
                val scheduleViewModel: ScheduleViewModel = viewModel(
                    factory = ScheduleViewModel.Factory(app.scheduleRepository)
                )
                ScheduleScreen(viewModel = scheduleViewModel)
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(app.userRepository)
                )
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
