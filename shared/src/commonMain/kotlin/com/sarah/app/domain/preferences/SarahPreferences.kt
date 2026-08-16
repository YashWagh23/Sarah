package com.sarah.app.domain.preferences

import com.sarah.app.domain.model.EnergyLevel
import kotlinx.coroutines.flow.Flow

interface SarahPreferences {
    var currentEnergyLevel: EnergyLevel
    val energyLevelFlow: Flow<EnergyLevel>

    var isOnboardingCompleted: Boolean
    val onboardingCompletedFlow: Flow<Boolean>

    var isDeadlineRemindersEnabled: Boolean
    val deadlineRemindersEnabledFlow: Flow<Boolean>

    var isCustomRemindersEnabled: Boolean
    val customRemindersEnabledFlow: Flow<Boolean>
}
