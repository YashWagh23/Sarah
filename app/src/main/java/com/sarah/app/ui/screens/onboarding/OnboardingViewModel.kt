package com.sarah.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.data.preferences.SarahPreferencesManager
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.model.UserProfile
import com.sarah.app.domain.repository.ScheduleRepository
import com.sarah.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,
    val name: String = "",
    val collegeName: String = "",
    val department: String = "",
    val semesterYear: String = "3rd Year",
    val sleepTimeMinutes: Int = 23 * 60 + 30, // 11:30 PM
    val collegeStartTimeMinutes: Int = 9 * 60, // 9:00 AM
    val collegeEndTimeMinutes: Int = 16 * 60 + 30, // 4:30 PM
    val isCompleted: Boolean = false
)

class OnboardingViewModel(
    private val userRepository: UserRepository,
    private val scheduleRepository: ScheduleRepository,
    private val preferencesManager: SarahPreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateName(name: String) = _uiState.update { it.copy(name = name) }
    fun updateCollege(college: String) = _uiState.update { it.copy(collegeName = college) }
    fun updateDepartment(department: String) = _uiState.update { it.copy(department = department) }
    fun updateSemester(sem: String) = _uiState.update { it.copy(semesterYear = sem) }
    fun updateSleepTime(mins: Int) = _uiState.update { it.copy(sleepTimeMinutes = mins) }
    fun updateCollegeHours(start: Int, end: Int) = _uiState.update { it.copy(collegeStartTimeMinutes = start, collegeEndTimeMinutes = end) }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = it.currentStep + 1) }
    }

    fun prevStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val state = _uiState.value
            val profile = UserProfile(
                id = 1,
                name = state.name.ifBlank { "Student" },
                collegeName = state.collegeName.ifBlank { "College of Engineering" },
                department = state.department.ifBlank { "Computer Science" },
                semesterYear = state.semesterYear,
                isOnboardingCompleted = true
            )
            val schedule = CollegeSchedule(
                id = 1,
                wakeTimeMinutes = 7 * 60,
                sleepTimeMinutes = state.sleepTimeMinutes,
                collegeStartTimeMinutes = state.collegeStartTimeMinutes,
                collegeEndTimeMinutes = state.collegeEndTimeMinutes,
                commuteMinutes = 45,
                dinnerBufferMinutes = 45,
                breakDurationMinutes = 15,
                preferredSessionLengthMinutes = 45
            )

            userRepository.saveUserProfile(profile)
            scheduleRepository.saveSchedule(schedule)
            preferencesManager.isOnboardingCompleted = true
            _uiState.update { it.copy(isCompleted = true) }
        }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val scheduleRepository: ScheduleRepository,
        private val preferencesManager: SarahPreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingViewModel(userRepository, scheduleRepository, preferencesManager) as T
        }
    }
}
