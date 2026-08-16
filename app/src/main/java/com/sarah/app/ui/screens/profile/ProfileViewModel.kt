package com.sarah.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferences
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



class ProfileViewModel(
    private val userRepository: UserRepository,
    private val preferencesManager: SarahPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        combine(
            userRepository.getUserProfile(),
            preferencesManager.deadlineRemindersEnabledFlow,
            preferencesManager.customRemindersEnabledFlow
        ) { profile, deadlineReminders, customReminders ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    userProfile = profile ?: UserProfile(),
                    isDeadlineRemindersEnabled = deadlineReminders,
                    isCustomRemindersEnabled = customReminders
                )
            }
        }.launchIn(viewModelScope)
    }

    fun setDeadlineRemindersEnabled(enabled: Boolean) {
        preferencesManager.isDeadlineRemindersEnabled = enabled
    }

    fun setCustomRemindersEnabled(enabled: Boolean) {
        preferencesManager.isCustomRemindersEnabled = enabled
    }

    fun saveProfile(
        name: String,
        collegeName: String,
        department: String,
        semesterYear: String,
        defaultEnergy: EnergyLevel
    ) {
        viewModelScope.launch {
            val updated = UserProfile(
                id = 1,
                name = name,
                collegeName = collegeName,
                department = department,
                semesterYear = semesterYear,
                isOnboardingCompleted = true,
                defaultEnergyLevel = defaultEnergy
            )
            userRepository.saveUserProfile(updated)
            _uiState.update { it.copy(userProfile = updated, isSaved = true) }
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val preferencesManager: SarahPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userRepository, preferencesManager) as T
        }
    }
}
