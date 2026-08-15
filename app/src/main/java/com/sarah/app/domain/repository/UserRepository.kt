package com.sarah.app.domain.repository

import com.sarah.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
