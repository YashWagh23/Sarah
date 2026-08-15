package com.sarah.app.ui.screens.today

import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.engine.AdaptivePlanner
import com.sarah.app.domain.engine.FeasibilityEngine
import com.sarah.app.domain.engine.NextActionEngine
import com.sarah.app.domain.engine.TaskPriorityScorer
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.model.TemporaryInterruption
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.DailyPlanRepository
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {

    private val taskFlow = MutableStateFlow<List<Task>>(emptyList())
    private val subjectFlow = MutableStateFlow<List<Subject>>(emptyList())
    private val scheduleFlow = MutableStateFlow<CollegeSchedule?>(CollegeSchedule())
    private val profileFlow = MutableStateFlow<UserProfile?>(UserProfile(name = "Alex"))
    private val energyFlow = MutableStateFlow(EnergyLevel.NORMAL)
    private val interruptionFlow = MutableStateFlow<List<TemporaryInterruption>>(emptyList())

    private val fakeTaskRepository = object : TaskRepository {
        override fun getAllTasks(): Flow<List<Task>> = taskFlow
        override fun getActiveTasks(): Flow<List<Task>> = taskFlow
        override fun getTasksBySubject(subjectId: Long): Flow<List<Task>> = taskFlow
        override suspend fun getTaskById(id: Long): Task? = taskFlow.value.firstOrNull { it.id == id }
        override suspend fun insertTask(task: Task): Long {
            val nextId = (taskFlow.value.maxOfOrNull { it.id } ?: 0L) + 1L
            val created = task.copy(id = nextId)
            taskFlow.value = taskFlow.value + created
            return nextId
        }
        override suspend fun updateTask(task: Task) {
            taskFlow.value = taskFlow.value.map { if (it.id == task.id) task else it }
        }
        override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
            taskFlow.value = taskFlow.value.map {
                if (it.id == id) it.copy(status = status, completedAtEpochMs = if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null)
                else it
            }
        }
        override suspend fun deleteTask(task: Task) {
            taskFlow.value = taskFlow.value.filterNot { it.id == task.id }
        }
        override suspend fun deleteTaskById(id: Long) {
            taskFlow.value = taskFlow.value.filterNot { it.id == id }
        }
    }

    private val fakeSubjectRepository = object : SubjectRepository {
        override fun getAllSubjects(): Flow<List<Subject>> = subjectFlow
        override fun getActiveSubjects(): Flow<List<Subject>> = subjectFlow
        override suspend fun getSubjectById(id: Long): Subject? = subjectFlow.value.firstOrNull { it.id == id }
        override suspend fun insertSubject(subject: Subject): Long = 1
        override suspend fun updateSubject(subject: Subject) {}
        override suspend fun deleteSubject(subject: Subject) {}
    }

    private val fakeScheduleRepository = object : ScheduleRepository {
        override fun getSchedule(): Flow<CollegeSchedule?> = scheduleFlow
        override suspend fun saveSchedule(schedule: CollegeSchedule) { scheduleFlow.value = schedule }
    }

    private val fakeUserRepository = object : UserRepository {
        override fun getUserProfile(): Flow<UserProfile?> = profileFlow
        override suspend fun saveUserProfile(profile: UserProfile) { profileFlow.value = profile }
        override suspend fun setOnboardingCompleted(completed: Boolean) {}
    }

    private val fakeDailyPlanRepository = object : DailyPlanRepository {
        override fun getDailyPlan(dateEpochDay: Long): Flow<DailyPlan?> = flowOf(null)
        override suspend fun saveDailyPlan(plan: DailyPlan): Long = 1
        override suspend fun updatePlanItemStatus(itemId: Long, status: PlanItemStatus) {}
        override fun getInterruptions(dateEpochDay: Long): Flow<List<TemporaryInterruption>> = interruptionFlow
        override suspend fun addInterruption(interruption: TemporaryInterruption): Long = 1
        override suspend fun deleteInterruption(id: Long) {}
        override suspend fun clearInterruptions(dateEpochDay: Long) {}
    }

    private class FakeSarahPreferences(private val energyFlow: MutableStateFlow<EnergyLevel>) : com.sarah.app.data.preferences.SarahPreferences {
        override var currentEnergyLevel: EnergyLevel
            get() = energyFlow.value
            set(value) { energyFlow.value = value }
        override val energyLevelFlow: Flow<EnergyLevel> = energyFlow
        override var isOnboardingCompleted: Boolean = true
        override val onboardingCompletedFlow: Flow<Boolean> = flowOf(true)
    }

    private lateinit var preferencesManager: com.sarah.app.data.preferences.SarahPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        preferencesManager = FakeSarahPreferences(energyFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state loads next action when task is present`() = runBlocking {
        val date = LocalDate.now()
        val tomorrowMs = date.plusDays(1).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val task = Task(
            id = 1,
            title = "DBMS Assignment",
            deadlineEpochMs = tomorrowMs,
            estimatedMinutes = 45,
            priority = TaskPriority.HIGH,
            type = TaskType.ASSIGNMENT
        )
        taskFlow.value = listOf(task)

        val viewModel = TodayViewModel(
            taskRepository = fakeTaskRepository,
            subjectRepository = fakeSubjectRepository,
            scheduleRepository = fakeScheduleRepository,
            userRepository = fakeUserRepository,
            preferencesManager = preferencesManager,
            feasibilityEngine = FeasibilityEngine(),
            dailyPlanRepository = fakeDailyPlanRepository,
            adaptivePlanner = AdaptivePlanner(TaskPriorityScorer()),
            nextActionEngine = NextActionEngine()
        )

        val state = viewModel.uiState.value
        assertNotNull(state.nextAction)
        assertEquals(NextActionType.START_TASK, state.nextAction?.actionType)
        assertEquals(1L, state.nextAction?.taskId)
        assertEquals("Start: DBMS Assignment", state.nextAction?.title)
    }

    @Test
    fun `test completing a task reactively updates next action to subsequent task or completion`() = runBlocking {
        val date = LocalDate.now()
        val tomorrowMs = date.plusDays(1).atTime(9, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val task1 = Task(id = 1, title = "Task 1", deadlineEpochMs = tomorrowMs, estimatedMinutes = 30, priority = TaskPriority.HIGH)
        val task2 = Task(id = 2, title = "Task 2", deadlineEpochMs = tomorrowMs, estimatedMinutes = 30, priority = TaskPriority.MEDIUM)
        taskFlow.value = listOf(task1, task2)

        val viewModel = TodayViewModel(
            taskRepository = fakeTaskRepository,
            subjectRepository = fakeSubjectRepository,
            scheduleRepository = fakeScheduleRepository,
            userRepository = fakeUserRepository,
            preferencesManager = preferencesManager,
            feasibilityEngine = FeasibilityEngine(),
            dailyPlanRepository = fakeDailyPlanRepository,
            adaptivePlanner = AdaptivePlanner(TaskPriorityScorer()),
            nextActionEngine = NextActionEngine()
        )

        assertEquals("Start: Task 1", viewModel.uiState.value.nextAction?.title)

        // Complete Task 1
        viewModel.completeTaskById(1L)

        // Next action should immediately shift to Task 2
        assertEquals(2L, viewModel.uiState.value.nextAction?.taskId)
        assertEquals("Start: Task 2", viewModel.uiState.value.nextAction?.title)

        // Complete Task 2
        viewModel.completeTaskById(2L)

        // Next action should transition to STOP_FOR_TONIGHT (All Done)
        assertEquals(NextActionType.STOP_FOR_TONIGHT, viewModel.uiState.value.nextAction?.actionType)
        assertEquals("All Done for Tonight!", viewModel.uiState.value.nextAction?.title)
    }

    @Test
    fun `test empty tasks recommends all caught up`() = runBlocking {
        taskFlow.value = emptyList()

        val viewModel = TodayViewModel(
            taskRepository = fakeTaskRepository,
            subjectRepository = fakeSubjectRepository,
            scheduleRepository = fakeScheduleRepository,
            userRepository = fakeUserRepository,
            preferencesManager = preferencesManager,
            feasibilityEngine = FeasibilityEngine(),
            dailyPlanRepository = fakeDailyPlanRepository,
            adaptivePlanner = AdaptivePlanner(TaskPriorityScorer()),
            nextActionEngine = NextActionEngine()
        )

        val state = viewModel.uiState.value
        assertNotNull(state.nextAction)
        assertEquals(NextActionType.STOP_FOR_TONIGHT, state.nextAction?.actionType)
        assertEquals("All Done for Tonight!", state.nextAction?.title)
    }
}
