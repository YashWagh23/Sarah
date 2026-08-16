package com.sarah.app.ui

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sarah.app.di.SarahAppContainer
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.util.currentTimeEpochMs
import com.sarah.app.ui.components.AddAcademicNoteSheet
import com.sarah.app.ui.navigation.Screen
import com.sarah.app.ui.screens.notes.NotesScreenContent
import com.sarah.app.ui.screens.notes.NotesUiState
import com.sarah.app.ui.screens.onboarding.OnboardingScreenContent
import com.sarah.app.ui.screens.onboarding.OnboardingUiState
import com.sarah.app.ui.screens.profile.ProfileScreenContent
import com.sarah.app.ui.screens.profile.ProfileUiState
import com.sarah.app.ui.screens.schedule.ScheduleScreenContent
import com.sarah.app.ui.screens.schedule.ScheduleUiState
import com.sarah.app.ui.screens.subjects.AddEditSubjectDialog
import com.sarah.app.ui.screens.subjects.SubjectFilterMode
import com.sarah.app.ui.screens.subjects.SubjectsScreenContent
import com.sarah.app.ui.screens.subjects.SubjectsUiState
import com.sarah.app.ui.screens.tasks.AddEditTaskDialog
import com.sarah.app.ui.screens.tasks.TasksScreenContent
import com.sarah.app.ui.screens.tasks.TasksUiState
import com.sarah.app.ui.screens.today.TodayScreenContent
import com.sarah.app.ui.screens.today.TodayUiState
import com.sarah.app.ui.theme.SarahBackground
import com.sarah.app.ui.theme.SarahPrimary
import com.sarah.app.ui.theme.SarahSecondary
import com.sarah.app.ui.theme.SarahSurfaceContainerLow
import com.sarah.app.ui.theme.SarahSurfaceContainerLowest
import kotlinx.coroutines.launch

@Composable
fun IosAppNavigation(
    container: SarahAppContainer,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val isOnboardingCompleted by container.preferences.onboardingCompletedFlow.collectAsState(
        initial = container.preferences.isOnboardingCompleted
    )

    var currentScreen by remember(isOnboardingCompleted) {
        mutableStateOf<Screen>(if (isOnboardingCompleted) Screen.Today else Screen.Onboarding)
    }

    val showBottomBar = currentScreen in Screen.bottomNavItems

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = SarahSurfaceContainerLowest.copy(alpha = 0.92f),
                    contentColor = SarahPrimary,
                    tonalElevation = 0.dp
                ) {
                    Screen.bottomNavItems.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) (screen.iconFilled ?: screen.icon!!) else screen.icon!!,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                currentScreen = screen
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = SarahPrimary,
                                selectedTextColor = SarahPrimary,
                                unselectedIconColor = SarahSecondary,
                                unselectedTextColor = SarahSecondary,
                                indicatorColor = SarahSurfaceContainerLow
                            )
                        )
                    }
                }
            }
        },
        containerColor = SarahBackground
    ) { innerPadding ->
        when (currentScreen) {
            Screen.Onboarding -> {
                var onboardingState by remember { mutableStateOf(OnboardingUiState()) }
                OnboardingScreenContent(
                    uiState = onboardingState,
                    onWakeTimeChanged = { onboardingState = onboardingState.copy(wakeTime = it) },
                    onSleepTimeChanged = { onboardingState = onboardingState.copy(sleepTime = it) },
                    onCollegeStartChanged = { onboardingState = onboardingState.copy(collegeStartTime = it) },
                    onCollegeEndChanged = { onboardingState = onboardingState.copy(collegeEndTime = it) },
                    onCommuteChanged = { onboardingState = onboardingState.copy(commuteMinutes = it) },
                    onComplete = {
                        coroutineScope.launch {
                            val schedule = CollegeSchedule(
                                wakeTimeMinutes = onboardingState.wakeTime.hour * 60 + onboardingState.wakeTime.minute,
                                sleepTimeMinutes = onboardingState.sleepTime.hour * 60 + onboardingState.sleepTime.minute,
                                collegeStartTimeMinutes = onboardingState.collegeStartTime.hour * 60 + onboardingState.collegeStartTime.minute,
                                collegeEndTimeMinutes = onboardingState.collegeEndTime.hour * 60 + onboardingState.collegeEndTime.minute,
                                commuteMinutes = onboardingState.commuteMinutes
                            )
                            container.scheduleRepository.saveSchedule(schedule)
                            container.preferences.isOnboardingCompleted = true
                            currentScreen = Screen.Today
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
            }

            Screen.Today -> {
                val tasks by container.taskRepository.getAllTasks().collectAsState(initial = emptyList())
                val subjects by container.subjectRepository.getAllSubjects().collectAsState(initial = emptyList())
                val schedule by container.scheduleRepository.getSchedule().collectAsState(initial = null)
                val userProfile by container.userRepository.getUserProfile().collectAsState(initial = null)
                val energyLevel by container.preferences.energyLevelFlow.collectAsState(initial = EnergyLevel.NORMAL)
                val upcomingReminders by container.reminderRepository.getActiveUpcomingReminders().collectAsState(initial = emptyList())

                val activeTasks = tasks.filter { it.status != TaskStatus.COMPLETED }
                val currentSchedule = schedule ?: CollegeSchedule()
                val nextAction = remember(activeTasks, schedule) {
                    container.nextActionEngine.computeNextAction(
                        plan = com.sarah.app.domain.model.DailyPlan(dateEpochDay = currentTimeEpochMs() / (86400 * 1000L)),
                        tasks = activeTasks,
                        schedule = currentSchedule
                    )
                }

                val feasibilityReport = remember(activeTasks, schedule, energyLevel) {
                    container.feasibilityEngine.evaluateToday(
                        tasks = activeTasks,
                        schedule = currentSchedule,
                        energyLevel = energyLevel
                    )
                }

                val dailyPlan = remember(activeTasks, schedule, energyLevel) {
                    container.adaptivePlanner.generatePlan(
                        tasks = activeTasks,
                        schedule = currentSchedule,
                        energyLevel = energyLevel
                    )
                }

                val todayUiState = TodayUiState(
                    isLoading = false,
                    greeting = "Good " + getDayGreeting() + ", " + (userProfile?.name?.ifBlank { "Student" } ?: "Student"),
                    todayFormatted = "Today",
                    userProfile = userProfile,
                    schedule = currentSchedule,
                    currentEnergyLevel = energyLevel,
                    tasks = activeTasks,
                    nextAction = nextAction,
                    feasibilityReport = feasibilityReport,
                    dailyPlan = dailyPlan,
                    upcomingReminders = upcomingReminders
                )

                var isQuickCaptureOpen by remember { mutableStateOf(false) }

                TodayScreenContent(
                    uiState = todayUiState,
                    onPrimaryNextActionClick = { action ->
                        action.taskId?.let { id ->
                            coroutineScope.launch {
                                container.taskRepository.updateTaskStatus(id, TaskStatus.IN_PROGRESS)
                            }
                        }
                    },
                    onMarkNextActionCompletedClick = { taskId ->
                        coroutineScope.launch {
                            container.taskRepository.updateTaskStatus(taskId, TaskStatus.COMPLETED)
                            container.reminderScheduler.cancelTaskReminders(taskId)
                        }
                    },
                    onToggleTask = { task ->
                        coroutineScope.launch {
                            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
                            container.taskRepository.updateTaskStatus(task.id, newStatus)
                            if (newStatus == TaskStatus.COMPLETED) {
                                container.reminderScheduler.cancelTaskReminders(task.id)
                            }
                        }
                    },
                    onAddReminder = { title, message, timeEpochMs, subjectId ->
                        coroutineScope.launch {
                            val rem = Reminder(
                                title = title,
                                message = message,
                                reminderTimeEpochMs = timeEpochMs,
                                subjectId = subjectId
                            )
                            val newId = container.reminderRepository.insertReminder(rem)
                            container.reminderScheduler.scheduleReminder(rem.copy(id = newId))
                        }
                    },
                    onSnoozeReminderMinutes = { id, mins ->
                        coroutineScope.launch {
                            container.reminderRepository.snoozeReminder(id, mins)
                            container.reminderRepository.getReminderById(id)?.let {
                                container.reminderScheduler.scheduleReminder(it)
                            }
                        }
                    },
                    onSnoozeReminderUntil = { id, epochMs ->
                        coroutineScope.launch {
                            container.reminderRepository.snoozeReminderUntil(id, epochMs)
                            container.reminderRepository.getReminderById(id)?.let {
                                container.reminderScheduler.scheduleReminder(it)
                            }
                        }
                    },
                    onDismissReminder = { rem ->
                        coroutineScope.launch {
                            container.reminderRepository.dismissReminder(rem.id)
                            container.reminderScheduler.cancelReminder(rem.id)
                        }
                    },
                    onDeleteReminder = { rem ->
                        coroutineScope.launch {
                            container.reminderRepository.deleteReminder(rem)
                            container.reminderScheduler.cancelReminder(rem.id)
                        }
                    },
                    onOpenQuickCapture = { isQuickCaptureOpen = true },
                    onNavigateToNotes = { currentScreen = Screen.Notes },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )

                if (isQuickCaptureOpen) {
                    IosQuickCaptureSheet(
                        container = container,
                        onDismiss = { isQuickCaptureOpen = false }
                    )
                }
            }

            Screen.Tasks -> {
                val tasks by container.taskRepository.getAllTasks().collectAsState(initial = emptyList())
                val subjects by container.subjectRepository.getAllSubjects().collectAsState(initial = emptyList())
                var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
                var editingTask by remember { mutableStateOf<Task?>(null) }
                var isAddDialogOpen by remember { mutableStateOf(false) }

                val tasksUiState = TasksUiState(
                    isLoading = false,
                    tasks = tasks,
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId
                )

                TasksScreenContent(
                    uiState = tasksUiState,
                    selectedSubjectId = selectedSubjectId,
                    onSubjectFilterSelected = { selectedSubjectId = it },
                    onToggleTask = { task ->
                        coroutineScope.launch {
                            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
                            container.taskRepository.updateTaskStatus(task.id, newStatus)
                            if (newStatus == TaskStatus.COMPLETED) {
                                container.reminderScheduler.cancelTaskReminders(task.id)
                            }
                        }
                    },
                    onAddTask = { isAddDialogOpen = true },
                    onEditTask = { editingTask = it },
                    onDeleteTask = { task ->
                        coroutineScope.launch {
                            container.taskRepository.deleteTask(task)
                            container.reminderScheduler.cancelTaskReminders(task.id)
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )

                if (isAddDialogOpen || editingTask != null) {
                    AddEditTaskDialog(
                        task = editingTask,
                        subjects = subjects,
                        onDismiss = {
                            isAddDialogOpen = false
                            editingTask = null
                        },
                        onSave = { title, subjectId, type, description, deadlineEpochMs, estimatedMinutes, priority, difficulty, energyRequirement ->
                            coroutineScope.launch {
                                val subj = subjects.find { it.id == subjectId }
                                val toSave = Task(
                                    id = editingTask?.id ?: 0L,
                                    title = title,
                                    subjectId = subjectId,
                                    subjectName = subj?.name ?: "",
                                    type = type,
                                    description = description,
                                    deadlineEpochMs = deadlineEpochMs,
                                    estimatedMinutes = estimatedMinutes,
                                    priority = priority,
                                    difficulty = difficulty,
                                    energyRequirement = energyRequirement,
                                    status = editingTask?.status ?: TaskStatus.PENDING
                                )
                                val savedId = container.taskRepository.insertTask(toSave)
                                if (container.preferences.isDeadlineRemindersEnabled) {
                                    val reminders = container.deadlineReminderEngine.generateDeadlineReminders(toSave.copy(id = savedId))
                                    reminders.forEach { rem ->
                                        val remId = container.reminderRepository.insertReminder(rem)
                                        container.reminderScheduler.scheduleReminder(rem.copy(id = remId))
                                    }
                                }
                                isAddDialogOpen = false
                                editingTask = null
                            }
                        },
                        onDelete = { task ->
                            coroutineScope.launch {
                                container.taskRepository.deleteTask(task)
                                container.reminderScheduler.cancelTaskReminders(task.id)
                                editingTask = null
                            }
                        }
                    )
                }
            }

            Screen.Subjects -> {
                val subjects by container.subjectRepository.getAllSubjects().collectAsState(initial = emptyList())
                val tasks by container.taskRepository.getAllTasks().collectAsState(initial = emptyList())
                var filterMode by remember { mutableStateOf(SubjectFilterMode.ALL) }
                var editingSubject by remember { mutableStateOf<Subject?>(null) }
                var isAddDialogOpen by remember { mutableStateOf(false) }

                val subjectsUiState = SubjectsUiState(
                    isLoading = false,
                    subjects = subjects,
                    tasks = tasks,
                    filterMode = filterMode
                )

                SubjectsScreenContent(
                    uiState = subjectsUiState,
                    filterMode = filterMode,
                    onFilterModeSelected = { filterMode = it },
                    onAddSubject = { isAddDialogOpen = true },
                    onEditSubject = { editingSubject = it },
                    onDeleteSubject = { subject ->
                        coroutineScope.launch {
                            container.subjectRepository.deleteSubject(subject)
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )

                if (isAddDialogOpen || editingSubject != null) {
                    AddEditSubjectDialog(
                        subject = editingSubject,
                        onDismiss = {
                            isAddDialogOpen = false
                            editingSubject = null
                        },
                        onSave = { name, code, prof, colorHex, weeklyHours, targetAtt, currAtt ->
                            coroutineScope.launch {
                                val subj = Subject(
                                    id = editingSubject?.id ?: 0L,
                                    name = name,
                                    code = code,
                                    professorName = prof,
                                    colorHex = colorHex,
                                    weeklyHours = weeklyHours,
                                    targetAttendancePercentage = targetAtt,
                                    currentAttendancePercentage = currAtt,
                                    isActive = true
                                )
                                container.subjectRepository.insertSubject(subj)
                                isAddDialogOpen = false
                                editingSubject = null
                            }
                        },
                        onDelete = { subj ->
                            coroutineScope.launch {
                                container.subjectRepository.deleteSubject(subj)
                                editingSubject = null
                            }
                        }
                    )
                }
            }

            Screen.Schedule -> {
                val schedule by container.scheduleRepository.getSchedule().collectAsState(initial = null)
                val scheduleUiState = ScheduleUiState(
                    isLoading = false,
                    schedule = schedule ?: CollegeSchedule()
                )

                ScheduleScreenContent(
                    uiState = scheduleUiState,
                    onSaveSchedule = { updatedSchedule ->
                        coroutineScope.launch {
                            container.scheduleRepository.saveSchedule(updatedSchedule)
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
            }

            Screen.Profile -> {
                val userProfile by container.userRepository.getUserProfile().collectAsState(initial = null)
                val deadlineRemindersEnabled by container.preferences.deadlineRemindersEnabledFlow.collectAsState(initial = true)
                val customRemindersEnabled by container.preferences.customRemindersEnabledFlow.collectAsState(initial = true)

                val profileUiState = ProfileUiState(
                    isLoading = false,
                    profile = userProfile ?: UserProfile(),
                    isDeadlineRemindersEnabled = deadlineRemindersEnabled,
                    isCustomRemindersEnabled = customRemindersEnabled
                )

                ProfileScreenContent(
                    uiState = profileUiState,
                    onSaveProfile = { updatedProfile ->
                        coroutineScope.launch {
                            container.userRepository.saveUserProfile(updatedProfile)
                        }
                    },
                    onToggleDeadlineReminders = { enabled ->
                        container.preferences.isDeadlineRemindersEnabled = enabled
                    },
                    onToggleCustomReminders = { enabled ->
                        container.preferences.isCustomRemindersEnabled = enabled
                    },
                    onNavigateToNotes = { currentScreen = Screen.Notes },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
            }

            Screen.Notes -> {
                val notes by container.academicNoteRepository.getAllNotes().collectAsState(initial = emptyList())
                val subjects by container.subjectRepository.getAllSubjects().collectAsState(initial = emptyList())
                var searchQuery by remember { mutableStateOf("") }
                var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
                var isAddNoteOpen by remember { mutableStateOf(false) }

                val notesUiState = NotesUiState(
                    isLoading = false,
                    notes = notes,
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    searchQuery = searchQuery
                )

                NotesScreenContent(
                    uiState = notesUiState,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedSubjectId = selectedSubjectId,
                    onSubjectFilterSelected = { selectedSubjectId = it },
                    onAddNote = { isAddNoteOpen = true },
                    onEditNote = { /* Edit note */ },
                    onDeleteNote = { note ->
                        coroutineScope.launch {
                            container.academicNoteRepository.deleteNote(note)
                        }
                    },
                    onTogglePin = { note ->
                        coroutineScope.launch {
                            container.academicNoteRepository.togglePin(note.id, !note.isPinned)
                        }
                    },
                    onConvertToTask = { note ->
                        coroutineScope.launch {
                            val task = Task(
                                title = note.title,
                                subjectId = note.subjectId,
                                subjectName = subjects.find { it.id == note.subjectId }?.name ?: "",
                                type = TaskType.ASSIGNMENT,
                                description = note.content,
                                priority = TaskPriority.MEDIUM,
                                deadlineEpochMs = currentTimeEpochMs() + (24 * 3600 * 1000L),
                                estimatedMinutes = 45
                            )
                            container.taskRepository.insertTask(task)
                        }
                    },
                    onConvertNoteToTask = { note, taskDraft ->
                        coroutineScope.launch {
                            container.taskRepository.insertTask(taskDraft.toTask())
                        }
                    },
                    onConvertToReminder = { note ->
                        coroutineScope.launch {
                            val rem = Reminder(
                                title = "Note Reminder: " + note.title,
                                message = note.content.take(100),
                                reminderTimeEpochMs = currentTimeEpochMs() + (3600 * 1000L),
                                subjectId = note.subjectId
                            )
                            val newId = container.reminderRepository.insertReminder(rem)
                            container.reminderScheduler.scheduleReminder(rem.copy(id = newId))
                        }
                    },
                    onNavigateBack = { currentScreen = Screen.Today },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )

                if (isAddNoteOpen) {
                    AddAcademicNoteSheet(
                        subjects = subjects,
                        onDismiss = { isAddNoteOpen = false },
                        onSave = { title, content, subjectId, isPinned ->
                            coroutineScope.launch {
                                val newNote = com.sarah.app.domain.model.AcademicNote(
                                    title = title,
                                    content = content,
                                    subjectId = subjectId,
                                    subjectName = subjects.find { it.id == subjectId }?.name,
                                    isPinned = isPinned,
                                    createdAtEpochMs = currentTimeEpochMs()
                                )
                                container.academicNoteRepository.insertNote(newNote)
                                isAddNoteOpen = false
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun getDayGreeting(): String {
    val currentHour = (currentTimeEpochMs() / (3600 * 1000L) % 24).toInt()
    return when (currentHour) {
        in 5..11 -> "Morning"
        in 12..16 -> "Afternoon"
        in 17..21 -> "Evening"
        else -> "Night"
    }
}
