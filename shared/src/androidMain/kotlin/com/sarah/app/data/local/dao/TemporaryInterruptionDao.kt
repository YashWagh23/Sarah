package com.sarah.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sarah.app.data.local.entity.TemporaryInterruptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemporaryInterruptionDao {
    @Query("SELECT * FROM temporary_interruptions WHERE dateEpochDay = :dateEpochDay ORDER BY startMinutes ASC")
    fun getInterruptionsForDate(dateEpochDay: Long): Flow<List<TemporaryInterruptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterruption(interruption: TemporaryInterruptionEntity): Long

    @Query("DELETE FROM temporary_interruptions WHERE id = :id")
    suspend fun deleteInterruption(id: Long)

    @Query("DELETE FROM temporary_interruptions WHERE dateEpochDay = :dateEpochDay")
    suspend fun clearInterruptionsForDate(dateEpochDay: Long)
}
