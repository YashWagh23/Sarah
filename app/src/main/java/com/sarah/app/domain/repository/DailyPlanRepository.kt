package com.sarah.app.domain.repository

import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.TemporaryInterruption
import kotlinx.coroutines.flow.Flow

interface DailyPlanRepository {
    fun getDailyPlan(dateEpochDay: Long): Flow<DailyPlan?>
    suspend fun saveDailyPlan(plan: DailyPlan): Long
    suspend fun updatePlanItemStatus(itemId: Long, status: PlanItemStatus)
    fun getInterruptions(dateEpochDay: Long): Flow<List<TemporaryInterruption>>
    suspend fun addInterruption(interruption: TemporaryInterruption): Long
    suspend fun deleteInterruption(id: Long)
    suspend fun clearInterruptions(dateEpochDay: Long)
}
