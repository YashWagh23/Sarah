package com.sarah.app.preferences

import com.sarah.app.domain.model.EnergyLevel
import com.sarah.app.domain.preferences.SarahPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

class IosSarahPreferences(
    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults
) : SarahPreferences {

    companion object {
        const val KEY_CURRENT_ENERGY = "key_current_energy"
        const val KEY_ONBOARDING_DONE = "key_onboarding_done"
        const val KEY_DEADLINE_REMINDERS = "key_deadline_reminders"
        const val KEY_CUSTOM_REMINDERS = "key_custom_reminders"
    }

    private val _energyLevelFlow = MutableStateFlow(readCurrentEnergyLevel())
    override val energyLevelFlow: Flow<EnergyLevel> = _energyLevelFlow.asStateFlow()

    private val _onboardingCompletedFlow = MutableStateFlow(readIsOnboardingCompleted())
    override val onboardingCompletedFlow: Flow<Boolean> = _onboardingCompletedFlow.asStateFlow()

    private val _deadlineRemindersEnabledFlow = MutableStateFlow(readIsDeadlineRemindersEnabled())
    override val deadlineRemindersEnabledFlow: Flow<Boolean> = _deadlineRemindersEnabledFlow.asStateFlow()

    private val _customRemindersEnabledFlow = MutableStateFlow(readIsCustomRemindersEnabled())
    override val customRemindersEnabledFlow: Flow<Boolean> = _customRemindersEnabledFlow.asStateFlow()

    private fun readCurrentEnergyLevel(): EnergyLevel {
        val str = userDefaults.stringForKey(KEY_CURRENT_ENERGY) ?: EnergyLevel.NORMAL.name
        return runCatching { EnergyLevel.valueOf(str) }.getOrDefault(EnergyLevel.NORMAL)
    }

    private fun readIsOnboardingCompleted(): Boolean {
        return userDefaults.boolForKey(KEY_ONBOARDING_DONE)
    }

    private fun readIsDeadlineRemindersEnabled(): Boolean {
        val obj = userDefaults.objectForKey(KEY_DEADLINE_REMINDERS)
        return if (obj != null) userDefaults.boolForKey(KEY_DEADLINE_REMINDERS) else true
    }

    private fun readIsCustomRemindersEnabled(): Boolean {
        val obj = userDefaults.objectForKey(KEY_CUSTOM_REMINDERS)
        return if (obj != null) userDefaults.boolForKey(KEY_CUSTOM_REMINDERS) else true
    }

    override var currentEnergyLevel: EnergyLevel
        get() = readCurrentEnergyLevel()
        set(value) {
            userDefaults.setObject(value.name, KEY_CURRENT_ENERGY)
            _energyLevelFlow.value = value
        }

    override var isOnboardingCompleted: Boolean
        get() = readIsOnboardingCompleted()
        set(value) {
            userDefaults.setBool(value, KEY_ONBOARDING_DONE)
            _onboardingCompletedFlow.value = value
        }

    override var isDeadlineRemindersEnabled: Boolean
        get() = readIsDeadlineRemindersEnabled()
        set(value) {
            userDefaults.setBool(value, KEY_DEADLINE_REMINDERS)
            _deadlineRemindersEnabledFlow.value = value
        }

    override var isCustomRemindersEnabled: Boolean
        get() = readIsCustomRemindersEnabled()
        set(value) {
            userDefaults.setBool(value, KEY_CUSTOM_REMINDERS)
            _customRemindersEnabledFlow.value = value
        }
}
