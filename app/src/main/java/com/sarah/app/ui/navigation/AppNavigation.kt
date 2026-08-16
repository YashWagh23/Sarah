package com.sarah.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sarah.app.SarahApp
import com.sarah.app.ui.screens.notes.NotesScreen
import com.sarah.app.ui.screens.notes.NotesViewModel
import com.sarah.app.ui.screens.onboarding.OnboardingScreen
import com.sarah.app.ui.screens.onboarding.OnboardingViewModel
import com.sarah.app.ui.screens.profile.ProfileScreen
import com.sarah.app.ui.screens.profile.ProfileViewModel
import com.sarah.app.ui.screens.quickcapture.QuickCaptureViewModel
import com.sarah.app.ui.screens.schedule.ScheduleScreen
import com.sarah.app.ui.screens.schedule.ScheduleViewModel
import com.sarah.app.ui.screens.subjects.SubjectsScreen
import com.sarah.app.ui.screens.subjects.SubjectsViewModel
import com.sarah.app.ui.screens.tasks.TasksScreen
import com.sarah.app.ui.screens.tasks.TasksViewModel
import com.sarah.app.ui.screens.today.TodayScreen
import com.sarah.app.ui.screens.today.TodayViewModel
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import com.sarah.app.ui.theme.SarahSurfaceContainerLow
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahOutlineVariant

@Composable
fun AppNavigation(
    app: SarahApp,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val isOnboardingDone by app.preferencesManager.onboardingCompletedFlow.collectAsState(
        initial = app.preferencesManager.isOnboardingCompleted
    )

    val startDestination = if (isOnboardingDone) Screen.Today.route else Screen.Onboarding.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in Screen.bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                // Frosted-glass bottom nav bar matching the reference design.
                // Semi-transparent white simulates the backdrop-blur: white/78 in CSS.
                NavigationBar(
                    containerColor = SarahSurfaceContainerLowest.copy(alpha = 0.92f),
                    contentColor   = SarahPrimary,
                    tonalElevation = 0.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) {
                                        screen.iconFilled ?: screen.icon!!
                                    } else {
                                        screen.icon!!
                                    },
                                    contentDescription = screen.title,
                                )
                            },
                            label = {
                                Text(
                                    text  = screen.title,
                                    style = MaterialTheme.typography.labelSmall
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
                                selectedIconColor   = SarahPrimary,
                                selectedTextColor   = SarahPrimary,
                                unselectedIconColor = SarahSecondary,
                                unselectedTextColor = SarahSecondary,
                                // Pill indicator behind active icon
                                indicatorColor      = SarahSurfaceContainerLow
                            )
                        )
                    }
                }
            }
        },
        containerColor = SarahBackground
    ) { innerPadding ->
        NavHost(
            navController     = navController,
            startDestination  = startDestination,
            modifier          = modifier
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
                        app.feasibilityEngine,
                        app.dailyPlanRepository,
                        app.adaptivePlanner,
                        app.nextActionEngine,
                        app.reminderRepository,
                        app.reminderScheduler,
                        app.deadlineReminderEngine
                    )
                )
                val quickCaptureViewModel: QuickCaptureViewModel = viewModel(
                    factory = QuickCaptureViewModel.Factory(
                        app.taskRepository,
                        app.subjectRepository,
                        app.naturalLanguageTaskParser,
                        app.documentTextExtractor,
                        app.reminderRepository,
                        app.reminderScheduler,
                        app.deadlineReminderEngine,
                        app.preferencesManager
                    )
                )
                TodayScreen(
                    viewModel            = todayViewModel,
                    quickCaptureViewModel = quickCaptureViewModel,
                    onNavigateToNotes    = { navController.navigate(Screen.Notes.route) }
                )
            }

            composable(Screen.Tasks.route) {
                val tasksViewModel: TasksViewModel = viewModel(
                    factory = TasksViewModel.Factory(
                        app.taskRepository,
                        app.subjectRepository,
                        app.reminderRepository,
                        app.reminderScheduler,
                        app.deadlineReminderEngine,
                        app.preferencesManager
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
                    factory = ProfileViewModel.Factory(app.userRepository, app.preferencesManager)
                )
                ProfileScreen(
                    viewModel         = profileViewModel,
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) }
                )
            }

            composable(Screen.Notes.route) {
                val notesViewModel: NotesViewModel = viewModel(
                    factory = NotesViewModel.Factory(
                        app.academicNoteRepository,
                        app.subjectRepository,
                        app.taskRepository,
                        app.reminderRepository,
                        app.reminderScheduler,
                        app.deadlineReminderEngine,
                        app.preferencesManager
                    )
                )
                NotesScreen(
                    viewModel       = notesViewModel,
                    onNavigateBack  = { navController.popBackStack() }
                )
            }
        }
    }
}
