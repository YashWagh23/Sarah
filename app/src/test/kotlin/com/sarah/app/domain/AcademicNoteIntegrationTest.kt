package com.sarah.app.domain

import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.AcademicNote
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Subject
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.AcademicNoteRepository
import com.sarah.app.domain.repository.ReminderRepository
import com.sarah.app.domain.repository.SubjectRepository
import com.sarah.app.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneId

class AcademicNoteIntegrationTest {

    private lateinit var noteRepository: FakeAcademicNoteRepository
    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var reminderRepository: FakeReminderRepository
    private lateinit var reminderScheduler: FakeReminderScheduler
    private lateinit var deadlineReminderEngine: DeadlineReminderEngine

    @Before
    fun setUp() {
        noteRepository = FakeAcademicNoteRepository()
        taskRepository = FakeTaskRepository()
        reminderRepository = FakeReminderRepository()
        reminderScheduler = FakeReminderScheduler()
        deadlineReminderEngine = DeadlineReminderEngine(ZoneId.of("UTC"))
    }

    @Test
    fun `test creating and retrieving an academic note`() = runBlocking {
        val note = AcademicNote(
            title = "Prof. Sharma Office Hours",
            content = "Wednesday 3 PM to 5 PM, Cabin B-12",
            subjectId = 1L,
            subjectName = "Java & OOP"
        )
        val id = noteRepository.insertNote(note)
        assertTrue(id > 0)

        val retrieved = noteRepository.getNoteById(id)
        assertNotNull(retrieved)
        assertEquals("Prof. Sharma Office Hours", retrieved?.title)
        assertEquals("Wednesday 3 PM to 5 PM, Cabin B-12", retrieved?.content)
        assertEquals(1L, retrieved?.subjectId)
        assertFalse(retrieved?.isPinned ?: true)
    }

    @Test
    fun `test updating an academic note content and title`() = runBlocking {
        val id = noteRepository.insertNote(
            AcademicNote(
                title = "Initial Syllabus",
                content = "Chapters 1-3"
            )
        )

        val existing = noteRepository.getNoteById(id)
        assertNotNull(existing)

        val updated = existing!!.copy(
            title = "Revised Syllabus",
            content = "Chapters 1-4, skip chapter 3 proofs"
        )
        noteRepository.updateNote(updated)

        val afterUpdate = noteRepository.getNoteById(id)
        assertEquals("Revised Syllabus", afterUpdate?.title)
        assertEquals("Chapters 1-4, skip chapter 3 proofs", afterUpdate?.content)
    }

    @Test
    fun `test toggling pin status on an academic note`() = runBlocking {
        val id = noteRepository.insertNote(
            AcademicNote(
                title = "Important Formula Sheet",
                content = "Euler's method formula: y_n+1 = y_n + h*f(x_n, y_n)",
                isPinned = false
            )
        )

        noteRepository.togglePin(id, true)
        var note = noteRepository.getNoteById(id)
        assertTrue(note?.isPinned == true)

        noteRepository.togglePin(id, false)
        note = noteRepository.getNoteById(id)
        assertFalse(note?.isPinned == true)
    }

    @Test
    fun `test filtering notes by subject`() = runBlocking {
        noteRepository.insertNote(AcademicNote(title = "Java Rule 1", content = "Interfaces", subjectId = 1L, subjectName = "Java"))
        noteRepository.insertNote(AcademicNote(title = "DBMS Rule 1", content = "BCNF", subjectId = 2L, subjectName = "DBMS"))
        noteRepository.insertNote(AcademicNote(title = "Java Rule 2", content = "Generics", subjectId = 1L, subjectName = "Java"))
        noteRepository.insertNote(AcademicNote(title = "General Tip", content = "Attendance", subjectId = null))

        val javaNotes = noteRepository.getNotesBySubject(1L).first()
        assertEquals(2, javaNotes.size)
        assertTrue(javaNotes.all { it.subjectId == 1L })

        val dbmsNotes = noteRepository.getNotesBySubject(2L).first()
        assertEquals(1, dbmsNotes.size)
        assertEquals("DBMS Rule 1", dbmsNotes[0].title)
    }

    @Test
    fun `test deleting an academic note`() = runBlocking {
        val id = noteRepository.insertNote(
            AcademicNote(
                title = "Temporary Note",
                content = "To be deleted"
            )
        )

        val retrieved = noteRepository.getNoteById(id)
        assertNotNull(retrieved)

        noteRepository.deleteNoteById(id)
        val afterDelete = noteRepository.getNoteById(id)
        assertNull(afterDelete)
    }

    @Test
    fun `test converting academic note to task`() = runBlocking {
        val note = AcademicNote(
            id = 5L,
            title = "Submit Lab Observation Slip",
            content = "Hand in signed observation sheet to Prof. Gupta",
            subjectId = 3L,
            subjectName = "Operating Systems"
        )

        // Convert note to task
        val now = System.currentTimeMillis()
        val tomorrow = now + 86400000L
        val task = Task(
            id = 0,
            title = note.title,
            subjectId = note.subjectId,
            subjectName = note.subjectName,
            type = TaskType.PRACTICAL,
            description = note.content,
            deadlineEpochMs = tomorrow,
            estimatedMinutes = 30,
            priority = TaskPriority.HIGH,
            status = TaskStatus.PENDING,
            createdAtEpochMs = now
        )

        val newTaskId = taskRepository.insertTask(task)
        assertTrue(newTaskId > 0)

        val createdTask = taskRepository.getTaskById(newTaskId)
        assertNotNull(createdTask)
        assertEquals("Submit Lab Observation Slip", createdTask?.title)
        assertEquals("Operating Systems", createdTask?.subjectName)
        assertEquals(3L, createdTask?.subjectId)
        assertEquals(TaskStatus.PENDING, createdTask?.status)
    }

    @Test
    fun `test converting academic note to reminder`() = runBlocking {
        val note = AcademicNote(
            id = 10L,
            title = "Ask Ma'am about Project Viva",
            content = "Clarify whether PPT presentation is required",
            subjectId = 1L,
            subjectName = "Java & OOP"
        )

        val reminderTime = System.currentTimeMillis() + 3600000L // 1 hour from now
        val reminder = Reminder(
            taskId = null,
            taskTitle = note.subjectName,
            title = note.title,
            message = note.content,
            reminderTimeEpochMs = reminderTime,
            type = ReminderType.CUSTOM_REMINDER
        )

        val reminderId = reminderRepository.insertReminder(reminder)
        assertTrue(reminderId > 0)
        reminderScheduler.scheduleReminder(reminder.copy(id = reminderId))

        val retrievedReminder = reminderRepository.getReminderById(reminderId)
        assertNotNull(retrievedReminder)
        assertEquals("Ask Ma'am about Project Viva", retrievedReminder?.title)
        assertEquals("Java & OOP", retrievedReminder?.taskTitle)
        assertEquals(1, reminderScheduler.scheduledReminders.size)
        assertEquals(reminderId, reminderScheduler.scheduledReminders[0].id)
    }

    // In-memory fake repositories for testing
    private class FakeAcademicNoteRepository : AcademicNoteRepository {
        private val notes = mutableListOf<AcademicNote>()
        private val notesFlow = MutableStateFlow<List<AcademicNote>>(emptyList())
        private var nextId = 1L

        override fun getAllNotes(): Flow<List<AcademicNote>> = notesFlow.map { list ->
            list.sortedWith(compareByDescending<AcademicNote> { it.isPinned }.thenByDescending { it.updatedEpochMs })
        }

        override fun getNotesBySubject(subjectId: Long): Flow<List<AcademicNote>> = notesFlow.map { list ->
            list.filter { it.subjectId == subjectId }
                .sortedWith(compareByDescending<AcademicNote> { it.isPinned }.thenByDescending { it.updatedEpochMs })
        }

        override suspend fun getNoteById(id: Long): AcademicNote? {
            return notes.find { it.id == id }
        }

        override suspend fun insertNote(note: AcademicNote): Long {
            val id = if (note.id == 0L) nextId++ else note.id
            val newNote = note.copy(id = id)
            notes.add(newNote)
            notesFlow.value = notes.toList()
            return id
        }

        override suspend fun updateNote(note: AcademicNote) {
            val index = notes.indexOfFirst { it.id == note.id }
            if (index != -1) {
                notes[index] = note
                notesFlow.value = notes.toList()
            }
        }

        override suspend fun deleteNote(note: AcademicNote) {
            deleteNoteById(note.id)
        }

        override suspend fun deleteNoteById(id: Long) {
            notes.removeAll { it.id == id }
            notesFlow.value = notes.toList()
        }

        override suspend fun togglePin(id: Long, isPinned: Boolean) {
            val note = getNoteById(id)
            if (note != null) {
                updateNote(note.copy(isPinned = isPinned, updatedEpochMs = System.currentTimeMillis()))
            }
        }
    }

    private class FakeTaskRepository : TaskRepository {
        private val tasks = mutableListOf<Task>()
        private var nextId = 1L

        override fun getAllTasks(): Flow<List<Task>> = flowOf(tasks.toList())
        override fun getActiveTasks(): Flow<List<Task>> = flowOf(tasks.filter { it.status != TaskStatus.COMPLETED })
        override fun getTasksBySubject(subjectId: Long): Flow<List<Task>> = flowOf(tasks.filter { it.subjectId == subjectId })
        override suspend fun getTaskById(id: Long): Task? = tasks.find { it.id == id }
        override suspend fun insertTask(task: Task): Long {
            val id = if (task.id == 0L) nextId++ else task.id
            tasks.add(task.copy(id = id))
            return id
        }
        override suspend fun updateTask(task: Task) {
            val index = tasks.indexOfFirst { it.id == task.id }
            if (index != -1) tasks[index] = task
        }
        override suspend fun deleteTask(task: Task) { tasks.removeAll { it.id == task.id } }
        override suspend fun deleteTaskById(id: Long) { tasks.removeAll { it.id == id } }
        override suspend fun updateTaskStatus(id: Long, status: TaskStatus) {
            val task = getTaskById(id)
            if (task != null) updateTask(task.copy(status = status))
        }
    }

    private class FakeReminderRepository : ReminderRepository {
        private val reminders = mutableListOf<Reminder>()
        private var nextId = 1L

        override fun getAllReminders(): Flow<List<Reminder>> = flowOf(reminders.toList())
        override fun getActiveUpcomingReminders(): Flow<List<Reminder>> = flowOf(reminders.filter { it.enabled && it.dismissedAtEpochMs == null })
        override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> = flowOf(reminders.filter { it.taskId == taskId })
        override suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<Reminder> = reminders.filter { it.enabled && it.dismissedAtEpochMs == null && it.reminderTimeEpochMs > nowMs }
        override suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder> = reminders.filter { it.taskId == taskId }
        override suspend fun getReminderById(id: Long): Reminder? = reminders.find { it.id == id }
        override suspend fun insertReminder(reminder: Reminder): Long {
            val id = if (reminder.id == 0L) nextId++ else reminder.id
            reminders.add(reminder.copy(id = id))
            return id
        }
        override suspend fun updateReminder(reminder: Reminder) {
            val index = reminders.indexOfFirst { it.id == reminder.id }
            if (index != -1) reminders[index] = reminder
        }
        override suspend fun deleteReminder(reminder: Reminder) { reminders.removeAll { it.id == reminder.id } }
        override suspend fun deleteReminderById(id: Long) { reminders.removeAll { it.id == id } }
        override suspend fun deleteRemindersByTaskId(taskId: Long) { reminders.removeAll { it.taskId == taskId } }
        override suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int) {
            val r = getReminderById(id)
            if (r != null) {
                val newTime = r.reminderTimeEpochMs + (snoozeDurationMinutes * 60000L)
                updateReminder(r.copy(reminderTimeEpochMs = newTime, snoozedUntilEpochMs = newTime))
            }
        }
        override suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long) {
            val r = getReminderById(id)
            if (r != null) {
                updateReminder(r.copy(reminderTimeEpochMs = newTimeEpochMs, snoozedUntilEpochMs = newTimeEpochMs))
            }
        }
        override suspend fun dismissReminder(id: Long) {
            val r = getReminderById(id)
            if (r != null) {
                updateReminder(r.copy(dismissedAtEpochMs = System.currentTimeMillis()))
            }
        }
        override suspend fun setReminderEnabled(id: Long, enabled: Boolean) {
            val r = getReminderById(id)
            if (r != null) {
                updateReminder(r.copy(enabled = enabled))
            }
        }
    }

    private class FakeReminderScheduler : ReminderScheduler {
        val scheduledReminders = mutableListOf<Reminder>()
        val cancelledReminderIds = mutableListOf<Long>()

        override fun scheduleReminder(reminder: Reminder) {
            scheduledReminders.removeAll { it.id == reminder.id }
            scheduledReminders.add(reminder)
        }
        override fun cancelReminder(reminderId: Long) {
            cancelledReminderIds.add(reminderId)
            scheduledReminders.removeAll { it.id == reminderId }
        }
        override fun cancelTaskReminders(taskId: Long) {
            scheduledReminders.removeAll { it.taskId == taskId }
        }
        override fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long) {
            val r = scheduledReminders.find { it.id == reminderId }
            if (r != null) {
                scheduleReminder(r.copy(reminderTimeEpochMs = newTimeEpochMs))
            }
        }
        override fun rescheduleAllActiveReminders() {}
    }
}
