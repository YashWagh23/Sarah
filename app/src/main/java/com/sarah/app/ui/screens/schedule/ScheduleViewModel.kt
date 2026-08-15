package com.sarah.app.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sarah.app.domain.model.CollegeSchedule
import com.sarah.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val schedule: CollegeSchedule = CollegeSchedule(),
    val isSaved: Boolean = false
)

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    init {
        scheduleRepository.getSchedule()
            .onEach { schedule ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        schedule = schedule ?: CollegeSchedule()
                    )
                }
            }.launchIn(viewModelScope)
    }

    fun updateSchedule(
        wakeTimeMinutes: Int,
        sleepTimeMinutes: Int,
        collegeStartTimeMinutes: Int,
        collegeEndTimeMinutes: Int,
        commuteMinutes: Int,
        dinnerBufferMinutes: Int,
        breakDurationMinutes: Int,
        preferredSessionLengthMinutes: Int
    ) {
        viewModelScope.launch {
            val updated = CollegeSchedule(
                id = 1,
                wakeTimeMinutes = wakeTimeMinutes,
                sleepTimeMinutes = sleepTimeMinutes,
                collegeStartTimeMinutes = collegeStartTimeMinutes,
                collegeEndTimeMinutes = collegeEndTimeMinutes,
                commuteMinutes = commuteMinutes,
                dinnerBufferMinutes = dinnerBufferMinutes,
                breakDurationMinutes = breakDurationMinutes,
                preferredSessionLengthMinutes = preferredSessionLengthMinutes
            )
            scheduleRepository.saveSchedule(updated)
            _uiState.update { it.copy(schedule = updated, isSaved = true) }
        }
    }

    class Factory(
        private val scheduleRepository: ScheduleRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ScheduleViewModel(scheduleRepository) as T
        }
    }
}
