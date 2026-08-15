package com.sarah.app.domain.model

enum class FeasibilityStatus(
    val title: String,
    val colorHex: String,
    val subtitle: String
) {
    OPTIMAL("Optimal Workload", "#10B981", "You have plenty of time to finish and rest comfortably."),
    MANAGEABLE("Manageable", "#3B82F6", "Achievable with focused study sessions."),
    TIGHT("Tight Schedule", "#F59E0B", "High workload tonight. Minimize distractions and stick to priorities."),
    OVERLOADED("Overloaded (High Risk)", "#EF4444", "You probably cannot finish everything comfortably tonight.")
}

enum class TaskBucket(val title: String, val badge: String) {
    MUST_DO("Must Do Tonight", "Urgent / High Priority"),
    SHOULD_DO("Should Do Tonight", "Important Revision"),
    CAN_DEFER("Can Defer", "Optional / Postponable")
}

data class AgendaItem(
    val startTimeFormatted: String,
    val endTimeFormatted: String,
    val title: String,
    val subtitle: String,
    val isBreak: Boolean,
    val durationMinutes: Int,
    val taskId: Long? = null
)

data class FeasibilityReport(
    val currentTimeMinutes: Int,
    val sleepTimeMinutes: Int,
    val minutesUntilSleep: Int,
    val rawAvailableMinutes: Int,
    val realisticProductiveMinutes: Int,
    val totalRequiredMinutes: Int,
    val mustDoMinutes: Int,
    val status: FeasibilityStatus,
    val mustDoTasks: List<Task>,
    val shouldDoTasks: List<Task>,
    val canDeferTasks: List<Task>,
    val suggestedAgenda: List<AgendaItem>,
    val guidanceMessage: String
)
