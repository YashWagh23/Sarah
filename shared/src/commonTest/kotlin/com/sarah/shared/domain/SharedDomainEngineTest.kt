package com.sarah.shared.domain

import com.sarah.app.domain.engine.HumanLanguageHelper
import com.sarah.app.domain.engine.NaturalLanguageTaskParser
import com.sarah.app.domain.engine.TaskPriorityScorer
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskBucket
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedDomainEngineTest {

    @Test
    fun testHumanLanguageHelperGreetings() {
        val morning = HumanLanguageHelper.getGreeting(8)
        assertTrue(morning.contains("Morning") || morning.contains("Ready") || morning.contains("Sarah"), "Morning greeting expected")

        val evening = HumanLanguageHelper.getGreeting(21)
        assertTrue(evening.contains("Evening") || evening.contains("Winding") || evening.contains("night"), "Evening greeting expected")
    }

    @Test
    fun testNaturalLanguageTaskParserQuickDraft() {
        val parser = NaturalLanguageTaskParser()
        val text = "Submit Math assignment by tomorrow 5pm high priority"
        val draft = parser.parseTask(text)

        assertNotNull(draft)
        assertTrue(draft.title.isNotBlank(), "Title should not be blank")
        assertEquals(TaskPriority.HIGH, draft.priority, "Should parse high priority")
    }

    @Test
    fun testTaskPriorityScorerUrgentDeadline() {
        val nowEpoch = 1750000000000L
        val urgentTask = Task(
            id = 1L,
            title = "Urgent Physics Lab",
            type = TaskType.ASSIGNMENT,
            priority = TaskPriority.HIGH,
            bucket = TaskBucket.ACADEMIC,
            energyRequirement = EnergyRequirement.HIGH,
            estimatedMinutes = 60,
            dueDateEpochMs = nowEpoch + (2 * 3600 * 1000L), // due in 2 hours
            status = TaskStatus.PENDING,
            createdAtEpochMs = nowEpoch - 10000L
        )

        val score = TaskPriorityScorer.calculatePriorityScore(
            task = urgentTask,
            currentEnergy = EnergyLevel.HIGH,
            nowEpochMs = nowEpoch
        )

        assertTrue(score >= 80.0, "Urgent high priority task should have a high score, got $score")
    }
}
