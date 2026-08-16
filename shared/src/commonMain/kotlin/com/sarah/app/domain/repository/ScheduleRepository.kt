package com.sarah.app.domain.repository

import com.sarah.app.domain.model.CollegeSchedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getSchedule(): Flow<CollegeSchedule?>
    suspend fun saveSchedule(schedule: CollegeSchedule)
}
