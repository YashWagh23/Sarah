package com.sarah.app.data.repository

import com.sarah.app.data.local.dao.UserProfileDao
import com.sarah.app.data.local.entity.UserProfileEntity
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userProfileDao: UserProfileDao
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getUserProfile().map { it?.toDomain() }
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.saveUserProfile(UserProfileEntity.fromDomain(profile))
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        userProfileDao.setOnboardingCompleted(completed)
    }
}
