package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.TemporaryInterruption
import com.sarah.app.domain.repository.DailyPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class IosDailyPlanRepository(
    private val database: IosSarahDatabase
) : DailyPlanRepository {

    override fun getDailyPlan(dateEpochDay: Long): Flow<DailyPlan?> {
        return database.dailyPlansFlow.map { it[dateEpochDay] }
    }

    override suspend fun saveDailyPlan(plan: DailyPlan): Long {
        val currentMap = database.dailyPlansFlow.value.toMutableMap()
        val newId = if (plan.id > 0) plan.id else (currentMap.values.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = plan.copy(id = newId)
        currentMap[plan.dateEpochDay] = toInsert
        database.dailyPlansFlow.value = currentMap
        database.saveDailyPlans()
        return newId
    }

    override suspend fun updatePlanItemStatus(itemId: Long, status: PlanItemStatus) {
        val currentMap = database.dailyPlansFlow.value.toMutableMap()
        var changed = false
        for ((date, plan) in currentMap) {
            val itemIndex = plan.items.indexOfFirst { it.id == itemId }
            if (itemIndex >= 0) {
                val updatedItems = plan.items.toMutableList()
                updatedItems[itemIndex] = updatedItems[itemIndex].copy(status = status)
                currentMap[date] = plan.copy(items = updatedItems)
                changed = true
                break
            }
        }
        if (changed) {
            database.dailyPlansFlow.value = currentMap
            database.saveDailyPlans()
        }
    }

    override fun getInterruptions(dateEpochDay: Long): Flow<List<TemporaryInterruption>> {
        return database.interruptionsFlow.map { it[dateEpochDay] ?: emptyList() }
    }

    override suspend fun addInterruption(interruption: TemporaryInterruption): Long {
        val currentMap = database.interruptionsFlow.value.toMutableMap()
        val listForDay = (currentMap[interruption.dateEpochDay] ?: emptyList()).toMutableList()
        val allInterruptions = currentMap.values.flatten()
        val newId = if (interruption.id > 0) interruption.id else (allInterruptions.maxOfOrNull { it.id } ?: 0L) + 1L
        val toInsert = interruption.copy(id = newId)
        listForDay.add(toInsert)
        currentMap[interruption.dateEpochDay] = listForDay
        database.interruptionsFlow.value = currentMap
        database.saveInterruptions()
        return newId
    }

    override suspend fun deleteInterruption(id: Long) {
        val currentMap = database.interruptionsFlow.value.toMutableMap()
        var changed = false
        for ((date, list) in currentMap) {
            if (list.any { it.id == id }) {
                currentMap[date] = list.filterNot { it.id == id }
                changed = true
                break
            }
        }
        if (changed) {
            database.interruptionsFlow.value = currentMap
            database.saveInterruptions()
        }
    }

    override suspend fun clearInterruptions(dateEpochDay: Long) {
        val currentMap = database.interruptionsFlow.value.toMutableMap()
        if (currentMap.containsKey(dateEpochDay)) {
            currentMap.remove(dateEpochDay)
            database.interruptionsFlow.value = currentMap
            database.saveInterruptions()
        }
    }
}
