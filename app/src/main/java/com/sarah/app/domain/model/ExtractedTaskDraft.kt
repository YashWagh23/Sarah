package com.sarah.app.domain.model

enum class CaptureSourceType(val displayName: String) {
    NATURAL_LANGUAGE("Natural Language Text"),
    PDF_DOCUMENT("PDF Document"),
    IMAGE_GALLERY("Gallery Image (JPG/JPEG)")
}

data class ExtractedTaskDraft(
    val title: String = "",
    val subjectId: Long? = null,
    val subjectName: String? = null,
    val type: TaskType = TaskType.ASSIGNMENT,
    val description: String = "",
    val deadlineEpochMs: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000),
    val estimatedMinutes: Int = 45,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val energyRequirement: EnergyRequirement = EnergyRequirement.MEDIUM,
    val confidenceScore: Float = 1.0f,
    val sourceType: CaptureSourceType = CaptureSourceType.NATURAL_LANGUAGE,
    val rawExtractedText: String = ""
) {
    fun toTask(): Task {
        return Task(
            id = 0,
            title = title.ifBlank { "Academic Task" },
            subjectId = subjectId,
            subjectName = subjectName,
            type = type,
            description = description,
            deadlineEpochMs = deadlineEpochMs,
            estimatedMinutes = estimatedMinutes,
            priority = priority,
            difficulty = difficulty,
            energyRequirement = energyRequirement,
            status = TaskStatus.PENDING,
            completionPercentage = 0,
            createdAtEpochMs = System.currentTimeMillis(),
            completedAtEpochMs = null
        )
    }
}
