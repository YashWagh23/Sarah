package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.Difficulty
import com.sarah.app.domain.model.EnergyRequirement
import com.sarah.app.domain.model.Task
import com.sarah.app.domain.model.TaskPriority
import com.sarah.app.domain.model.TaskStatus
import com.sarah.app.domain.model.TaskType

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val subjectId: Long?,
    val subjectName: String?,
    val type: String,
    val description: String,
    val deadlineEpochMs: Long,
    val estimatedMinutes: Int,
    val priority: String,
    val difficulty: String,
    val energyRequirement: String,
    val status: String,
    val completionPercentage: Int,
    val createdAtEpochMs: Long,
    val completedAtEpochMs: Long?
) {
    fun toDomain(): Task {
        return Task(
            id = id,
            title = title,
            subjectId = subjectId,
            subjectName = subjectName,
            type = runCatching { TaskType.valueOf(type) }.getOrDefault(TaskType.ASSIGNMENT),
            description = description,
            deadlineEpochMs = deadlineEpochMs,
            estimatedMinutes = estimatedMinutes,
            priority = runCatching { TaskPriority.valueOf(priority) }.getOrDefault(TaskPriority.MEDIUM),
            difficulty = runCatching { Difficulty.valueOf(difficulty) }.getOrDefault(Difficulty.MEDIUM),
            energyRequirement = runCatching { EnergyRequirement.valueOf(energyRequirement) }.getOrDefault(EnergyRequirement.MEDIUM),
            status = runCatching { TaskStatus.valueOf(status) }.getOrDefault(TaskStatus.PENDING),
            completionPercentage = completionPercentage,
            createdAtEpochMs = createdAtEpochMs,
            completedAtEpochMs = completedAtEpochMs
        )
    }

    companion object {
        fun fromDomain(task: Task): TaskEntity {
            return TaskEntity(
                id = task.id,
                title = task.title,
                subjectId = task.subjectId,
                subjectName = task.subjectName,
                type = task.type.name,
                description = task.description,
                deadlineEpochMs = task.deadlineEpochMs,
                estimatedMinutes = task.estimatedMinutes,
                priority = task.priority.name,
                difficulty = task.difficulty.name,
                energyRequirement = task.energyRequirement.name,
                status = task.status.name,
                completionPercentage = task.completionPercentage,
                createdAtEpochMs = task.createdAtEpochMs,
                completedAtEpochMs = task.completedAtEpochMs
            )
        }
    }
}
