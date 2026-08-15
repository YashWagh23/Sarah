package com.sarah.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val isSaved: Boolean = false
)

class ProfileViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        userRepository.getUserProfile()
            .onEach { profile ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userProfile = profile ?: UserProfile()
                    )
                }
            }.launchIn(viewModelScope)
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
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userRepository) as T
        }
    }
}
