package com.sarah.app.domain.engine

import com.sarah.app.domain.model.FeasibilityReport
import com.sarah.app.domain.model.FeasibilityStatus

object HumanLanguageHelper {

    fun formatFeasibilityHeadline(report: FeasibilityReport): String {
        return when (report.status) {
            FeasibilityStatus.OPTIMAL -> "You're in great shape for tonight."
            FeasibilityStatus.MANAGEABLE -> "Tonight is well balanced and manageable."
            FeasibilityStatus.TIGHT -> "Tonight is a bit full, but very doable."
            FeasibilityStatus.OVERLOADED -> "Tonight is a little heavy. Sarah adjusted your plan."
        }
    }

    fun formatFeasibilitySubtext(report: FeasibilityReport): String {
        val deferredCount = report.canDeferTasks.size
        return when (report.status) {
            FeasibilityStatus.OPTIMAL -> "All planned study fits comfortably with proper breaks."
            FeasibilityStatus.MANAGEABLE -> "Focus on your scheduled sessions and you will finish on time."
            FeasibilityStatus.TIGHT -> "Stick to your top priorities and take restorative breaks."
            FeasibilityStatus.OVERLOADED -> {
                if (deferredCount > 0) {
                    "Sarah moved $deferredCount lower-priority task${if (deferredCount > 1) "s" else ""} to tomorrow so you don't burn out."
                } else {
                    "Consider postponing non-urgent tasks to protect your sleep."
                }
            }
        }
    }

    fun formatCapacitySummary(realisticCapacityMinutes: Int): String {
        val hours = realisticCapacityMinutes / 60
        val mins = realisticCapacityMinutes % 60
        return when {
            hours > 0 && mins > 0 -> "You have about $hours hr ${mins} min of focus time tonight."
            hours > 0 -> "You have about $hours hours of study time tonight."
            mins > 0 -> "You have about $mins minutes of focus time tonight."
            else -> "No study time remaining for tonight."
        }
    }

    fun formatDailySummary(
        pendingTasksCount: Int,
        tomorrowDeadlinesCount: Int,
        remindersCount: Int
    ): String {
        if (pendingTasksCount == 0 && tomorrowDeadlinesCount == 0 && remindersCount == 0) {
            return "You're all caught up 🎉"
        }

        val parts = mutableListOf<String>()
        if (pendingTasksCount > 0) {
            parts.add("$pendingTasksCount to do")
        }
        if (tomorrowDeadlinesCount > 0) {
            parts.add("$tomorrowDeadlinesCount deadline${if (tomorrowDeadlinesCount > 1) "s" else ""} tomorrow")
        }
        if (remindersCount > 0) {
            parts.add("$remindersCount reminder${if (remindersCount > 1) "s" else ""}")
        }

        return parts.joinToString(" • ")
    }
}
