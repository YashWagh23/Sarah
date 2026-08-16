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
import com.sarah.app.domain.model.AcademicNote
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
import com.sarah.app.ui.navigation.Screen
import com.sarah.app.ui.screens.notes.NotesScreenContent
import com.sarah.app.ui.screens.notes.NotesUiState
import com.sarah.app.ui.screens.onboarding.OnboardingScreenContent
import com.sarah.app.ui.screens.onboarding.OnboardingUiState
import com.sarah.app.ui.screens.profile.ProfileScreenContent
import com.sarah.app.ui.screens.profile.ProfileUiState
import com.sarah.app.ui.screens.schedule.ScheduleScreenContent
import com.sarah.app.ui.screens.schedule.ScheduleUiState
import com.sarah.app.ui.screens.subjects.SubjectWithTaskCount
import com.sarah.app.ui.screens.subjects.SubjectsScreenContent
import com.sarah.app.ui.screens.subjects.SubjectsUiState
import com.sarah.app.ui.screens.tasks.TaskFilter
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
                    onNameChange = { onboardingState = onboardingState.copy(name = it) },
                    onCollegeChange = { onboardingState = onboardingState.copy(collegeName = it) },
                    onDepartmentChange = { onboardingState = onboardingState.copy(department = it) },
                    onSleepChange = { onboardingState = onboardingState.copy(sleepTimeMinutes = it) },
                    onCollegeHoursChange = { start, end ->
                        onboardingState = onboardingState.copy(
                            collegeStartTimeMinutes = start,
                            collegeEndTimeMinutes = end
                        )
                    },
                    onNextStep = {
                        if (onboardingState.currentStep < 2) {
                            onboardingState = onboardingState.copy(currentStep = onboardingState.currentStep + 1)
                        }
                    },
                    onPrevStep = {
                        if (onboardingState.currentStep > 0) {
                            onboardingState = onboardingState.copy(currentStep = onboardingState.currentStep - 1)
                        }
                    },
                    onCompleteOnboarding = {
                        coroutineScope.launch {
                            val userProfile = UserProfile(
                                name = onboardingState.name,
                                collegeName = onboardingState.collegeName,
                                department = onboardingState.department,
                                semesterYear = onboardingState.semesterYear
                            )
                            container.userRepository.saveUserProfile(userProfile)
                            val schedule = CollegeSchedule(
                                sleepTimeMinutes = onboardingState.sleepTimeMinutes,
                                collegeStartTimeMinutes = onboardingState.collegeStartTimeMinutes,
                                collegeEndTimeMinutes = onboardingState.collegeEndTimeMinutes
                            )
                            container.scheduleRepository.saveSchedule(schedule)
                            container.preferences.isOnboardingCompleted = true
                            currentScreen = Screen.Today
                        }
                    },
                    onCompleted = {
                        currentScreen = Screen.Today
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
                    userProfile = userProfile,
                    schedule = currentSchedule,
                    energyLevel = energyLevel,
                    tasks = activeTasks,
                    subjects = subjects,
                    upcomingReminders = upcomingReminders,
                    feasibilityReport = feasibilityReport,
                    dailyPlan = dailyPlan,
                    nextAction = nextAction
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
                var selectedFilter by remember { mutableStateOf(TaskFilter.ACTIVE) }
                var selectedSubjectId by remember { mutableStateOf<Long?>(null) }
                var editingTask by remember { mutableStateOf<Task?>(null) }
                var isAddEditDialogOpen by remember { mutableStateOf(false) }

                val filtered = remember(tasks, selectedFilter, selectedSubjectId) {
                    tasks.filter { task ->
                        val matchesFilter = when (selectedFilter) {
                            TaskFilter.ALL -> true
                            TaskFilter.ACTIVE -> task.status != TaskStatus.COMPLETED
                            TaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
                        }
                        val matchesSubject = selectedSubjectId == null || task.subjectId == selectedSubjectId
                        matchesFilter && matchesSubject
                    }
                }

                val tasksUiState = TasksUiState(
                    isLoading = false,
                    tasks = tasks,
                    filteredTasks = filtered,
                    subjects = subjects,
                    selectedFilter = selectedFilter,
                    selectedSubjectId = selectedSubjectId,
                    isAddEditDialogOpen = isAddEditDialogOpen,
                    editingTask = editingTask
                )

                TasksScreenContent(
                    uiState = tasksUiState,
                    onOpenAddTask = {
                        editingTask = null
                        isAddEditDialogOpen = true
                    },
                    onFilterChange = { selectedFilter = it },
                    onSubjectFilterChange = { selectedSubjectId = it },
                    onToggleTaskStatus = { task ->
                        coroutineScope.launch {
                            val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
                            container.taskRepository.updateTaskStatus(task.id, newStatus)
                            if (newStatus == TaskStatus.COMPLETED) {
                                container.reminderScheduler.cancelTaskReminders(task.id)
                            }
                        }
                    },
                    onEditTask = { task ->
                        editingTask = task
                        isAddEditDialogOpen = true
                    },
                    onCloseAddEditDialog = {
                        isAddEditDialogOpen = false
                        editingTask = null
                    },
                    onSaveTask = { title, subjectId, type, description, deadlineEpochMs, estimatedMinutes, priority, difficulty, energyRequirement ->
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
                            isAddEditDialogOpen = false
                            editingTask = null
                        }
                    },
                    onDeleteTask = { task ->
                        coroutineScope.launch {
                            container.taskRepository.deleteTask(task)
                            container.reminderScheduler.cancelTaskReminders(task.id)
                            isAddEditDialogOpen = false
                            editingTask = null
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
            }

            Screen.Subjects -> {
                val subjects by container.subjectRepository.getAllSubjects().collectAsState(initial = emptyList())
                val tasks by container.taskRepository.getAllTasks().collectAsState(initial = emptyList())
                var editingSubject by remember { mutableStateOf<Subject?>(null) }
                var isAddEditDialogOpen by remember { mutableStateOf(false) }

                val subjectsWithCount = remember(subjects, tasks) {
                    subjects.map { subj ->
                        SubjectWithTaskCount(
                            subject = subj,
                            pendingTasksCount = tasks.count { it.subjectId == subj.id && it.status != TaskStatus.COMPLETED }
                        )
                    }
                }

                val subjectsUiState = SubjectsUiState(
                    isLoading = false,
                    subjectsWithCount = subjectsWithCount,
                    isAddEditDialogOpen = isAddEditDialogOpen,
                    editingSubject = editingSubject
                )

                SubjectsScreenContent(
                    uiState = subjectsUiState,
                    onOpenAddSubjectDialog = {
                        editingSubject = null
                        isAddEditDialogOpen = true
                    },
                    onOpenEditSubjectDialog = { subj ->
                        editingSubject = subj
                        isAddEditDialogOpen = true
                    },
                    onCloseAddEditDialog = {
                        isAddEditDialogOpen = false
                        editingSubject = null
                    },
                    onSaveSubject = { name, code, prof, colorHex, weeklyHours, targetAtt, currAtt ->
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
                            isAddEditDialogOpen = false
                            editingSubject = null
                        }
                    },
                    onDeleteSubject = { subject ->
                        coroutineScope.launch {
                            container.subjectRepository.deleteSubject(subject)
                            isAddEditDialogOpen = false
                            editingSubject = null
                        }
                    },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
            }

            Screen.Schedule -> {
                val schedule by container.scheduleRepository.getSchedule().collectAsState(initial = null)
                val scheduleUiState = ScheduleUiState(
                    isLoading = false,
                    schedule = schedule ?: CollegeSchedule()
                )

                ScheduleScreenContent(
                    uiState = scheduleUiState,
                    onUpdateSchedule = { wake, sleep, start, end, commute, dinner, breakDur, sessionLen ->
                        coroutineScope.launch {
                            val updated = CollegeSchedule(
                                wakeTimeMinutes = wake,
                                sleepTimeMinutes = sleep,
                                collegeStartTimeMinutes = start,
                                collegeEndTimeMinutes = end,
                                commuteMinutes = commute,
                                dinnerBufferMinutes = dinner,
                                breakDurationMinutes = breakDur,
                                preferredSessionLengthMinutes = sessionLen
                            )
                            container.scheduleRepository.saveSchedule(updated)
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
                    userProfile = userProfile ?: UserProfile(),
                    isDeadlineRemindersEnabled = deadlineRemindersEnabled,
                    isCustomRemindersEnabled = customRemindersEnabled
                )

                ProfileScreenContent(
                    uiState = profileUiState,
                    onDeadlineRemindersToggled = { enabled ->
                        container.preferences.isDeadlineRemindersEnabled = enabled
                    },
                    onCustomRemindersToggled = { enabled ->
                        container.preferences.isCustomRemindersEnabled = enabled
                    },
                    onSaveProfile = { name, college, dept, sem, defaultEnergy ->
                        coroutineScope.launch {
                            val updated = UserProfile(
                                name = name,
                                collegeName = college,
                                department = dept,
                                semesterYear = sem,
                                defaultEnergyLevel = defaultEnergy
                            )
                            container.userRepository.saveUserProfile(updated)
                        }
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
                var isAddSheetOpen by remember { mutableStateOf(false) }
                var editingNote by remember { mutableStateOf<AcademicNote?>(null) }
                var reminderConversionNote by remember { mutableStateOf<AcademicNote?>(null) }
                var userMessage by remember { mutableStateOf<String?>(null) }

                val filtered = remember(notes, searchQuery, selectedSubjectId) {
                    notes.filter { note ->
                        val matchesSearch = searchQuery.isBlank() ||
                            note.title.contains(searchQuery, ignoreCase = true) ||
                            note.content.contains(searchQuery, ignoreCase = true)
                        val matchesSubject = selectedSubjectId == null || note.subjectId == selectedSubjectId
                        matchesSearch && matchesSubject
                    }
                }

                val notesUiState = NotesUiState(
                    notes = notes,
                    filteredNotes = filtered,
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    searchQuery = searchQuery,
                    isLoading = false,
                    isAddSheetOpen = isAddSheetOpen,
                    editingNote = editingNote,
                    reminderConversionNote = reminderConversionNote,
                    userMessage = userMessage
                )

                NotesScreenContent(
                    uiState = notesUiState,
                    onSearchQueryChange = { searchQuery = it },
                    onSelectSubject = { selectedSubjectId = it },
                    onOpenAddSheet = { noteToEdit ->
                        editingNote = noteToEdit
                        isAddSheetOpen = true
                    },
                    onCloseAddSheet = {
                        isAddSheetOpen = false
                        editingNote = null
                    },
                    onSaveNote = { title, content, subjectId, isPinned ->
                        coroutineScope.launch {
                            val note = AcademicNote(
                                id = editingNote?.id ?: 0L,
                                title = title,
                                content = content,
                                subjectId = subjectId,
                                subjectName = subjects.find { it.id == subjectId }?.name,
                                isPinned = isPinned,
                                createdAtEpochMs = editingNote?.createdAtEpochMs ?: currentTimeEpochMs()
                            )
                            container.academicNoteRepository.insertNote(note)
                            isAddSheetOpen = false
                            editingNote = null
                        }
                    },
                    onTogglePin = { note, isPinned ->
                        coroutineScope.launch {
                            container.academicNoteRepository.togglePin(note.id, isPinned)
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
                            userMessage = "Task created from note!"
                        }
                    },
                    onOpenReminderConversion = { note ->
                        reminderConversionNote = note
                    },
                    onCloseReminderConversion = {
                        reminderConversionNote = null
                    },
                    onSaveReminderFromNote = { note, title, message, timeEpochMs ->
                        coroutineScope.launch {
                            val rem = Reminder(
                                title = title,
                                message = message,
                                reminderTimeEpochMs = timeEpochMs,
                                subjectId = note.subjectId
                            )
                            val newId = container.reminderRepository.insertReminder(rem)
                            container.reminderScheduler.scheduleReminder(rem.copy(id = newId))
                            reminderConversionNote = null
                            userMessage = "Reminder set for note!"
                        }
                    },
                    onDeleteNote = { note ->
                        coroutineScope.launch {
                            container.academicNoteRepository.deleteNote(note)
                        }
                    },
                    onClearUserMessage = { userMessage = null },
                    onNavigateBack = { currentScreen = Screen.Today },
                    modifier = modifier.fillMaxSize().padding(innerPadding)
                )
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
