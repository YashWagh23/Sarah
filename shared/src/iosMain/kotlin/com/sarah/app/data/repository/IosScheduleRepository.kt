package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow

class IosScheduleRepository(
    private val database: IosSarahDatabase
) : ScheduleRepository {

    override fun getSchedule(): Flow<CollegeSchedule?> {
        return database.scheduleFlow
    }

    override suspend fun saveSchedule(schedule: CollegeSchedule) {
        database.scheduleFlow.value = schedule
        database.saveSchedule()
    }
}
