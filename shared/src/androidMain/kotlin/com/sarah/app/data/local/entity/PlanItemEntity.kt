package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.PlanItem
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.PlanItemType

@Entity(
    tableName = "plan_items",
    foreignKeys = [
        ForeignKey(
            entity = DailyPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["dailyPlanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dailyPlanId"])]
)
data class PlanItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dailyPlanId: Long,
    val taskId: Long?,
    val taskTitle: String,
    val subjectName: String?,
    val type: String,
    val status: String,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val durationMinutes: Int,
    val orderIndex: Int,
    val reason: String,
    val isBreak: Boolean
) {
    fun toDomain(): PlanItem {
        return PlanItem(
            id = id,
            dailyPlanId = dailyPlanId,
            taskId = taskId,
            taskTitle = taskTitle,
            subjectName = subjectName,
            type = runCatching { PlanItemType.valueOf(type) }.getOrDefault(PlanItemType.TASK),
            status = runCatching { PlanItemStatus.valueOf(status) }.getOrDefault(PlanItemStatus.PLANNED),
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            durationMinutes = durationMinutes,
            orderIndex = orderIndex,
            reason = reason,
            isBreak = isBreak
        )
    }

    companion object {
        fun fromDomain(item: PlanItem, planId: Long = item.dailyPlanId): PlanItemEntity {
            return PlanItemEntity(
                id = item.id,
                dailyPlanId = planId,
                taskId = item.taskId,
                taskTitle = item.taskTitle,
                subjectName = item.subjectName,
                type = item.type.name,
                status = item.status.name,
                startTimeMinutes = item.startTimeMinutes,
                endTimeMinutes = item.endTimeMinutes,
                durationMinutes = item.durationMinutes,
                orderIndex = item.orderIndex,
                reason = item.reason,
                isBreak = item.isBreak
            )
        }
    }
}
