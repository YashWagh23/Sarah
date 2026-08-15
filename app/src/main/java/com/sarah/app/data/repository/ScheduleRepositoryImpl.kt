package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.ScheduleDao
import com.sarah.app.data.local.entity.ScheduleEntity
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ScheduleRepositoryImpl(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getSchedule(): Flow<CollegeSchedule?> {
        return scheduleDao.getSchedule().map { it?.toDomain() }
    }

    override suspend fun saveSchedule(schedule: CollegeSchedule) {
        scheduleDao.saveSchedule(ScheduleEntity.fromDomain(schedule))
    }
}
