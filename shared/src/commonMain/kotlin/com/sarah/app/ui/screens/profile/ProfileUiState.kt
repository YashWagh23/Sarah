package com.sarah.app.ui.screens.profile

import com.sarah.app.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val userProfile: UserProfile = UserProfile(),
    val isDeadlineRemindersEnabled: Boolean = true,
    val isCustomRemindersEnabled: Boolean = true,
    val isSaved: Boolean = false
)
