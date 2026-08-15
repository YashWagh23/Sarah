package com.sarah.app.domain.engine

import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskBucket
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskType
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

data class ScoredTask(
    val task: Task,
    val score: Double,
    val bucket: TaskBucket,
    val reason: String
)

class TaskPriorityScorer {

    /**
     * Deterministically scores a task based on:
     * 1. Deadline urgency (hours remaining)
     * 2. Explicit task priority (Critical, High, Medium, Low)
     * 3. Task type importance (Practical, Submission, Exam Prep, Assignment, Revision, Reading)
     * 4. Energy fit (matches student's current energy with task difficulty/requirement)
     * 5. Effort efficiency (shorter remaining tasks get a quick-win boost)
     */
    fun scoreTask(
        task: Task,
        currentEnergy: EnergyLevel,
        currentDate: LocalDate = LocalDate.now(),
        currentTime: LocalTime = LocalTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): ScoredTask {
        val currentEpochMs = currentDate.atTime(currentTime).atZone(zoneId).toInstant().toEpochMilli()
        val msUntilDeadline = task.deadlineEpochMs - currentEpochMs
        val hoursUntilDeadline = msUntilDeadline / (1000.0 * 60.0 * 60.0)

        // 1. Deadline Urgency Weight (Max 60.0)
        val (deadlineWeight, deadlineReason) = when {
            hoursUntilDeadline <= 18.0 -> 60.0 to "Due tonight or tomorrow morning"
            hoursUntilDeadline <= 36.0 -> 42.0 to "Due tomorrow"
            hoursUntilDeadline <= 60.0 -> 26.0 to "Due in 2 days"
            hoursUntilDeadline <= 120.0 -> 12.0 to "Due this week"
            else -> 0.0 to "Due later"
        }

        // 2. Explicit Priority Weight (Max 40.0)
        val (priorityWeight, priorityReason) = when (task.priority) {
            TaskPriority.CRITICAL -> 40.0 to "Critical priority"
            TaskPriority.HIGH -> 25.0 to "High priority"
            TaskPriority.MEDIUM -> 12.0 to "Medium priority"
            TaskPriority.LOW -> 0.0 to "Low priority"
        }

        // 3. Task Type Weight (Max 25.0)
        val (typeWeight, typeReason) = when (task.type) {
            TaskType.PRACTICAL, TaskType.SUBMISSION -> 25.0 to "Formal submission/practical"
            TaskType.EXAM_PREP -> 20.0 to "Exam preparation"
            TaskType.ASSIGNMENT -> 15.0 to "Graded assignment"
            TaskType.REVISION -> 8.0 to "Revision"
            TaskType.PROJECT -> 12.0 to "Project milestone"
            TaskType.READING, TaskType.OTHER -> 4.0 to "General study"
        }

        // 4. Energy Fit Weight (-35.0 to +20.0)
        val (energyFitWeight, energyReason) = when (currentEnergy) {
            EnergyLevel.HIGH -> when (task.energyRequirement) {
                EnergyRequirement.HIGH -> 15.0 to "High energy fits deep focus work"
                EnergyRequirement.MEDIUM -> 6.0 to "Moderate energy fit"
                EnergyRequirement.LOW -> 0.0 to "Low energy task"
            }
            EnergyLevel.NORMAL -> when (task.energyRequirement) {
                EnergyRequirement.HIGH -> 5.0 to "Standard focus capacity"
                EnergyRequirement.MEDIUM -> 8.0 to "Balanced task fit"
                EnergyRequirement.LOW -> 5.0 to "Manageable task"
            }
            EnergyLevel.LOW -> when (task.energyRequirement) {
                EnergyRequirement.LOW -> 18.0 to "Low energy task provides easy progress"
                EnergyRequirement.MEDIUM -> 0.0 to "Medium energy requirement"
                EnergyRequirement.HIGH -> -18.0 to "High focus needed while student energy is low"
            }
            EnergyLevel.EXHAUSTED -> when (task.energyRequirement) {
                EnergyRequirement.LOW -> 22.0 to "Light task safe during fatigue"
                EnergyRequirement.MEDIUM -> -10.0 to "Medium task during fatigue"
                EnergyRequirement.HIGH -> -35.0 to "Intense task avoided to prevent burnout"
            }
        }

        // 5. Effort Efficiency / Remaining Duration Weight (Max +10.0, Min -6.0)
        val remaining = task.remainingMinutes
        val (efficiencyWeight, efficiencyReason) = when {
            remaining <= 25 -> 10.0 to "Quick win (<= 25m)"
            remaining <= 45 -> 5.0 to "Standard session size"
            remaining > 90 -> -6.0 to "Large task requires multiple focus blocks"
            else -> 0.0 to ""
        }

        val totalScore = deadlineWeight + priorityWeight + typeWeight + energyFitWeight + efficiencyWeight

        // Bucket classification
        val bucket = when {
            // Must do if urgent deadline AND critical/high priority or practical/submission, or score >= 75
            (hoursUntilDeadline <= 36.0 && (task.priority == TaskPriority.CRITICAL || task.priority == TaskPriority.HIGH || task.type == TaskType.PRACTICAL || task.type == TaskType.SUBMISSION)) || totalScore >= 75.0 -> {
                if (currentEnergy == EnergyLevel.EXHAUSTED && task.energyRequirement == EnergyRequirement.HIGH && task.priority != TaskPriority.CRITICAL) {
                    TaskBucket.SHOULD_DO
                } else {
                    TaskBucket.MUST_DO
                }
            }
            totalScore >= 40.0 || hoursUntilDeadline <= 60.0 || task.type == TaskType.REVISION || task.type == TaskType.EXAM_PREP -> {
                if (currentEnergy == EnergyLevel.EXHAUSTED && task.energyRequirement == EnergyRequirement.HIGH) {
                    TaskBucket.CAN_DEFER
                } else {
                    TaskBucket.SHOULD_DO
                }
            }
            else -> TaskBucket.CAN_DEFER
        }

        val primaryReason = listOf(deadlineReason, priorityReason, typeReason, energyReason, efficiencyReason)
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("; ")

        return ScoredTask(
            task = task,
            score = totalScore,
            bucket = bucket,
            reason = primaryReason
        )
    }

    fun prioritizeTasks(
        tasks: List<Task>,
        currentEnergy: EnergyLevel,
        currentDate: LocalDate = LocalDate.now(),
        currentTime: LocalTime = LocalTime.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<ScoredTask> {
        return tasks
            .map { scoreTask(it, currentEnergy, currentDate, currentTime, zoneId) }
            .sortedWith(
                compareByDescending<ScoredTask> { it.bucket == TaskBucket.MUST_DO }
                    .thenByDescending { it.bucket == TaskBucket.SHOULD_DO }
                    .thenByDescending { it.score }
                    .thenBy { it.task.deadlineEpochMs }
                    .thenBy { it.task.remainingMinutes }
            )
    }
}
