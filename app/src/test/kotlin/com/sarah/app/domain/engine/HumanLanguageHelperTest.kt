package com.sarah.app.domain.engine

import com.sarah.app.domain.model.FeasibilityReport
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HumanLanguageHelperTest {

    @Test
    fun `test formatDailySummary when all caught up`() {
        val summary = HumanLanguageHelper.formatDailySummary(0, 0, 0)
        assertEquals("You're all caught up 🎉", summary)
    }

    @Test
    fun `test formatDailySummary with active tasks, deadlines and reminders`() {
        val summary = HumanLanguageHelper.formatDailySummary(
            pendingTasksCount = 3,
            tomorrowDeadlinesCount = 1,
            remindersCount = 2
        )
        assertEquals("3 to do • 1 deadline tomorrow • 2 reminders", summary)
    }

    @Test
    fun `test formatCapacitySummary formats hours and minutes nicely`() {
        assertEquals("You have about 2 hr 15 min of focus time tonight.", HumanLanguageHelper.formatCapacitySummary(135))
        assertEquals("You have about 2 hours of study time tonight.", HumanLanguageHelper.formatCapacitySummary(120))
        assertEquals("You have about 45 minutes of focus time tonight.", HumanLanguageHelper.formatCapacitySummary(45))
        assertEquals("No study time remaining for tonight.", HumanLanguageHelper.formatCapacitySummary(0))
    }

    @Test
    fun `test formatFeasibilityHeadline and subtext for overloaded status`() {
        val report = FeasibilityReport(
            currentTimeMinutes = 1200,
            sleepTimeMinutes = 1410,
            minutesUntilSleep = 210,
            rawAvailableMinutes = 210,
            realisticProductiveMinutes = 150,
            totalRequiredMinutes = 250,
            mustDoMinutes = 120,
            status = FeasibilityStatus.OVERLOADED,
            mustDoTasks = listOf(Task(id = 1, title = "Task 1", deadlineEpochMs = 0)),
            shouldDoTasks = emptyList(),
            canDeferTasks = listOf(Task(id = 2, title = "Task 2", deadlineEpochMs = 0)),
            suggestedAgenda = emptyList(),
            guidanceMessage = ""
        )

        val headline = HumanLanguageHelper.formatFeasibilityHeadline(report)
        val subtext = HumanLanguageHelper.formatFeasibilitySubtext(report)

        assertTrue(headline.contains("Sarah adjusted your plan"))
        assertTrue(subtext.contains("Sarah moved 1 lower-priority task to tomorrow"))
    }
}
