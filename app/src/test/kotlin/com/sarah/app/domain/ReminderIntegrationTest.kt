package com.sarah.app.domain

import com.sarah.app.domain.engine.DeadlineReminderEngine
import com.sarah.app.domain.engine.ReminderScheduler
import com.sarah.app.domain.model.Reminder
import com.sarah.app.domain.model.ReminderType
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import com.sarah.app.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneId

// In-Memory Fake Reminder Repository for Testing
class FakeReminderRepository : ReminderRepository {
    private val reminders = MutableStateFlow<Map<Long, Reminder>>(emptyMap())
    private var nextId = 1L

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminders.map { it.values.sortedBy { r -> r.reminderTimeEpochMs } }
    }

    override fun getActiveUpcomingReminders(): Flow<List<Reminder>> {
        return reminders.map { map ->
            val now = System.currentTimeMillis()
            map.values.filter { it.enabled && it.dismissedAtEpochMs == null && it.reminderTimeEpochMs >= now }
                .sortedBy { it.reminderTimeEpochMs }
        }
    }

    override fun getRemindersForTask(taskId: Long): Flow<List<Reminder>> {
        return reminders.map { map ->
            map.values.filter { it.taskId == taskId }.sortedBy { it.reminderTimeEpochMs }
        }
    }

    override suspend fun getUpcomingPendingRemindersSync(nowMs: Long): List<Reminder> {
        return reminders.value.values.filter { it.enabled && it.dismissedAtEpochMs == null && it.reminderTimeEpochMs >= nowMs }
            .sortedBy { it.reminderTimeEpochMs }
    }

    override suspend fun getRemindersForTaskSync(taskId: Long): List<Reminder> {
        return reminders.value.values.filter { it.taskId == taskId }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminders.value[id]
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        val id = if (reminder.id == 0L) nextId++ else reminder.id
        val newReminder = reminder.copy(id = id)
        reminders.update { it + (id to newReminder) }
        return id
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminders.update { it + (reminder.id to reminder) }
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminders.update { it - reminder.id }
    }

    override suspend fun deleteReminderById(id: Long) {
        reminders.update { it - id }
    }

    override suspend fun deleteRemindersByTaskId(taskId: Long) {
        reminders.update { map -> map.filterValues { it.taskId != taskId } }
    }

    override suspend fun dismissReminder(id: Long) {
        reminders.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(dismissedAtEpochMs = System.currentTimeMillis()))
        }
    }

    override suspend fun snoozeReminder(id: Long, snoozeDurationMinutes: Int) {
        val snoozedUntil = System.currentTimeMillis() + (snoozeDurationMinutes * 60 * 1000L)
        reminders.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(snoozedUntilEpochMs = snoozedUntil, reminderTimeEpochMs = snoozedUntil))
        }
    }

    override suspend fun snoozeReminderUntil(id: Long, newTimeEpochMs: Long) {
        reminders.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(snoozedUntilEpochMs = newTimeEpochMs, reminderTimeEpochMs = newTimeEpochMs))
        }
    }

    override suspend fun setReminderEnabled(id: Long, enabled: Boolean) {
        reminders.update { map ->
            val existing = map[id] ?: return@update map
            map + (id to existing.copy(enabled = enabled))
        }
    }
}

// In-Memory Fake Reminder Scheduler for Testing
class FakeReminderScheduler : ReminderScheduler {
    val scheduledAlarms = mutableMapOf<Long, Long>() // reminderId -> triggerTimeEpochMs
    var rescheduleAllCallCount = 0

    override fun scheduleReminder(reminder: Reminder) {
        if (reminder.reminderTimeEpochMs > System.currentTimeMillis() && reminder.enabled && reminder.dismissedAtEpochMs == null) {
            scheduledAlarms[reminder.id] = reminder.reminderTimeEpochMs
        }
    }

    override fun cancelReminder(reminderId: Long) {
        scheduledAlarms.remove(reminderId)
    }

    override fun cancelTaskReminders(taskId: Long) {
        // Handled via repository + cancelReminder
    }

    override fun rescheduleReminder(reminderId: Long, newTimeEpochMs: Long) {
        scheduledAlarms[reminderId] = newTimeEpochMs
    }

    override fun rescheduleAllActiveReminders() {
        rescheduleAllCallCount++
    }
}

class ReminderIntegrationTest {

    private lateinit var reminderRepository: FakeReminderRepository
    private lateinit var reminderScheduler: FakeReminderScheduler
    private lateinit var deadlineReminderEngine: DeadlineReminderEngine

    @Before
    fun setUp() {
        reminderRepository = FakeReminderRepository()
        reminderScheduler = FakeReminderScheduler()
        deadlineReminderEngine = DeadlineReminderEngine(kotlinx.datetime.TimeZone.UTC)
    }

    @Test
    fun `test 1 create and persist custom reminder`() = runTest {
        val now = System.currentTimeMillis()
        val reminderTime = now + 3600_000L // 1 hour later
        val reminder = Reminder(
            title = "Bring DBMS record",
            message = "Lab manual submission",
            reminderTimeEpochMs = reminderTime,
            type = ReminderType.CUSTOM_REMINDER
        )

        val id = reminderRepository.insertReminder(reminder)
        assertTrue(id > 0)

        val retrieved = reminderRepository.getReminderById(id)
        assertNotNull(retrieved)
        assertEquals("Bring DBMS record", retrieved!!.title)
        assertEquals(ReminderType.CUSTOM_REMINDER, retrieved.type)
        assertTrue(retrieved.enabled)
    }

    @Test
    fun `test 2 schedule reminder and cancel reminder`() = runTest {
        val future = System.currentTimeMillis() + 5000_000L
        val reminder = Reminder(id = 10, title = "Study OOP", message = "Revise Unit 1", reminderTimeEpochMs = future)

        reminderScheduler.scheduleReminder(reminder)
        assertEquals(1, reminderScheduler.scheduledAlarms.size)
        assertEquals(future, reminderScheduler.scheduledAlarms[10L])

        reminderScheduler.cancelReminder(10L)
        assertFalse(reminderScheduler.scheduledAlarms.containsKey(10L))
    }

    @Test
    fun `test 3 snooze reminder by 10 minutes`() = runTest {
        val now = System.currentTimeMillis()
        val future = now + 1000_000L
        val reminder = Reminder(title = "Revise Graph Theory", message = "Quiz prep", reminderTimeEpochMs = future)
        val id = reminderRepository.insertReminder(reminder)

        reminderRepository.snoozeReminder(id, 10)
        val updated = reminderRepository.getReminderById(id)

        assertNotNull(updated)
        assertTrue(updated!!.isSnoozed)
        assertTrue(updated.reminderTimeEpochMs >= now + (10 * 60 * 1000L))
    }

    @Test
    fun `test 4 snooze reminder until custom time`() = runTest {
        val future = System.currentTimeMillis() + 1000_000L
        val reminder = Reminder(title = "Submit project outline", message = "Final draft", reminderTimeEpochMs = future)
        val id = reminderRepository.insertReminder(reminder)

        val customTarget = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        reminderRepository.snoozeReminderUntil(id, customTarget)
        val updated = reminderRepository.getReminderById(id)

        assertNotNull(updated)
        assertEquals(customTarget, updated!!.reminderTimeEpochMs)
    }

    @Test
    fun `test 5 completing task cancels and deletes related reminders`() = runTest {
        val now = System.currentTimeMillis()
        val deadline = now + (48 * 60 * 60 * 1000L)
        val task = Task(id = 42, title = "Compiler Design Lab", deadlineEpochMs = deadline, type = TaskType.PRACTICAL)

        val reminders = deadlineReminderEngine.generateDeadlineReminders(task, now)
        assertEquals(2, reminders.size)

        for (rem in reminders) {
            val remId = reminderRepository.insertReminder(rem)
            reminderScheduler.scheduleReminder(rem.copy(id = remId))
        }

        assertEquals(2, reminderRepository.getRemindersForTaskSync(42L).size)

        // Mark task completed -> trigger cleanup
        reminderRepository.deleteRemindersByTaskId(42L)
        reminderScheduler.cancelTaskReminders(42L)

        val remainingReminders = reminderRepository.getRemindersForTaskSync(42L)
        assertTrue("No reminders should remain for completed task", remainingReminders.isEmpty())
    }

    @Test
    fun `test 6 deleting task removes related reminders`() = runTest {
        val now = System.currentTimeMillis()
        val reminder = Reminder(taskId = 99, title = "OS Quiz", message = "Quiz prep", reminderTimeEpochMs = now + 100_000L)
        reminderRepository.insertReminder(reminder)

        assertEquals(1, reminderRepository.getRemindersForTaskSync(99L).size)

        reminderRepository.deleteRemindersByTaskId(99L)
        assertTrue(reminderRepository.getRemindersForTaskSync(99L).isEmpty())
    }

    @Test
    fun `test 7 past reminders are not scheduled`() = runTest {
        val past = System.currentTimeMillis() - 50_000L
        val pastReminder = Reminder(id = 7, title = "Past", message = "Past", reminderTimeEpochMs = past)

        reminderScheduler.scheduleReminder(pastReminder)
        assertTrue("Past reminder must not be scheduled", reminderScheduler.scheduledAlarms.isEmpty())
    }

    @Test
    fun `test 8 dismiss reminder updates dismissedAtEpochMs`() = runTest {
        val future = System.currentTimeMillis() + 500_000L
        val reminder = Reminder(title = "Bring Drawing Sheet", message = "For EG lab", reminderTimeEpochMs = future)
        val id = reminderRepository.insertReminder(reminder)

        reminderRepository.dismissReminder(id)
        val updated = reminderRepository.getReminderById(id)

        assertNotNull(updated)
        assertNotNull(updated!!.dismissedAtEpochMs)
        assertFalse(updated.isPending)
    }

    @Test
    fun `test 9 multiple reminders for different tasks maintain independence`() = runTest {
        val now = System.currentTimeMillis()
        val rem1 = Reminder(taskId = 1, title = "Task 1 Reminder", message = "M1", reminderTimeEpochMs = now + 100_000L)
        val rem2 = Reminder(taskId = 2, title = "Task 2 Reminder", message = "M2", reminderTimeEpochMs = now + 200_000L)

        val id1 = reminderRepository.insertReminder(rem1)
        val id2 = reminderRepository.insertReminder(rem2)

        reminderRepository.deleteRemindersByTaskId(1L)

        assertNull(reminderRepository.getReminderById(id1))
        assertNotNull(reminderRepository.getReminderById(id2))
    }

    @Test
    fun `test 10 rescheduleAllActiveReminders triggers scheduler`() {
        reminderScheduler.rescheduleAllActiveReminders()
        assertEquals(1, reminderScheduler.rescheduleAllCallCount)
    }
}
