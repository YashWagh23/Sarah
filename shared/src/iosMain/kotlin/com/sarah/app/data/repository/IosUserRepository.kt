package com.sarah.app.data.repository

import com.sarah.app.data.local.IosSarahDatabase
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class IosUserRepository(
    private val database: IosSarahDatabase
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return database.userProfileFlow
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        database.userProfileFlow.value = profile
        database.saveUserProfile()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        val current = database.userProfileFlow.value ?: UserProfile()
        database.userProfileFlow.value = current.copy(isOnboardingCompleted = completed)
        database.saveUserProfile()
    }
}
