package com.sarah.app.ui.screens.notes

import android.content.Context
import android.content.SharedPreferences
import com.sarah.app.data.preferences.SarahPreferencesManager
import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.repository.AcademicNoteRepository
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneId

import com.sarah.app.data.preferences.SarahPreferences

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    private lateinit var fakeNoteRepository: FakeAcademicNoteRepo
    private lateinit var fakeSubjectRepository: FakeSubjectRepo
    private lateinit var fakeTaskRepository: FakeTaskRepo
    private lateinit var fakeReminderRepository: FakeReminderRepo
    private lateinit var fakeReminderScheduler: FakeReminderSched
    private lateinit var deadlineReminderEngine: DeadlineReminderEngine
    private lateinit var preferencesManager: SarahPreferences

    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Unconfined)
        fakeNoteRepository = FakeAcademicNoteRepo()
        fakeSubjectRepository = FakeSubjectRepo()
        fakeTaskRepository = FakeTaskRepo()
        fakeReminderRepository = FakeReminderRepo()
        fakeReminderScheduler = FakeReminderSched()
        deadlineReminderEngine = DeadlineReminderEngine(ZoneId.of("UTC"))
        preferencesManager = FakeSarahPreferences()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial state loads notes and subjects`() = runBlocking {
        val s1 = Subject(id = 1, name = "Java & OOP", code = "CS301")
        val s2 = Subject(id = 2, name = "Database Systems", code = "CS302")
        fakeSubjectRepository.subjects.value = listOf(s1, s2)

        fakeNoteRepository.insertNote(AcademicNote(title = "Java Notes", content = "Interfaces", subjectId = 1, subjectName = "Java & OOP"))
        fakeNoteRepository.insertNote(AcademicNote(title = "DBMS Tips", content = "SQL joins", subjectId = 2, subjectName = "Database Systems", isPinned = true))

        val viewModel = NotesViewModel(
            academicNoteRepository = fakeNoteRepository,
            subjectRepository = fakeSubjectRepository,
            taskRepository = fakeTaskRepository,
            reminderRepository = fakeReminderRepository,
            reminderScheduler = fakeReminderScheduler,
            deadlineReminderEngine = deadlineReminderEngine,
            preferencesManager = preferencesManager
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.notes.size)
        assertEquals(2, state.subjects.size)
        assertEquals(1, state.pinnedNotes.size)
        assertEquals("DBMS Tips", state.pinnedNotes[0].title)
        assertEquals(1, state.unpinnedNotes.size)
    }

    @Test
    fun `test searching notes filters by keyword`() = runBlocking {
        fakeNoteRepository.insertNote(AcademicNote(title = "Mid-term Syllabus", content = "Unit 1 and 2"))
        fakeNoteRepository.insertNote(AcademicNote(title = "Lab Viva Guidelines", content = "Experiments 1-4"))

        val viewModel = NotesViewModel(
            academicNoteRepository = fakeNoteRepository,
            subjectRepository = fakeSubjectRepository,
            taskRepository = fakeTaskRepository,
            reminderRepository = fakeReminderRepository,
            reminderScheduler = fakeReminderScheduler,
            deadlineReminderEngine = deadlineReminderEngine,
            preferencesManager = preferencesManager
        )

        viewModel.setSearchQuery("viva")
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredNotes.size)
        assertEquals("Lab Viva Guidelines", state.filteredNotes[0].title)
    }

    @Test
    fun `test filtering notes by subject chip`() = runBlocking {
        fakeNoteRepository.insertNote(AcademicNote(title = "Note A", content = "A", subjectId = 1L))
        fakeNoteRepository.insertNote(AcademicNote(title = "Note B", content = "B", subjectId = 2L))

        val viewModel = NotesViewModel(
            academicNoteRepository = fakeNoteRepository,
            subjectRepository = fakeSubjectRepository,
            taskRepository = fakeTaskRepository,
            reminderRepository = fakeReminderRepository,
            reminderScheduler = fakeReminderScheduler,
            deadlineReminderEngine = deadlineReminderEngine,
            preferencesManager = preferencesManager
        )

        viewModel.setSelectedSubject(1L)
        val state = viewModel.uiState.value
        assertEquals(1, state.filteredNotes.size)
        assertEquals("Note A", state.filteredNotes[0].title)
    }

    @Test
    fun `test converting note to task adds task in repository`() = runBlocking {
        val note = AcademicNote(
            id = 1,
            title = "Write Lab Assignment 3",
            content = "Complete quicksort implementation",
            subjectId = 1L,
            subjectName = "Algorithms"
        )
        fakeNoteRepository.insertNote(note)

        val viewModel = NotesViewModel(
            academicNoteRepository = fakeNoteRepository,
            subjectRepository = fakeSubjectRepository,
            taskRepository = fakeTaskRepository,
            reminderRepository = fakeReminderRepository,
            reminderScheduler = fakeReminderScheduler,
            deadlineReminderEngine = deadlineReminderEngine,
            preferencesManager = preferencesManager
        )

        viewModel.convertNoteToTask(note)

        assertEquals(1, fakeTaskRepository.tasks.size)
        val createdTask = fakeTaskRepository.tasks[0]
        assertEquals("Write Lab Assignment 3", createdTask.title)
        assertEquals("Algorithms", createdTask.subjectName)
        assertEquals(1L, createdTask.subjectId)
        assertEquals(TaskStatus.PENDING, createdTask.status)
    }

    @Test
    fun `test converting note to reminder schedules custom reminder`() = runBlocking {
        val note = AcademicNote(
            id = 1,
            title = "Ask Professor about Exam Pattern",
            content = "Clarify MCQs vs Descriptive",
            subjectId = 2L,
            subjectName = "Database Management"
        )

        val viewModel = NotesViewModel(
            academicNoteRepository = fakeNoteRepository,
            subjectRepository = fakeSubjectRepository,
            taskRepository = fakeTaskRepository,
            reminderRepository = fakeReminderRepository,
            reminderScheduler = fakeReminderScheduler,
            deadlineReminderEngine = deadlineReminderEngine,
            preferencesManager = preferencesManager
        )

        val reminderTime = System.currentTimeMillis() + 1800000L
        viewModel.createReminderFromNote(note, reminderTime)

        assertEquals(1, fakeReminderRepository.reminders.size)
        val created = fakeReminderRepository.reminders[0]
        assertEquals("Ask Professor about Exam Pattern", created.title)
        assertEquals(1, fakeReminderScheduler.scheduled.size)
    }

    // Fakes
    private class FakeAcademicNoteRepo : AcademicNoteRepository {
        val notes = mutableListOf<AcademicNote>()
        val flow = MutableStateFlow<List<AcademicNote>>(emptyList())
        private var nextId = 1L

        override fun getAllNotes(): Flow<List<AcademicNote>> = flow.map {
            it.sortedWith(compareByDescending<AcademicNote> { n -> n.isPinned }.thenByDescending { n -> n.updatedEpochMs })
        }
        override fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNote>> = flow.map {
            it.filter { n -> n.subjectId == subjectId }
        }
        override suspend fun getNoteById(id: Long): AcademicNote? = notes.find { it.id == id }
        override suspend fun insertNote(note: AcademicNote): Long {
            val id = if (note.id == 0L) nextId++ else note.id
            val n = note.copy(id = id)
            notes.add(n)
            flow.value = notes.toList()
            return id
        }
        override suspend fun updateNote(note: AcademicNote) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) {
                notes[index] = note
                flow.value = notes.toList()
            }
        }
        override suspend fun deleteNote(note: AcademicNote) = deleteNoteById(note.id)
        override suspend fun deleteNoteById(id: Long) {
            notes.removeAll { it.id == id }
            flow.value = notes.toList()
        }
        override suspend fun togglePin(id: Long, isPinned: Boolean) {
            val n = getNoteById(id)
            if (n != null) updateNote(n.copy(isPinned = isPinned))
        }
    }

    private class FakeSubjectRepo : SubjectRepository {
        val subjects = MutableStateFlow<List<Subject>>(emptyList())
        override fun getAllSubjects(): Flow<List<Subject>> = subjects
        override fun getActiveSubjects(): Flow<List<Subject>> = subjects
        override suspend fun getSubjectById(id: Long): Subject? = subjects.value.find { it.id == id }
        override suspend fun insertSubject(subject: Subject): Long = 1L
        override suspend fun updateSubject(subject: Subject) {}
        override suspend fun deleteSubject(subject: Subject) {}
    }

    private class FakeTaskRepo : TaskRepository {
        val tasks = mutableListOf<Task>()
        private var nextId = 1L
        override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks)
        override fun getActiveTasks(): Flow<List<Task>> = flowOf(tasks.filter { it.status != TaskStatus.COMPLETED })
        override fun getTasksBySubject(subjectId: Long): Flow<List<Task>> = flowOf(tasks.filter { it.subjectId == subjectId })
        override suspend fun getTaskById(id: Long): Task? = tasks.find { it.id == id }
        override suspend fun insertTask(task: Task): Long {
            val id = if (task.id == 0L) nextId++ else task.id
            tasks.add(task.copy(id = id))
            return id
        }
        override suspend fun updateTask(task: Task) {}
        override suspend fun deleteTask(task: Task) {}
        override suspend fun deleteTaskById(id: Long) { tasks.removeAll { it.id == id } }
        override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {}
    }

    private class FakeReminderRepo : ReminderRepository {
        val reminders = mutableListOf<Reminder>()
        private var nextId = 1L
        override fun getAllReminders(): Flow<List<Reminder>> = flowOf(reminders)
        override fun getActiveUpcomingReminders(): Flow<List<Reminder>> = flowOf(reminders)
        override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> = flowOf(reminders)
        override suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<Reminder> = emptyList()
        override suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder> = emptyList()
        override suspend fun getReminderById(id: Long): Reminder? = reminders.find { it.id == id }
        override suspend fun insertReminder(reminder: Reminder): Long {
            val id = if (reminder.id == 0L) nextId++ else reminder.id
            reminders.add(reminder.copy(id = id))
            return id
        }
        override suspend fun updateReminder(reminder: Reminder) {}
        override suspend fun deleteReminder(reminder: Reminder) {}
        override suspend fun deleteReminderById(id: Long) { reminders.removeAll { it.id == id } }
        override suspend fun deleteRemindersByTaskId(taskId: Long) {}
        override suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int) {}
        override suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long) {}
        override suspend fun dismissReminder(id: Long) {}
        override suspend fun setReminderEnabled(id: Long, enabled: Boolean) {}
    }

    private class FakeReminderSched : ReminderScheduler {
        val scheduled = mutableListOf<Reminder>()
        override fun scheduleReminder(reminder: Reminder) { scheduled.add(reminder) }
        override fun cancelReminder(reminderId: Long) {}
        override fun cancelTaskReminders(taskId: Long) {}
        override fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long) {}
        override fun rescheduleAllActiveReminders() {}
    }

    private class FakeSarahPreferences : SarahPreferences {
        override var currentEnergyLevel: EnergyLevel = EnergyLevel.NORMAL
        override val energyLevelFlow: Flow<EnergyLevel> = flowOf(EnergyLevel.NORMAL)
        override var isOnboardingCompleted: Boolean = true
        override val onboardingCompletedFlow: Flow<Boolean> = flowOf(true)
        override var isDeadlineRemindersEnabled: Boolean = true
        override val deadlineRemindersEnabledFlow: Flow<Boolean> = flowOf(true)
        override var isCustomRemindersEnabled: Boolean = true
        override val customRemindersEnabledFlow: Flow<Boolean> = flowOf(true)
    }
}
