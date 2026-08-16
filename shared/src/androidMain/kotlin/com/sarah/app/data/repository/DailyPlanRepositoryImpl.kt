package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.DailyPlanDao
import com.sarah.app.data.local.dao.TemporaryInterruptionDao
import com.sarah.app.data.local.entity.DailyPlanEntity
import com.sarah.app.data.local.entity.PlanItemEntity
import com.sarah.app.data.local.entity.TemporaryInterruptionEntity
import com.sarah.app.domain.model.DailyPlan
import com.sarah.app.domain.model.PlanItemStatus
import com.sarah.app.domain.model.TemporaryInterruption
import com.sarah.app.domain.repository.DailyPlanRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DailyPlanRepositoryImpl(
    private val dailyPlanDao: DailyPlanDao,
    private val interruptionDao: TemporaryInterruptionDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DailyPlanRepository {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override fun getDailyPlan(dateEpochDay: Long): Flow<DailyPlan?> {
        return dailyPlanDao.getDailyPlanByDate(dateEpochDay).flatMapLatest { planEntity ->
            if (planEntity == null) {
                flowOf(null)
            } else {
                dailyPlanDao.getPlanItemsForPlan(planEntity.id).map { itemEntities ->
                    val items = itemEntities.map { it.toDomain() }
                    planEntity.toDomain(items = items)
                }
            }
        }
    }

    override suspend fun saveDailyPlan(plan: DailyPlan): Long = withContext(ioDispatcher) {
        val planEntity = DailyPlanEntity.fromDomain(plan)
        val itemEntities = plan.items.map { PlanItemEntity.fromDomain(it) }
        dailyPlanDao.saveFullPlan(planEntity, itemEntities)
    }

    override suspend fun updatePlanItemStatus(itemId: Long, status: PlanItemStatus) = withContext(ioDispatcher) {
        dailyPlanDao.updatePlanItemStatus(itemId, status.name)
    }

    override fun getInterruptions(dateEpochDay: Long): Flow<List<TemporaryInterruption>> {
        return interruptionDao.getInterruptionsForDate(dateEpochDay).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addInterruption(interruption: TemporaryInterruption): Long = withContext(ioDispatcher) {
        interruptionDao.insertInterruption(TemporaryInterruptionEntity.fromDomain(interruption))
    }

    override suspend fun deleteInterruption(id: Long) = withContext(ioDispatcher) {
        interruptionDao.deleteInterruption(id)
    }

    override suspend fun clearInterruptions(dateEpochDay: Long) = withContext(ioDispatcher) {
        interruptionDao.clearInterruptionsForDate(dateEpochDay)
    }
}
