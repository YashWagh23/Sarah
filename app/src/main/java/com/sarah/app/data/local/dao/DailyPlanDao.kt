package com.sarah.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sarah.app.data.local.entity.DailyPlanEntity
import com.sarah.app.data.local.entity.PlanItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanDao {
    @Query("SELECT * FROM daily_plans WHERE dateEpochDay = :dateEpochDay ORDER BY updatedAtEpochMs DESC LIMIT 1")
    fun getDailyPlanByDate(dateEpochDay: Long): Flow<DailyPlanEntity?>

    @Query("SELECT * FROM daily_plans WHERE id = :id")
    suspend fun getDailyPlanById(id: Long): DailyPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyPlan(plan: DailyPlanEntity): Long

    @Update
    suspend fun updateDailyPlan(plan: DailyPlanEntity)

    @Query("SELECT * FROM plan_items WHERE dailyPlanId = :dailyPlanId ORDER BY orderIndex ASC, startTimeMinutes ASC")
    fun getPlanItemsForPlan(dailyPlanId: Long): Flow<List<PlanItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlanItems(items: List<PlanItemEntity>)

    @Query("DELETE FROM plan_items WHERE dailyPlanId = :dailyPlanId")
    suspend fun deletePlanItemsForPlan(dailyPlanId: Long)

    @Query("UPDATE plan_items SET status = :status WHERE id = :itemId")
    suspend fun updatePlanItemStatus(itemId: Long, status: String)

    @Transaction
    suspend fun saveFullPlan(plan: DailyPlanEntity, items: List<PlanItemEntity>): Long {
        val planId = insertDailyPlan(plan)
        deletePlanItemsForPlan(planId)
        val itemsWithPlanId = items.map { it.copy(dailyPlanId = planId) }
        insertPlanItems(itemsWithPlanId)
        return planId
    }
}
