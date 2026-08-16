package com.sarah.app.domain.model

enum class TaskType(val displayName: String) {
    ASSIGNMENT("Assignment"),
    PRACTICAL("Practical"),
    EXAM_PREP("Exam Prep"),
    REVISION("Revision"),
    READING("Reading"),
    PROJECT("Project"),
    SUBMISSION("Submission"),
    OTHER("Other")
}

enum class TaskPriority(val displayName: String, val level: Int) {
    CRITICAL("Critical", 4),
    HIGH("High", 3),
    MEDIUM("Medium", 2),
    LOW("Low", 1)
}

enum class Difficulty(val displayName: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard")
}

enum class EnergyRequirement(val displayName: String) {
    LOW("Low Energy"),
    MEDIUM("Medium Energy"),
    HIGH("High Focus")
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    POSTPONED
}

data class Task(
    val id: Long = 0,
    val title: String,
    val subjectId: Long? = null,
    val subjectName: String? = null,
    val type: TaskType = TaskType.ASSIGNMENT,
    val description: String = "",
    val deadlineEpochMs: Long,
    val estimatedMinutes: Int = 45,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val energyRequirement: EnergyRequirement = EnergyRequirement.MEDIUM,
    val status: TaskStatus = TaskStatus.PENDING,
    val completionPercentage: Int = 0,
    val completedMinutes: Int = 0,
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val completedAtEpochMs: Long? = null
) {
    val remainingMinutes: Int
        get() = maxOf(0, estimatedMinutes - completedMinutes)
}
