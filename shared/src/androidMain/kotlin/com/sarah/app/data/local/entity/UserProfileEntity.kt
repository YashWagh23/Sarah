package com.sarah.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val name: String,
    val collegeName: String,
    val department: String,
    val semesterYear: String,
    val isOnboardingCompleted: Boolean,
    val defaultEnergyLevel: String
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            id = id,
            name = name,
            collegeName = collegeName,
            department = department,
            semesterYear = semesterYear,
            isOnboardingCompleted = isOnboardingCompleted,
            defaultEnergyLevel = runCatching { EnergyLevel.valueOf(defaultEnergyLevel) }.getOrDefault(EnergyLevel.NORMAL)
        )
    }

    companion object {
        fun fromDomain(profile: UserProfile): UserProfileEntity {
            return UserProfileEntity(
                id = profile.id,
                name = profile.name,
                collegeName = profile.collegeName,
                department = profile.department,
                semesterYear = profile.semesterYear,
                isOnboardingCompleted = profile.isOnboardingCompleted,
                defaultEnergyLevel = profile.defaultEnergyLevel.name
            )
        }
    }
}
