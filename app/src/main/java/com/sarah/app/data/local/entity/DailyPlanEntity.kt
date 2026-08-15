package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.FeasibilityStatus
import com.sarah.app.domain.model.NextAction
import com.sarah.app.domain.model.NextActionType
import com.sarah.app.domain.model.PlanItem
import com.sarah.app.domain.model.SchedulePaceStatus
import com.sarah.app.domain.model.ScheduleProgress
import com.sarah.app.domain.model.Task

@Entity(tableName = "daily_plans")
data class DailyPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val generatedAtEpochMs: Long,
    val updatedAtEpochMs: Long,
    val availableMinutes: Int,
    val realisticCapacityMinutes: Int,
    val requiredMinutes: Int,
    val feasibilityStatus: String,
    val currentEnergy: String,
    val completedMinutes: Int,
    val totalPlannedMinutes: Int,
    val completedTaskCount: Int,
    val totalTaskCount: Int,
    val minutesBehindSchedule: Int,
    val paceStatus: String,
    val nextActionTitle: String?,
    val nextActionSubtitle: String?,
    val nextActionType: String?,
    val nextActionTaskId: Long?,
    val nextActionDurationMinutes: Int?,
    val nextActionReason: String?,
    val nextActionUrgencyBadge: String?
) {
    fun toDomain(items: List<PlanItem> = emptyList(), deferredTasks: List<Task> = emptyList()): DailyPlan {
        val nextAction = if (nextActionTitle != null && nextActionSubtitle != null) {
            NextAction(
                title = nextActionTitle,
                subtitle = nextActionSubtitle,
                actionType = runCatching { NextActionType.valueOf(nextActionType ?: "START_TASK") }.getOrDefault(NextActionType.START_TASK),
                taskId = nextActionTaskId,
                durationMinutes = nextActionDurationMinutes ?: 0,
                reason = nextActionReason ?: "",
                urgencyBadge = nextActionUrgencyBadge ?: ""
            )
        } else null

        return DailyPlan(
            id = id,
            dateEpochDay = dateEpochDay,
            generatedAtEpochMs = generatedAtEpochMs,
            updatedAtEpochMs = updatedAtEpochMs,
            availableMinutes = availableMinutes,
            realisticCapacityMinutes = realisticCapacityMinutes,
            requiredMinutes = requiredMinutes,
            feasibilityStatus = runCatching { FeasibilityStatus.valueOf(feasibilityStatus) }.getOrDefault(FeasibilityStatus.MANAGEABLE),
            currentEnergy = runCatching { EnergyLevel.valueOf(currentEnergy) }.getOrDefault(EnergyLevel.NORMAL),
            items = items,
            deferredTasks = deferredTasks,
            scheduleProgress = ScheduleProgress(
                completedMinutes = completedMinutes,
                totalPlannedMinutes = totalPlannedMinutes,
                completedTaskCount = completedTaskCount,
                totalTaskCount = totalTaskCount,
                minutesBehindSchedule = minutesBehindSchedule,
                paceStatus = runCatching { SchedulePaceStatus.valueOf(paceStatus) }.getOrDefault(SchedulePaceStatus.ON_TRACK)
            ),
            nextAction = nextAction
        )
    }

    companion object {
        fun fromDomain(plan: DailyPlan): DailyPlanEntity {
            return DailyPlanEntity(
                id = plan.id,
                dateEpochDay = plan.dateEpochDay,
                generatedAtEpochMs = plan.generatedAtEpochMs,
                updatedAtEpochMs = plan.updatedAtEpochMs,
                availableMinutes = plan.availableMinutes,
                realisticCapacityMinutes = plan.realisticCapacityMinutes,
                requiredMinutes = plan.requiredMinutes,
                feasibilityStatus = plan.feasibilityStatus.name,
                currentEnergy = plan.currentEnergy.name,
                completedMinutes = plan.scheduleProgress.completedMinutes,
                totalPlannedMinutes = plan.scheduleProgress.totalPlannedMinutes,
                completedTaskCount = plan.scheduleProgress.completedTaskCount,
                totalTaskCount = plan.scheduleProgress.totalTaskCount,
                minutesBehindSchedule = plan.scheduleProgress.minutesBehindSchedule,
                paceStatus = plan.scheduleProgress.paceStatus.name,
                nextActionTitle = plan.nextAction?.title,
                nextActionSubtitle = plan.nextAction?.subtitle,
                nextActionType = plan.nextAction?.actionType?.name,
                nextActionTaskId = plan.nextAction?.taskId,
                nextActionDurationMinutes = plan.nextAction?.durationMinutes,
                nextActionReason = plan.nextAction?.reason,
                nextActionUrgencyBadge = plan.nextAction?.urgencyBadge
            )
        }
    }
}
